# Forward API additions (unreleased)

This guide describes the development SDK after `0.1.0`, checked against the official Forward documentation on 2026-10-28. These additions are not available from the published `0.1.0` JAR. Both `ForwardClient` and `AsyncForwardClient` expose the same resources, including `withResponse()` for status, headers and request IDs. Managed remains a separate future scope.

## Search resources

Environment, File, Skill and Vault services provide `search()` and `search(params, options)`. Search uses POST with a JSON object, including `{}` when no filters are specified. The SDK adds `x-qoder-beta: search-2026-08-31`, preserving other effective Beta selectors.

```java
FileListResponse page = client.files().search(FileSearchParams.builder()
    .name("report")
    .metadata(java.util.Collections.singletonMap("project", "demo"))
    .limit(20)
    .build());
for (FileMetadata file : page) { // Lazy iteration across pages.
    System.out.println(file.id());
}
```

Filters stay in the body when paging. The SDK replays `next_page` unchanged and detects repeated cursors. Request options, authentication and the selected Identity scope are retained; asynchronous continuation can be cancelled.

| Service | Additional typed filters |
| --- | --- |
| `environments()` | `name`, `includeArchived`, `createdAtGte`, `createdAtLte` (RFC 3339 strings) |
| `files()` | `name`, `scopeId` |
| `skills()` | `source`, `displayTitle` |
| `vaults()` | `name`, `includeArchived` |

All four support `metadata`, `limit` and `page`. The Environment timestamp keys on the wire are literally `created_at[gte]` and `created_at[lte]`. Search does not use the GET list method's query filters.

For PAT access to an Identity-owned resource, set `params.identityId(id)`; it goes into the **query**, never the JSON body. A per-call `RequestOptions.query("identity_id", id)` overrides this value. For any SAT, omit the selector and use the token's ownership scope. The service enforces permissions; the SDK does not infer ownership or change authentication.

Official contracts: [Environment](https://docs.qoder.com/cloud-agents/api/forward/environments/search), [File](https://docs.qoder.com/cloud-agents/api/forward/files/search), [Skill](https://docs.qoder.com/cloud-agents/api/forward/skills/search), [Vault](https://docs.qoder.com/cloud-agents/api/forward/vaults/search).

## Dreams

`client.dreams()` exposes `create`, `list`, `retrieve`, `cancel` and `archive`. Creation accepts PAT or administrator SAT, not Identity SAT.

```java
Dream dream = client.dreams().create(DreamCreateParams.builder()
    .inputs(java.util.Collections.singletonList(DreamInput.builder()
        .type("memory_store").memoryStoreId("memstore_example").build()))
    .build());
Dream current = client.dreams().retrieve(dream.id());
```

Inputs contain exactly one primary `memory_store` or `identity_template` input; additional `sessions` inputs can focus the consolidation. A Memory Store input creates a new output Store and does not replace the original. An Identity/Template input may replace that pair's default Store when it completes. Creation executes work and can consume credits.

Omitting `model`, passing null, or using `DreamModelConfig.of("")` leaves the documented automatic model routing to the server. Use `DreamModelConfig.of("auto")` or a structured builder with `id`, `effort` and `contextWindow`; Dream models do not expose Template's `speed`. Responses expose structured model configuration, inputs, output Store IDs and touched paths, token usage, errors and lifecycle timestamps. Canceling execution and archiving a record are separate operations.

See [Dream creation](https://docs.qoder.com/cloud-agents/api/forward/dreams/create) and [schemas](https://docs.qoder.com/cloud-agents/api/forward/dreams/schemas).

## Pairing management

`channelPairings().list(...)`, `.retrieve(pairingId)` and `.update(pairingId, params)` supplement the existing create/delete methods. Lists support Channel, status, target type/external ID, Identity/Template, limit and bidirectional cursors. Target filters use `user` / `group`; response scope types use `direct` / `room`.

```java
ChannelPairing pairing = client.channelPairings().update("pair_example",
    ChannelPairingUpdateParams.builder().templateId("tmpl_new").build());
```

Update changes the Identity and/or Template of an **active** binding. At least one nonblank field is required; omitted fields stay unchanged and null is not supported. Use existing `create` with a code or pairing ID to complete a pending pairing. Updating a binding changes how subsequent channel messages execute; default examples only list existing bindings.

See [list](https://docs.qoder.com/cloud-agents/api/forward/channels/list-channel-pairings), [retrieve](https://docs.qoder.com/cloud-agents/api/forward/channels/get-channel-pairing), and [update](https://docs.qoder.com/cloud-agents/api/forward/channels/update-channel-pairing).

## Decisions (Beta)

`decisions().create(...)` evaluates questions synchronously and stores the result; `retrieve` and cursor-based `list` read records. Supply exactly one of a nonempty `state` object or a `sessionId`, and at least one question. `state.session_context` is reserved.

```java
Decision result = client.decisions().create(DecisionCreateParams.builder()
    .state(java.util.Collections.singletonMap("resolved", JsonValue.of(true)))
    .questions(java.util.Collections.singletonList(DecisionQuestion.builder()
        .type("choice").instructions("Is resolved true in the supplied state?")
        .choices(java.util.Arrays.asList(
            DecisionChoice.builder().value(true).build(),
            DecisionChoice.builder().value(false).build()))
        .build()))
    .build(), RequestOptions.builder().idempotencyKey("one-logical-decision").build());
```

Choice values preserve their string or boolean JSON type. Score levels and answer scores use `BigDecimal`. Response context, errors and unknown fields remain readable; optional fields may be absent on running or failed records. `completed` means judgment finished, not that the business objective succeeded; inspect the answers.

The service binds an idempotency key to the exact request bytes. SDK retries retain those bytes. Reuse the original key and immutable parameters when reconciling an uncertain result; use a new key for a new judgment. HTTP 409 conflicts are not automatically retried. Without a key, a failed POST is not automatically retried on connection/5xx errors. See [official Decision semantics](https://docs.qoder.com/cloud-agents/api/forward/decisions/create).

## Existing contract updates

- Outcome text rubrics use `OutcomeRubric.content(...)` / `content()`, serialized as `{"type":"text","content":"..."}`. The deprecated `text(...)` / `text()` methods delegate to content; raw `fromJson` remains literal, so migrate raw JSON's old `text` key yourself. File rubrics continue to use `fileId`. The server generates the Outcome ID.
- Identity create/update/response expose `autoMemoryStore`. Create omission/null means the server default `true`; update omission/null leaves the value unchanged. `false` does not delete existing stores or mounts.
- Independent Memory Store mounting accepts `access`. Omission retains the permission on an existing mount and defaults a new mount to `read_only`. Mount lists support `IdentityMemoryStoreListParams.limit/afterId/beforeId` and lazy iteration / asynchronous paging. The original boxed `Boolean hasMore()` signature is preserved. The default Store remains first, followed by server-ordered explicit mounts.
- Schedule create/update/read expose `resources` using `SessionResourceSpec`. Update omission preserves the configuration; null or `[]` clears it according to the service contract. Resources are separate from inherited Template/Identity files and do not change `initial_events_version`.
- `ScheduleRunRetrieveParams.includeInitialEvents(true)` requests the bound input snapshot. `ScheduleRun.initialEvents()` is null when absent and an empty list when the server explicitly returns `[]`; `field("initial_events")` distinguishes missing from explicit null.
- `SessionEvent.modelCredits/sandboxRuntimeCredits/totalCredits` read top-level cumulative `session.usage` credits; `modelUsageCredits()` reads incremental model request credits and `usageCredits()` reads compaction credits. These are exact decimals. Replace cumulative snapshots; do not add compaction credits to the total again. Existing raw Map accessors remain available.

## Session creation metadata

Typed `sessions().create(...)` automatically merges three string-valued metadata fields:

| Key | Value |
| --- | --- |
| `__qca_sdk_created` | `"true"` |
| `__qca_sdk_language` | `"java"` |
| `__qca_sdk_version` | Version from the actual Java SDK JAR |

This applies to synchronous, asynchronous and `withResponse()` creation. Missing or null metadata becomes an object containing the markers. An already-present marker with the same string value is allowed; a different value or type throws `ConfigurationException` before sending HTTP. Other keys, including unrecognized `__qca_sdk_` keys, are preserved and caller maps are not modified. These three entries count toward the service metadata limit. Metadata updates, generic `request(...)` calls, Realtime creation and Forward's downstream Managed calls are not rewritten.

## Verification and executable example

[ForwardCapabilitiesExample](../examples/src/main/java/com/qoder/cloudagents/examples/ForwardCapabilitiesExample.java) consumes the installed JAR. Its default `search` mode reads one page of files; `pairings` only reads. `decision` and `dream` require both `QODER_ALLOW_WRITE=true` and `QODER_ALLOW_EXECUTION=true`. Dream also requires `QODER_MEMORY_STORE_ID`; its task/output are retained for inspection. For SAT searches, leave `QODER_IDENTITY_ID` unset.

```sh
./mvnw -B -ntp -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.ForwardCapabilitiesExample \
  -Dexec.args='search report'
```

Offline request fixtures cover all 15 new operations through sync/async and response wrappers, plus pagination, metadata conflicts, exact numeric values and cancellation. The independent consumer tests verify the packaged API surface. Real CN/Global PAT/SA verification and QA-package acceptance remain separate gates; offline mocks do not establish real channel pairing, Dream execution or service availability. See [live testing](live-testing.md).
