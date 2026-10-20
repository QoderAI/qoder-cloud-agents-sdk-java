# Template API

Implemented: list, retrieve, create, update, archive and clone. These six operations share the same request engine and credential handling in synchronous and asynchronous clients.

## Methods

| Operation | Synchronous | Asynchronous |
| --- | --- | --- |
| List | `client.templates().list(params, options)` | `client.async().templates().list(params, options)` |
| Retrieve | `retrieve(id, options)` | `retrieve(id, options)` |
| Create | `create(params, options)` | `create(params, options)` |
| Update | `update(id, params, options)` | `update(id, params, options)` |
| Archive | `archive(id, options)` | `archive(id, options)` |
| Clone | `clone(id, params, options)` | `clone(id, params, options)` |

Every method has an overload without `RequestOptions`. Async methods return `CompletableFuture<T>`. Use `templates().withResponse()` for status, headers and request ID alongside the typed result. No Template delete operation is exposed.

## Typed configuration

```java
import com.qoder.cloudagents.forward.*;
import java.util.Arrays;
import java.util.Collections;

TemplateCreateParams params = TemplateCreateParams.builder()
    .name("Support assistant")
    .environmentId("env_example")
    .model(ModelConfig.builder()
        .id("ultimate")
        .effort("high")
        .speed("standard")
        .contextWindow(400000L)
        .build())
    .maxToolRounds(12L)
    .tools(Collections.singletonList(Tool.builder()
        .type("agent_toolset_20260401")
        .enabledTools(Arrays.asList("Read", "Bash"))
        .build()))
    .managedToolConfig(ManagedToolConfig.builder()
        .enabledTools(Collections.singletonList("create_forward_schedule"))
        .build())
    .environmentVariables(EnvironmentVariables.of(
        Collections.singletonMap("MODE", "support")))
    .build();
```

Use values supported by the selected environment and model; the SDK does not infer platform defaults or validate account-specific limits. Use `modelId("auto")` or `model(ModelConfig.of("auto"))` to send a model string. Structured models retain their object form. `EnvironmentVariables.fromText(...)` retains the server-supported string form; `of(Map<String, String>)` sends an object.

| Template field | Java type |
| --- | --- |
| `model` | `ModelConfig`: string or object with id, effort, speed, context window |
| `tools` | `List<Tool>`, with `ToolConfig`, `PermissionPolicy` and JSON Schema |
| `managed_tool_config` | `ManagedToolConfig` |
| `mcp_servers` | `List<MCPServer>` |
| `skills` | `List<SkillBinding>` |
| `multiagent` | `MultiagentConfig`, `MultiagentEntry` (including Advisor model) |
| `vaults`, `files` | `Map<String, ResourceBinding>` |
| `github_repositories` | Request: `GitHubRepositoryConfig`; response: `GitHubRepository` |
| `environment_variables` | `EnvironmentVariables`: object or string |
| `metadata`, unknown fields | `JsonValue`, preserving arbitrary JSON and number precision |

GitHub `authorizationToken(...)` is a write-only request field. The typed response does not offer this getter. Object `toString()` omits content, but `rawJson()` and `toJsonValue().toJson()` deliberately expose the complete payload, which may contain secrets. Do not log raw configuration.

List summaries can omit detail fields. Optional typed getters return null for missing or null fields; `field("wire_name").isMissing()` and `isNull()` distinguish them. Unknown values are retained instead of rejected as closed enums.

## Updates: omitted, null and empty

```java
TemplateUpdateParams update = TemplateUpdateParams.builder()
    .description("Revised instructions")
    .multiagent(null)
    .maxToolRounds(null)
    .tools(Collections.emptyList())
    .files(Collections.emptyMap())
    .build();
client.templates().update("tmpl_example", update);
```

- Unset fields are absent from JSON, so the server preserves existing values.
- Null setters send JSON null. For example, clearing `multiagent` disables it; clearing `max_tool_rounds` restores the platform default.
- Empty arrays and objects are serialized as empty containers, not omitted or converted to null.
- `omit("wire_name")` removes an earlier builder value.
- Builders snapshot supplied collections. Changing a list, map or builder later cannot mutate an already built request.

The SDK preserves these wire distinctions; each field's clearing/merge semantics remain server-defined. In particular, metadata patch semantics are handled by the service.

## Headers and safe retries

```java
import com.qoder.cloudagents.core.RequestOptions;

RequestOptions options = RequestOptions.builder()
    .idempotencyKey(java.util.UUID.randomUUID().toString())
    .build();
Template created = client.templates().create(params, options);
```

Use one idempotency key for attempts of the **same operation and payload**, and a new key for each different operation. Header options are not JSON fields. Browser-capable configurations can explicitly use `header("X-Qoder-Beta", "browser-use-2026-07-14")`; the SDK does not enable Beta capabilities automatically.

Write operations use the shared safety rules: no implicit retry of connection/5xx failures without an idempotency key; 429 follows the configured retry policy; 409 is not retried. Archive sends an empty POST body.

## Pagination

```java
TemplateListResponse page = client.templates().list(
    TemplateListParams.builder().status("active").limit(20).build());

// Iterate current page only, without extra requests:
for (Template template : page.data()) {
    System.out.println(template.id());
}

// Or iterate all pages lazily; keep the owning client open:
for (Template template : page) {
    System.out.println(template.id());
}
```

`page.iterPages()` lazily iterates page objects. Iteration retains server order, filter/query overrides, headers, retry limits and timeouts; authentication is resolved for every request. A per-request total timeout applies separately to each page, not the whole iteration. Stopping iteration does not prefetch more pages.

Manual pagination:

```java
if (page.hasNextPage()) {
    TemplateListResponse next = page.getNextPage();
}
if (page.hasNextPage()) {
    java.util.concurrent.CompletableFuture<TemplateListResponse> pending =
        page.getNextPageAsync();
    // Cancel this returned future when the result is no longer needed:
    // pending.cancel(true);
}
```

Forward pagination uses `last_id` as the next `after_id`; a request using `before_id` continues with `first_id`. These two request cursors are mutually exclusive, including request-option overrides. A false `has_more`, empty data or absent cursor ends iteration. Calling next on an exhausted page raises `PaginationException`, as does a repeated cursor that would cause a loop. Async consumers use explicit page futures; no whole-result buffering or async stream API is provided.

## Contract sources and validation boundary

Routes and baseline fields follow the pinned Go/Python/TypeScript SDK commits in [coverage](forward-api-coverage.md). Template parameters also expose `speed`, `max_tool_rounds` and Advisor configuration. Java exposes these fields without imposing new defaults.

Default verification uses mock HTTP services and an independently installed JAR. See [live testing](live-testing.md) to exercise the resource lifecycle against a selected environment.
