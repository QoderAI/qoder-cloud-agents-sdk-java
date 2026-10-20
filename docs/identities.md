# Identity and configuration

The Identity resource currently exposes eleven operations, its Config child exposes four, and its Memory Store child exposes three. All use the client's shared PAT/SA authentication, request options and retry policy. See [Memory Store mounts](sessions.md#memory-store-mounts) for list/mount/detach. Identity SAT issuance is a separate planned addition.

## Resource methods

| Resource | Methods | Result |
| --- | --- | --- |
| `client.identities()` | `create(params)`, `retrieve(id)`, `update(id, params)` | `Identity` |
| | `list()`, `list(params)` | `IdentityListResponse` cursor page |
| | `ensureAdmin()` | Current administrator `Identity` |
| | `stats()` | `IdentityStats` |
| | `listTemplates(id)` | `IdentityListTemplatesResponse` with a single `data()` list |
| | `enable(id)`, `disable(id)` | Updated `Identity` |
| | `clear(id, params)` | `IdentityClearResponse` with status and counters |
| | `delete(id)` | `DeletedIdentity` with `id()` and `deleted()` |
| `client.identities().configs()` | `list(identityId)`, `list(identityId, params)` | `IdentityConfigListResponse` cursor page |
| | `retrieve(identityId, templateId)` | Stored `IdentityConfig` |
| | `upsert(identityId, templateId, params)` | Updated `IdentityConfig` |
| | `getEffective(identityId, templateId)` | Compiled `EffectiveConfig` |

Each method also accepts `RequestOptions` as its final argument. `client.async()` exposes the same methods with `CompletableFuture` results. `withResponse()` returns HTTP status, headers and request ID in addition to the typed data.

The API's historical `/identities/{id}/agents` route remains unchanged on the wire, but is called `listTemplates` in Java. Similarly, `IdentityStats.totalTemplates()` reads the wire field `total_agents`. Associated Template listing is not a cursor endpoint.

## Create and list

```java
import com.qoder.cloudagents.forward.*;

Identity identity = client.identities().create(
    IdentityCreateParams.builder()
        .externalId("customer-example")
        .name("Example customer")
        .enabled(true)
        .build());

IdentityListResponse page = client.identities().list(
    IdentityListParams.builder().enabled(true).limit(20).build());
for (Identity item : page) {
    System.out.println(item.id());
}
```

The iterator fetches subsequent pages lazily and preserves server order. Use `page.data()` for only the current page, or check `hasNextPage()` before `getNextPage()` / `getNextPageAsync()`. The shared cursor implementation preserves nested paths, filters, request options and pagination direction; repeated cursors raise `PaginationException`.

Identity filters include `externalId`, `identityIds`, `search` and `enabled`. `identityIds(List<String>)` uses the endpoint's comma-separated query representation. Identity `limit` accepts positive integers; the service applies its documented maximum of 100. Config list parameters support `templateId`, `status` and `limit` from 1 to 100. Both lists accept `afterId` or `beforeId`, never both.

## Per-Identity configuration

```java
IdentityConfig config = client.identities().configs().upsert(
    identity.id(), "tmpl_example",
    IdentityConfigUpsertParams.builder()
        .identityConfig(IdentityConfigSpec.builder()
            .system(SystemOverride.builder()
                .mode("append")
                .content("Prefer concise answers.")
                .build())
            .model(ModelOverride.builder().effort("high").build())
            .build())
        .build());

EffectiveConfig effective =
    client.identities().configs().getEffective(identity.id(), "tmpl_example");
System.out.println(effective.effectiveHash());
```

`IdentityConfigSpec` contains overrides; it is not a Template replacement or an already compiled runtime config. `retrieve` returns the stored override. `getEffective` returns the server's merged result, with separately typed agent and session settings. The SDK does not merge them locally or inject defaults.

| Override | Java representation |
| --- | --- |
| System prompt | `SystemOverride`: mode and content |
| Model | `ModelOverride.of("model-id")` or a partial builder; effort/speed can be changed without supplying an ID |
| Tools | `IdentityTools.overrides(map)` for per-name overrides, or `IdentityTools.replacement(list)` for full replacement |
| Managed tools | `Map<String, ManagedToolOverride>`, with each entry providing `enabled`; this differs from Template's enabled-tools list |
| MCP / Skills / Toolsets | Sparse maps of `MCPServerOverride`, `SkillOverride`, `ToolsetOverride` |
| Files / Vaults | Resource-ID maps of `ResourceBinding` |
| GitHub repositories | Request maps of `GitHubRepositoryConfig`; response maps of `GitHubRepository` |
| Environment variables | Name-keyed `EnvironmentVariableOverride` with `op` set/unset and optional value |
| Agent metadata | `Map<String, JsonValue>` |

No typed environment or environment-ID override is provided: the service does not permit changing the Template's environment through Identity config.

An unset field is omitted. Calling a setter with null emits JSON null, and empty collections remain empty. Null entries in sparse override maps are preserved to restore inheritance where the API supports it. For example, to restore a managed tool's inherited setting:

```java
IdentityConfigSpec restore = IdentityConfigSpec.builder()
    .managedToolConfig(java.util.Collections.singletonMap("list_drive_entries", null))
    .build();
```

Whether null is accepted depends on the particular server field; the SDK does not treat every null as a deletion. `omit("wire_name")` removes a previously set builder field. Config upsert requires `identityConfig`, but an empty object is allowed. Config top-level metadata uses replacement semantics, while Identity update metadata uses the endpoint's merge semantics; both are performed by the server.

Models preserve unknown fields and exact JSON numbers. `field(...)` and `rawJson()` expose those fields explicitly. Avoid logging raw configuration: environment variables and write-only repository credentials can be sensitive.

## Clear and delete

`clear` affects an Identity's associated resources and sessions but retains the Identity itself. It can have partial effects if cleanup fails. `delete` deletes the Identity; it returns a JSON confirmation rather than an empty response. Do not substitute one operation for the other.

The [Identity configuration example](../examples/README.md#identity-configuration) creates a new test Identity and uses clear-before-delete cleanup. If clear fails or does not report `completed`, it leaves the Identity available for recovery and preserves both the original and cleanup failures. It never runs this cleanup against a caller-supplied business Identity.

## Contract and verification

Routes and baseline fields were checked against the three [pinned SDK revisions](forward-api-coverage.md).

Default tests cover request contracts, pagination, errors and packaged consumers. See [live testing](live-testing.md) for opt-in resource and execution checks against a selected environment.
