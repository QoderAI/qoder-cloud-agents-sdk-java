# Session HTTP APIs and Memory Store mounts

Session, event history, file attachment and Thread HTTP APIs share the client's PAT/SA credential, timeout, retry and error handling. This guide covers 13 ordinary HTTP operations. Session and Thread SSE subscriptions are described in [streaming](session-streaming.md).

## Methods

| Resource | Methods | Response |
| --- | --- | --- |
| `client.sessions()` | `create(params)`, `retrieve(sessionId)`, `update(sessionId, params)` | `Session` |
| | `list()`, `list(params)` | `SessionListResponse` |
| | `archive(sessionId)`, `cancel(sessionId)` | `Session`, possibly a partial response |
| `client.sessions().events()` | `list(sessionId, params)` | `SessionEventListResponse` |
| | `send(sessionId, params)` | `SessionEventSendResponse` |
| `client.sessions().resources()` | `add(sessionId, params)` | `SessionResource` |
| `client.sessions().threads()` | `list(sessionId, params)` | `SessionThreadListResponse` |
| | `retrieve(sessionId, threadId)`, `archive(sessionId, threadId)` | `SessionThread` |
| `client.sessions().threads().events()` | `list(sessionId, threadId, params)` | `SessionEventListResponse` |

List methods also have overloads without params. Every operation accepts a final `RequestOptions` argument and has a `withResponse()` view. `client.async()` exposes equivalent methods returning cancellable `CompletableFuture` results.

## Create and update

```java
import com.qoder.cloudagents.forward.*;
import com.qoder.cloudagents.core.RequestOptions;
import java.util.Collections;
import java.util.UUID;

Session session = client.sessions().create(
    SessionCreateParams.builder()
        .identityId("idn_example")
        .templateId("tmpl_example")
        .title("Support request")
        .config(SessionConfig.builder()
            .environmentVariables(Collections.singletonMap("LOCALE", "zh-CN"))
            .build())
        .build(),
    RequestOptions.builder().idempotencyKey(UUID.randomUUID().toString()).build());
```

Creation requires an Identity and Template accessible to the credential. The service compiles the Template and Identity configuration and applies the Session's environment variables. The SDK does not supply source type, resolve ownership or merge configuration locally.

Session update allows title, metadata and `config.environment_variables`. The environment-variable map **replaces the whole Session-level map**, then overlays the Template/Identity base. An empty map clears the Session override; an omitted field leaves it unchanged. The service rejects null config/environment-variable maps. Builders preserve explicit null for wire fidelity; they do not silently turn it into omission or an empty map.

```java
client.sessions().update(session.id(), SessionUpdateParams.builder()
    .config(SessionConfig.builder().environmentVariables(Collections.emptyMap()).build())
    .build());
```

Metadata follows the endpoint's merge behavior. File resources can be supplied on creation with `SessionResourceSpec`; later additions use `sessions().resources().add(...)`, not Session update.

## Send and read events

```java
SessionEventSendResponse accepted = client.sessions().events().send(
    session.id(),
    SessionEventSendParams.builder().events(Collections.singletonList(
        SessionEventParam.builder()
            .type("user.message")
            .content(EventContent.text("Summarize the project."))
            .build())).build(),
    RequestOptions.builder().idempotencyKey(UUID.randomUUID().toString()).build());
System.out.println(accepted.requestId());
```

`EventContent.text(...)` creates the content-block array required for a user message. Use `EventContent.blocks(...)` with typed `ContentBlock` and `ImageSource` for text/image inputs. `EventContent.ofString(...)` represents string content for event types such as tool results; do not use it for a `user.message`.

Since 0.2.0, `ImageSource.builder().type("file").fileId(fileId)` and `fileId()` support images referenced by file ID. The service validates the image source and file permissions.

`SessionEventParam` also exposes tool confirmation/result fields, custom tool result IDs, interrupt reason and outcome definition. `OutcomeRubric` uses `content` for `type="text"` and `fileId` for `type="file"`. The deprecated `text` setter now writes `content`; migrate raw JSON explicitly. Event-specific validation and permissions remain server-side.

A successful send acknowledges accepted input; it does not mean the model finished. Reading history returns persisted events, not a live subscription:

```java
SessionEventListResponse page = client.sessions().events().list(
    session.id(), SessionEventListParams.builder()
        .types(java.util.Arrays.asList("agent.message", "session.status_idle"))
        .includeToolCalls(true).includeThinking(false).limit(20).build());
for (SessionEvent event : page) {
    System.out.println(event.id() + " " + event.type());
}
```

Known event fields are typed. Unknown event names, content-block types and JSON fields remain accessible through `field(...)` / `toJsonValue()`. Optional payload fields return null when absent; malformed known values produce a validation exception when accessed. Avoid logging complete event/config bodies because they may contain user content or credentials.

The SDK preserves response order and does not infer task completion from the last event in one page. It also does not automatically resend events after an ambiguous network failure. Ordinary writes without an idempotency key retain the shared conservative retry policy; supplying a key allows eligible retries, but HTTP 409 remains non-retryable.

## Pagination and selectors

- Session list: search, Identity IDs, Template ID, source type, created/updated time comparisons, order and archived inclusion.
- Session event history: order, event types, tool/thinking inclusion.
- Thread list and Thread event history: only limit and before/after cursors in typed parameters; their order is fixed by the service.

Each cursor list supports `data()`, `hasNextPage()`, `getNextPage()`, `getNextPageAsync()`, lazy item iteration and `iterPages()`. Filters, nested Session/Thread IDs and request options follow subsequent pages. Options apply separately to each page.

`SessionListParams.identityIds(list)` and `SessionEventListParams.types(list)` use the documented comma-separated query forms `identity_ids` and `type`. Session time filters are named `createdAtGt/Gte/Lt/Lte` and `updatedAtGt/Gte/Lt/Lte`; strings are sent as provided for service validation. Dates are not converted to the machine's local timezone.

## Cancel, archive and attach files

`sessions().cancel(id)` requests cancellation of the current turn and accepts HTTP 200 or 202. A partial response such as `status=canceling` means the request was accepted. Retrieve the Session or, once implemented, observe its stream to confirm completion. The SDK does not automatically archive immediately after cancellation.

Cancelling an asynchronous Java future is different: it stops that HTTP request locally and does not send the Session cancel API.

Archive retains the Session record. Thread archive is limited by the service to idle child Threads; coordinator or running Threads may return 409. Use a new idempotency key for a new logical archive attempt after state changes, and retrieve the Thread to confirm its final state.

```java
SessionResource resource = client.sessions().resources().add(
    session.id(), SessionResourceAddParams.builder()
        .type("file").fileId("file_already_uploaded")
        .mountPath("/data/workspace/spec.md").build());
System.out.println(resource.mountPath());
```

The file must already exist and be uploaded. This operation mounts it; it does not upload bytes or notify the Agent. Send a separate user message containing the returned path when the Agent should process it. Use the [Files API](environments-files.md) to upload the content first.

## Response numbers and variants

Session/Thread usage and duration fields use `BigDecimal` to preserve JSON numeric precision. Current documented Session duration values are whole seconds; the SDK also preserves fractional values if returned. Counts, versions and iteration values use 64-bit integers. Missing usage differs from an explicit zero.

Session Template summaries accept the existing string/object model representation. Thread responses expose parent, role, status, stop reason, stats, usage and archive time. The SDK preserves unknown states without converting them to a fixed enum.

## Memory Store mounts

```java
MemoryStoreMount mount = client.identities().memoryStores().mount(
    "idn_example", "tmpl_example",
    IdentityMemoryStoreMountParams.builder().memoryStoreId("memstore_example").build());

IdentityMemoryStoreListResponse mounts =
    client.identities().memoryStores().list("idn_example", "tmpl_example");
for (MemoryStoreMount item : mounts.data()) {
    System.out.println(item.memoryStoreId() + " " + item.access());
}

DeletedMemoryStoreMount detached =
    client.identities().memoryStores().detach("idn_example", "tmpl_example", "memstore_example");
```

These three methods also support async, request options and `withResponse()`. Listing returns one bounded list and has no automatic pagination despite its `has_more` field. The SDK preserves the service's order. Explicit mounts are read-only; the service owns default-Store creation, limits and permission checks. Detach removes the association only and does not delete the Store or its memories. Detaching the system default Store can return 409.

See the [isolated lifecycle example](../examples/README.md#session-lifecycle-and-optional-memory-store-mount) for cleanup that leaves failures recoverable.

## Contract and testing

Implementation follows the [pinned SDK baseline](forward-api-coverage.md). Newer documented fields such as Session search, usage breakdown, outcomes and Thread stats are included without changing wire routes.

Default tests cover local protocol behavior and independent JAR consumers. Session/Thread SSE and resumable subscriptions are described in [streaming](session-streaming.md); the [live runner](live-testing.md) separately checks actual Session execution.

See [Forward contract updates](forward-parity.md) for automatic Session metadata markers, Credits event accessors, and Memory Store mount pagination.
