# Client configuration

Shared configuration for the synchronous and asynchronous Forward clients. See the [README](../README.md) for installation and quick starts.

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

Forward SAT management uses explicit per-call SA Keys through `serviceAccountTokens()` and `identities().serviceAccountTokens()`; see the [usage guide](usage-sat-drive.md). A client without a business credential can call those methods; ordinary requests fail locally before network access until a business credential is configured.

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

- Default connection timeout: 5 seconds. Default response-header wait and read/write idle timeouts: 10 minutes. `.timeout(...)` overrides the latter limits for one attempt. Optional `.totalTimeout(...)` covers credentials, all attempts, backoff and body reads. Timeouts must be positive. SSE disables read-idle timing after accepted headers; an optional subscription-wide total deadline remains active, including reconnects. See [SSE lifecycle](session-streaming.md).
- Default maximum retries: 2, giving at most 3 attempts. The shared engine follows QCA safety rules: 409 is never retried; non-idempotent writes only qualify for 429 retries; non-repeatable bodies are not retried. Server retry hints remain subject to these checks.
- `ApiException` covers request failures. `ApiStatusException` exposes HTTP status, request ID, headers and parsed error body. Connection, timeout, cancellation and response parsing failures have corresponding subclasses. SA issuer failures use `ServiceAccountExchangeException`, exposing status/request ID without retaining the issuer body. Failed SA exchange does not switch to PAT.
- Models provide `requestId()`, `field(name)` for unknown fields and `rawJson()`. Unknown fields and numeric precision are preserved; invalid typed field access raises `ResponseValidationException`. Request metadata is separate from the JSON body.
- Template list parameters support `limit`, `status`, `afterId` or `beforeId`. Use `page.data()` for one page, `getNextPage()` / `getNextPageAsync()` for manual pagination, or `for (Template template : page)` for lazy synchronous iteration. Server order is retained; filters and request options follow subsequent pages. Check `hasNextPage()` before fetching; an exhausted page or repeated cursor raises `PaginationException`.

## Client extensions

The following client extensions are included in `0.2.0`.

### Derived clients

```java
try (ForwardClient slow = client.withOptions(options -> options
        .timeout(java.time.Duration.ofSeconds(60))
        .maxRetries(1)
        .defaultHeader("X-Trace-Source", "background-job"))) {
    slow.models().list();
}
```

`withOptions` retains the resolved BaseURL, authentication, headers, interceptors and custom transport unless changed. It does not reread environment credentials. Header setters replace matching names case-insensitively; interceptors append in registration order. Use `clearDefaultHeaders()` or `clearInterceptors()` to replace those collections. Per-request options still take precedence. `client.async().withOptions(...)` creates an independently owned async client.

Copies have separate default connection pools, retry timers and in-flight request lifecycles. Closing either client does not close the other. Custom transports and credentials are shared by reference and remain caller-owned; keep them open until all clients and streams finish. Changing `.pat(...)`, `.accessToken(...)` or `.credential(...)` on a copy replaces its inherited credential source. For a static SAT use `.authMode(AuthMode.SA).accessToken(token)`.

### Additional JSON endpoints

```java
import com.qoder.cloudagents.core.ApiRequest;
import com.qoder.cloudagents.core.ApiResponse;
import com.qoder.cloudagents.core.JsonValue;

ApiResponse<JsonValue> response = client.request(
    ApiRequest.builder("GET", "models").build(),
    RequestOptions.builder().totalTimeout(java.time.Duration.ofSeconds(30)).build());
System.out.println(response.requestId());
```

Pass unencoded path **segments**, for example `ApiRequest.builder("POST", "sessions", sessionId, "new-action")`; the SDK encodes each segment beneath its configured BaseURL. Absolute URLs, slash-separated paths, traversal and embedded queries/fragments are rejected. Use `RequestOptions.query(...)` and `header(...)` for query parameters and headers, and `.body(JsonValue)` for a JSON body. GET/HEAD cannot have a body. HEAD and 204/205 responses return JSON null.

The return type includes JSON, status and response headers; `client.async().request(...)` returns a cancellable `CompletableFuture<ApiResponse<JsonValue>>`. HTTP errors retain the existing exception mapping. This entry point uses business PAT/SAT credentials and the shared retry rules; it does not add resource-specific validation. Prefer typed resource methods where available, particularly SA Key token management and credential rotation. Use the existing resource APIs for multipart upload, downloads and SSE.

### HTTP interceptors and custom transport

```java
try (ForwardClient traced = client.withOptions(options -> options.interceptor((request, chain) -> {
    long started = System.nanoTime();
    return chain.proceed(request.withHeader("X-Trace-Source", "example"))
        .whenComplete((response, error) -> {
            long elapsedMs = (System.nanoTime() - started) / 1_000_000;
            System.out.println("http_elapsed_ms=" + elapsedMs);
        });
}))) {
    traced.models().list();
}
```

`HttpInterceptor` runs after authentication on **every attempt**, including SSE connection/reconnection and explicit SA Key token-management requests. It may inspect metadata, add ordinary headers or return a response, but may call `chain.proceed(...)` only once. It cannot change routing, method, body, timeout, Authorization or idempotency. Do not log headers, bodies or full URLs, or read a streaming body just to log it. Interceptor callbacks must return promptly. Their completion future measures response-header arrival, not SSE/download completion.

Implement `com.qoder.cloudagents.core.http.HttpTransport` and pass it to `.transport(...)` when an application needs its own HTTP library, proxy, TLS settings or connection pool. The SDK passes an immutable `HttpRequest` with URI, headers, connect/read/write timeouts and an optional streaming `Body`. Use `Body.contentType()`, `contentLength()` and `writeTo(OutputStream)` when sending uploads; do not buffer all uploads or close the destination from `writeTo`.

The transport must return a `CompletableFuture<HttpResponse>` promptly, perform one attempt without automatic redirects/retries/authentication, honor timeouts, and abort its I/O when its future is cancelled. Return an unconsumed `InputStream` in `HttpResponse`; the SDK closes it on completion, cancellation or failure. Transport implementations must release responses that lose a race with cancellation. Read timeout is zero for an accepted SSE stream; SDK header and optional total deadlines remain active. Cancelling an SDK request also cancels downstream transport futures even when an interceptor has wrapped them in `thenApply`/`whenComplete`.

| Operation | Custom transport | API interceptors |
| --- | --- | --- |
| JSON, multipart, authenticated downloads | Yes | Yes |
| Session/Thread SSE, including resumption | Yes | Yes, per connection |
| File signed-URL storage download | Yes | No; no API headers/credentials copied |
| Realtime create/History HTTP | Yes | Yes |
| Realtime WebSocket | No | No |
| `ServiceAccountCredential` platform exchange | No | No |

The default HTTP implementation remains OkHttp. Injected transports and interceptors do not change the SDK's retry safety rules or grant additional API permissions.

## Template configuration example

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

An unset builder field is omitted. A setter with `null` writes JSON null; empty collections write `[]` or `{}`. The server decides which fields accept null. `omit("wire_name")` removes a previously set field. Requests and responses preserve unknown JSON fields; raw JSON can contain secrets and should not be logged. See [Template operations and configuration](templates.md) and the [lifecycle example](../examples/README.md).
