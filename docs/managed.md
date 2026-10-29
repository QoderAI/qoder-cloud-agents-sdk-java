# Managed API

Managed support is included with the Forward additions in **0.2.0**. The [coverage inventory](managed-api-coverage.md) records 108 documented operations, including two SSE endpoints, checked against the [official documentation](https://docs.qoder.com/cloud-agents/overview) on 2026-10-28.

## Client and authentication

Use `ManagedClient` or `AsyncManagedClient`; resource models live in `com.qoder.cloudagents.managed`. The root client shares the existing Java 8 transport, credentials, request options, retry/cancellation handling and typed errors with Forward. Models are immutable, preserve unknown JSON fields, and keep omitted fields distinct from explicit JSON null.

```java
import com.qoder.cloudagents.ManagedClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.managed.*;

try (ManagedClient client = ManagedClient.builder()
        .baseUrl("https://api.qoder.com.cn/api/v1/cloud")
        .pat(System.getenv("QODER_PAT"))
        .build()) {
    ModelListResponse models = client.models().list();
    Agent agent = client.agents().create(AgentCreateParams.builder()
        .name("reviewer")
        .model("auto")
        .system("Review code and explain your findings.")
        .build());
}
```

The base URL is resolved from `.baseUrl(...)`, then `QODER_BASE_URL`, then Global `https://api.qoder.com/api/v1/cloud`. CN uses `https://api.qoder.com.cn/api/v1/cloud`. PAT and static SAT use the same configuration rules as Forward; `ServiceAccountCredential` exchanges an SA Key for a platform SAT with `qca.access`. The examples' `ManagedExampleClient` handles PAT, SA Key and static SAT selection. Managed does not expose Forward's Identity query selector.

`client.async()` shares the parent's transport; closing that view is a no-op. `ManagedClient.builder().buildAsync()` owns its transport. `withOptions(...)` creates an independently closeable client. Ordinary HTTP methods also offer `withResponse()` for status, headers and request ID. SSE streams expose handshake metadata through `response()` and must be closed.

## Agent versions and Session runtime

Agent creation requires a name and model. Updates require the current `AgentUpdateParams.version(...)` for optimistic concurrency. `agents().retrieve(id, AgentRetrieveParams.builder().version(n).build())` reads a version snapshot, and `agents().versions().list(id)` lists snapshots. Use the returned opaque `next_page`: Agent snapshots share an Agent ID.

Models accept an ID string or `ModelConfig` with `id`, `effort` and `contextWindow`. Managed does not accept Forward's `speed` setting. Session responses can additionally expose `effectiveContextWindow`. Multiagent rosters support string Agent references, `{type: agent, id, version}`, `self`, and an Advisor with a model ID. Child versions support numeric values and `latest`; preserve the server-resolved snapshot when reading sessions.

```java
Session session = client.sessions().create(SessionCreateParams.builder()
    .agent(AgentReference.builder().type("agent").id(agent.id()).version(agent.version()).build())
    .environmentId(environmentId)
    .budget(SessionBudget.builder().type("limit").maxCreditCost("100.00").build())
    .build());

client.sessions().update(session.id(), SessionUpdateParams.builder()
    .agent(SessionAgentPatch.builder().system("Use the updated review policy.").build())
    .build(), RequestOptions.builder()
        .header("x-qoder-beta", "session-agent-patch-2026-07-21")
        .build());
```

`SessionAgentPatch` changes supported runtime fields (`model`, `system`, `tools`, `mcpServers`, `skills`, `name`, `description`). It does not repin an Agent ID/version or change the multiagent roster. Send the documented `session-agent-patch-2026-07-21` Beta header for model, prompt, skill, name or description changes. Browser tools additionally require their documented Beta header. See [official Session updates](https://docs.qoder.com/cloud-agents/api/sessions/update).

Typed Session creation adds exactly three string metadata fields: `__qca_sdk_created=true`, `__qca_sdk_language=java`, and `__qca_sdk_version` from the installed JAR. Same-value markers are accepted; different values or types fail before HTTP. Custom fields and unknown prefixed keys remain unchanged, and caller maps are not mutated. These entries count toward the service's metadata limit. Updates and generic `request(...)` calls do not inject markers.

Budget values are positive decimal strings with up to 12 integer and two fractional digits. Omit or pass null when creating an unlimited Session. When updating, omission preserves the budget and `.budget(null)` removes it. Credits use `BigDecimal`: Session usage and top-level `session.usage` events expose `modelCredits`, `sandboxRuntimeCredits` and `totalCredits`; model-request and compaction events expose nested `modelUsageCredits` and `usageCredits`.

Session environment variables use `Map<String,String>`. Deployment environment variables use a string of `KEY=VALUE` lines. Session metadata updates are merge patches; null values remove keys, while top-level null is a service no-op. Deployment metadata has its own merge-patch rules. The SDK preserves the submitted representation.

## Events, resources and pagination

`SessionEventParam` and `EventContent.text(...)` build user events as content-block arrays. List events with `SessionEventListParams.types(...)`, which sends the Managed `types` query key; Session lifecycle filters use `statuses`. Date comparisons use `createdAtGt/Gte/Lt/Lte` where supported.

```java
SessionEventSendResponse sent = client.sessions().events().send(session.id(),
    SessionEventSendParams.builder().events(java.util.Collections.singletonList(
        SessionEventParam.builder().type("user.message")
            .content(EventContent.text("Hello")).build())).build());
try (com.qoder.cloudagents.streaming.EventStream<SessionEvent> stream =
        client.sessions().events().streamEvents(session.id(),
            SessionEventStreamParams.builder().lastEventId(sent.data().get(0).id()).build())) {
    for (SessionEvent event : stream) {
        if ("session.status_idle".equals(event.type())) break;
    }
}
```

Session streams accept repeated `event_deltas[]` selectors; thread streams accept no query selectors and use `/sessions/{session}/threads/{thread}/stream`. Both put the resume cursor in `Last-Event-ID`. Per-call headers override parameter/default cursors. `resumableStream(...)` is explicit and never resends user messages. Closing a stream cancels the subscription, not the Session.

Create-time resources support File, GitHub, generic Git and Memory Store attachments. `SessionResourceSpec` includes checkout, credentials, mount path and memory access as applicable. Existing Sessions can add File resources through `sessions().resources().create(...)`; resource updates rotate repository credentials. Credentials are request fields; responses do not expose them.

Cursor lists retain supported filters and request options; `getNextPageAsync()` remains cancellable. Search uses POST JSON bodies and automatically adds `search-2026-08-31`, preserving other selected Betas and opaque page tokens. File scope filtering cannot be combined with cursor continuation. Models and Dreams return non-paginated collections; memory entry/version lists expose only the filters documented by Managed.

## Deployments, Work and resources

- `deployments()` supports create/retrieve/update/list/search, pause/unpause/archive and manual run. Omit `schedule` for manual-only execution. Use `deployments().runs()` for scoped runs and `deploymentRuns()` for global runs.
- `environments().work()` supports poll, ack, heartbeat, retrieve/list/stats, metadata update and stop for self-hosted environments. Poll may return null. Heartbeat parameters go in the query. Supply `Worker-ID` through `RequestOptions.header(...)`. Poll/ack/heartbeat are never automatically retried because they claim or advance a lease.
- Environment storage supports typed OSS buckets, RAM roles and access-key credentials. Set `x-qoder-beta: oss-bucket-2026-09-23` explicitly when required by the [storage contract](https://docs.qoder.com/cloud-agents/api/environments/schemas). No dedicated OSS live configuration is included in the test suite.
- Files support multipart upload, metadata, search, URL retrieval and isolated streaming download. Skill creation accepts `displayTitle`, repeatable files and metadata; Skill Versions support immutable upload/retrieve/list/delete and ZIP download. The deprecated Skill PUT update remains available for compatibility.
- Vaults support archives and credential CRUD/archive/validation. `oauth().start(...)` sends optional client secrets in the JSON body and returns `authorizationUrl`, `state` and `callbackOrigin`. User authorization still happens at the provider. Credential updates, OAuth startup and validation disable automatic retries to avoid replaying token rotations or OAuth state changes.
- Dream inputs support a single primary Memory Store plus optional Session IDs. Forward's `identity_template` input does not apply to Managed. `Dream.sessionId()` exposes the execution Session when available.

## Verification and release

The offline suite tests all 106 HTTP operations in four call styles, both SSE routes, exact wire placement, pagination, metadata/budget semantics, and response examples from the official docs. The independent examples project compiles and tests against the installed JAR. Run [the opt-in Managed integration entry](managed-live-testing.md) separately; offline coverage is not a claim that all operations passed against production.

Forward and Managed are delivered together in 0.2.0. Before publishing, run internal integration tests from the merged version commit and accept that build's QA package; results from an earlier development package do not replace version-package acceptance.
