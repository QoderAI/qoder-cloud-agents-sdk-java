# Qoder Cloud Agents Java SDK

Java 8 compatible client for the **Forward API**. The repository follows the resource and authentication structure of the QCA Go, Python and TypeScript SDKs, using Maven and Java builders.

**Status: source available; not yet published to Maven Central.** The current version is `0.1.0`. Build from source to use the SDK until a public Maven release is available.

The SDK provides synchronous and asynchronous clients for Models, Templates, Identities, Sessions and Threads, Memory Stores, Environments, Files, Skills, Vaults and Credentials, Schedules, Batches, Channels and Pairing, Usage, Service Account Tokens and Drive. Shared support includes PAT/SA authentication, timeouts, retries, pagination, typed errors and request IDs. Session/Thread SSE supports explicit resumption; Realtime supports text, PCM audio and History. The [API coverage](docs/forward-api-coverage.md) records the 123-operation baseline and its exclusions. Managed APIs are outside this project's scope.

See the [examples](examples/README.md) for authentication and resource workflows, and the [maintenance guide](docs/maintaining.md) for contributor checks and version updates.

## Maven dependency

After installing the SDK locally or publishing it to your configured Maven repository, use:

```xml
<dependency>
  <groupId>com.qoder.sdk</groupId>
  <artifactId>qoder-cloud-agents-sdk</artifactId>
  <version>0.1.0</version>
</dependency>
```

The Maven coordinates do not change Java imports, which remain under `com.qoder.cloudagents`.

## Local build

```sh
./mvnw clean install
./mvnw -f examples/pom.xml verify
python3 .github/scripts/check_package.py
```

Requires JDK 8 or later. JDK 17 is recommended for maintenance builds; compilation targets Java 8. The independent examples project consumes the installed JAR. See [CONTRIBUTING.md](CONTRIBUTING.md) for checks and repository organization; CI checks Java 8, 11, 17, 21 and 25.

## PAT

```java
import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.forward.Template;

try (ForwardClient client = ForwardClient.builder()
        .baseUrl("https://api.qoder.com/api/v1/forward")
        .pat(System.getenv("QODER_PAT"))
        .build()) {
    Template template = client.templates().retrieve("tmpl_example");
    System.out.println(template.id());
    System.out.println(template.requestId());
}
```

The default BaseURL is Global. For CN, use `https://api.qoder.com.cn/api/v1/forward`. `QODER_FORWARD_BASE_URL` provides the default when `.baseUrl(...)` is omitted.

With no explicit credentials, PAT mode reads `QODER_ACCESS_TOKEN`, then `QODER_PAT`. Explicit `.pat(...)`, `.accessToken(...)` or `.credential(...)` takes precedence over environment tokens; configure only one explicit source.

## Service Account

```java
import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.auth.Region;
import com.qoder.cloudagents.auth.ServiceAccountCredential;

try (ServiceAccountCredential credential = ServiceAccountCredential.builder()
        .saKey(System.getenv("QODER_SA_KEY"))
        .region(Region.GLOBAL)
        .scopes("qca.access", "forward.access")
        .build();
     ForwardClient client = ForwardClient.builder().credential(credential).build()) {
    System.out.println(client.models().list().data().size());
}
```

The credential lazily exchanges the SA Key for a platform SAT, caches and refreshes the SAT before expiry, and shares concurrent refreshes. Business requests use the SAT; the Key only goes to the fixed regional exchange endpoint. Set both `Region.CN` and the CN BaseURL for CN requests. No SA credential is inferred merely because `QODER_SA_KEY` exists.

The caller owns the credential. Keep it open until all clients using it are closed. Closing a client does not close its credential.

For a SAT managed by your application:

```java
ForwardClient client = ForwardClient.builder()
    .authMode(com.qoder.cloudagents.auth.AuthMode.SA)
    .accessToken(System.getenv("QODER_ACCESS_TOKEN"))
    .build();
```

Forward SAT management uses explicit per-call SA Keys through `serviceAccountTokens()` and `identities().serviceAccountTokens()`; see the [usage guide](docs/usage-sat-drive.md). A client without a business credential can call those methods; ordinary requests fail locally before network access until a business credential is configured.

Close the client after use. A static SAT is not automatically renewed. SA mode does not fall back to `QODER_PAT`. For application-managed rotation, implement `Credential.getToken()` returning a `CompletableFuture<String>`; it is called before each attempt and must be thread-safe and return promptly. Cancelling a request does not cancel a shared credential future.

## Asynchronous calls and cancellation

```java
java.util.concurrent.CompletableFuture<com.qoder.cloudagents.forward.Template> pending =
    client.async().templates().retrieve("tmpl_example");
pending.cancel(true); // when the caller no longer needs the result
```

Cancellation propagates to the HTTP call and retry timer. The returned future follows Java's `CancellationException` contract. Interrupting a synchronous call cancels its request, preserves the interrupt flag and raises `ApiUserAbortException`.

`client.async()` shares its parent's resources; closing the view is a no-op. Closing the parent cancels active requests. `ForwardClient.builder().buildAsync()` creates an independently owned, closeable async client. Keep the owning client open until futures finish. Additional futures created with application-side `thenApply(...)` follow normal Java cancellation behavior; cancel the SDK-returned future to abort its request.

## Request options and errors

```java
com.qoder.cloudagents.core.RequestOptions options =
    com.qoder.cloudagents.core.RequestOptions.builder()
        .timeout(java.time.Duration.ofSeconds(30))
        .totalTimeout(java.time.Duration.ofSeconds(60))
        .maxRetries(1)
        .build();
com.qoder.cloudagents.core.ApiResponse<com.qoder.cloudagents.forward.Template> response =
    client.templates().withResponse().retrieve("tmpl_example", options);
```

- Default connection timeout: 5 seconds. Default response-header wait and read/write idle timeouts: 10 minutes. `.timeout(...)` overrides the latter limits for one attempt. Optional `.totalTimeout(...)` covers credentials, all attempts, backoff and body reads. Timeouts must be positive. SSE disables read-idle timing after accepted headers; an optional subscription-wide total deadline remains active, including reconnects. See [SSE lifecycle](docs/session-streaming.md).
- Default maximum retries: 2, giving at most 3 attempts. The shared engine follows QCA safety rules: 409 is never retried; non-idempotent writes only qualify for 429 retries; non-repeatable bodies are not retried. Server retry hints remain subject to these checks.
- `ApiException` covers request failures. `ApiStatusException` exposes HTTP status, request ID, headers and parsed error body. Connection, timeout, cancellation and response parsing failures have corresponding subclasses. SA issuer failures use `ServiceAccountExchangeException`, exposing status/request ID without retaining the issuer body. Failed SA exchange does not switch to PAT.
- Models provide `requestId()`, `field(name)` for unknown fields and `rawJson()`. Unknown fields and numeric precision are preserved; invalid typed field access raises `ResponseValidationException`. Request metadata is separate from the JSON body.
- Template list parameters support `limit`, `status`, `afterId` or `beforeId`. Use `page.data()` for one page, `getNextPage()` / `getNextPageAsync()` for manual pagination, or `for (Template template : page)` for lazy synchronous iteration. Server order is retained; filters and request options follow subsequent pages. Check `hasNextPage()` before fetching; an exhausted page or repeated cursor raises `PaginationException`.

## Template configuration

```java
import com.qoder.cloudagents.forward.TemplateCreateParams;
import com.qoder.cloudagents.forward.TemplateUpdateParams;
import com.qoder.cloudagents.core.RequestOptions;

Template template = client.templates().create(
    TemplateCreateParams.builder()
        .name("Support assistant")
        .modelId("auto")
        .environmentId("env_example")
        .build(),
    RequestOptions.builder().idempotencyKey(java.util.UUID.randomUUID().toString()).build());

client.templates().update(template.id(), TemplateUpdateParams.builder()
    .description("Updated description")
    .multiagent(null) // explicitly clear the multi-agent configuration
    .build());
```

An unset builder field is omitted. A setter with `null` writes JSON null; empty collections write `[]` or `{}`. The server decides which fields accept null. `omit("wire_name")` removes a previously set field. Requests and responses preserve unknown JSON fields; raw JSON can contain secrets and should not be logged. See [Template operations and configuration](docs/templates.md) and the [lifecycle example](examples/README.md).

## Documentation

- [Runnable examples and shared PAT/SA setup](examples/README.md)
- [Template operations, configuration and pagination](docs/templates.md)
- [Identity operations and configuration overrides](docs/identities.md)
- [Session HTTP operations, events, threads and Memory Store mounts](docs/sessions.md)
- [Session/Thread SSE, cancellation and resumable streaming](docs/session-streaming.md)
- [Memory Stores, content updates, versions and lifecycle rules](docs/memory-stores.md)
- [Environments, multipart upload and streaming file download](docs/environments-files.md)
- [Skills, versions, Vaults and Credentials](docs/skills-vaults.md)
- [Schedules, Runs and Batches](docs/schedules-batches.md)
- [Channels, Pairing and QR authorization](docs/channels.md)
- [Usage, Forward SAT management and Drive](docs/usage-sat-drive.md)
- [Forward API coverage](docs/forward-api-coverage.md)
- [Realtime text, PCM audio and History](docs/realtime.md)
- [Opt-in live testing](docs/live-testing.md)
- [Maintenance and release setup](docs/maintaining.md)
- [Changelog](CHANGELOG.md)

`./mvnw verify` also builds source and Javadoc JARs. API documentation is generated under `target/reports/apidocs/`; packages under `core.internal` are implementation details.
