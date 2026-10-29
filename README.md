# Qoder Cloud Agents Java SDK

[Changelog](CHANGELOG.md) · [Releases](https://github.com/QoderAI/qoder-cloud-agents-sdk-java/releases) · [Maven Central](https://central.sonatype.com/artifact/com.qoder.sdk/qoder-cloud-agents-sdk)

The Qoder Cloud Agents Java SDK provides typed synchronous and asynchronous clients, with shared authentication, timeouts, retries, pagination, streaming and error handling.

Version **0.2.0** supports both the **Forward API** and the **[Managed API](docs/managed.md)** in one package. Use `ForwardClient` / `AsyncForwardClient` for Forward and `ManagedClient` / `AsyncManagedClient` for Managed.

| Capability | Resources |
| --- | --- |
| Agent configuration | Forward Template, Identity and per-Identity configuration; Managed Agent and versions |
| Conversations | Session, Thread, event history and SSE subscriptions |
| Execution | Forward Schedule, Schedule Run, Batch and Batch Task; Managed Deployment, Run and self-hosted Work |
| Resources | Environment, File, Skill, Vault, Memory Store and resource Search; Forward Drive |
| Channels and notifications | Forward Channel configuration, Pairing management, QR authorization and Webhooks |
| Authentication and usage | PAT, Service Account credentials, Forward SAT issuance/revocation, Model and Usage |
| Realtime (Forward, Beta) | Text/audio WebSocket conversations and persisted History |

See the [Forward](docs/forward-api-coverage.md) and [Managed](docs/managed-api-coverage.md) coverage inventories and resource guides for the supported operations.

The [0.2.0 changelog](CHANGELOG.md) covers Forward Search, Dreams, Decisions, pairing management, contract updates, Session creation metadata, Webhooks, client extensions and 107 Managed operations. See the [Forward migration guide](docs/forward-parity.md) for the rubric field and reserved metadata changes.

- [Installation](#installation)
- [Quick start](#quick-start)
- [PAT and Service Account authentication](#pat-and-service-account-authentication)
- [Conversations and streaming](#conversations-and-streaming)
- [Asynchronous calls and cancellation](#asynchronous-calls-and-cancellation)
- [Pagination](#pagination)
- [Timeouts, retries and errors](#timeouts-retries-and-errors)
- [Realtime](#realtime)
- [Examples and documentation](#examples-and-documentation)
- [Development](#development)

## Installation

Add the Maven dependency; published versions require no local SDK build or additional repository configuration.

```xml
<dependency>
  <groupId>com.qoder.sdk</groupId>
  <artifactId>qoder-cloud-agents-sdk</artifactId>
  <version>0.2.0</version>
</dependency>
```

Requirements:

- Java **8** or later; CI covers Java **8, 11, 17, 21 and 25**.
- A PAT or Service Account credential for the target region, with access to the resources you call.

## Quick start

Set `QODER_PAT` in your environment. This complete Forward program connects to Global and lists the enabled models. For Managed client creation, authentication and resource examples, see the [Managed quick start](docs/managed.md#client-and-authentication).

```java
import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.forward.Model;

public class QuickStart {
    public static void main(String[] args) {
        try (ForwardClient client = ForwardClient.builder()
                .pat(System.getenv("QODER_PAT"))
                .build()) {
            for (Model model : client.models().list().data()) {
                if (model.isEnabled()) {
                    System.out.println(model.id());
                }
            }
        }
    }
}
```

| Region | Forward BaseURL |
| --- | --- |
| Global (default) | `https://api.qoder.com/api/v1/forward` |
| CN | `https://api.qoder.com.cn/api/v1/forward` |

For CN, add `.baseUrl("https://api.qoder.com.cn/api/v1/forward")` to the builder. If omitted, the SDK reads `QODER_FORWARD_BASE_URL`, then uses Global. BaseURLs include the full API root path.

Clients are thread-safe and can be reused. Close the owning client when your application finishes using it. The following snippets assume an open `client`; replace resource IDs with IDs accessible to your credential.

## PAT and Service Account authentication

PAT and SA are parallel authentication modes:

| Mode | Configuration | Token renewal |
| --- | --- | --- |
| PAT | `.pat(token)` | Managed by your application |
| SA Key | `.credential(ServiceAccountCredential)` | SDK exchanges, caches and refreshes the SAT on demand |
| Existing SAT | `.authMode(AuthMode.SA).accessToken(token)` | Managed by your application |

With no explicit credentials, PAT mode reads `QODER_ACCESS_TOKEN`, then `QODER_PAT`. An explicit token or credential takes precedence over environment tokens; configure only one explicit source. The SDK does not load `.env` files or enable SA merely because `QODER_SA_KEY` is set.

To use automatic SA exchange, set `QODER_SA_KEY` and configure a credential:

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

Business requests use the SAT; the SA Key is sent only to the exchange service. For CN, use both `Region.CN` and the CN BaseURL. Keep a shared credential open until all clients using it are closed. Failed SA authentication does not fall back to PAT.

Forward also provides explicit administrator/Identity SAT issuance and revocation through `serviceAccountTokens()` and `identities().serviceAccountTokens()`. These methods take a per-call SA Key and do not retry automatically. See [SAT management](docs/usage-sat-drive.md) and [authentication configuration](docs/client-configuration.md).

## Conversations and streaming

Create a Session using an existing Identity and Template, send a user message, and consume events through SSE:

```java
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import com.qoder.cloudagents.streaming.EventStream;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

Session session = client.sessions().create(
    SessionCreateParams.builder()
        .identityId("idn_example")
        .templateId("tmpl_example")
        .title("SDK quick start")
        .build(),
    RequestOptions.builder().idempotencyKey(UUID.randomUUID().toString()).build());

SessionEventSendResponse sent = client.sessions().events().send(session.id(),
    SessionEventSendParams.builder().events(Collections.singletonList(
        SessionEventParam.builder()
            .type("user.message")
            .content(EventContent.text("Hello!"))
            .build())).build(),
    RequestOptions.builder().idempotencyKey(UUID.randomUUID().toString()).build());

// Start after the accepted input, excluding events from earlier turns.
String inputEventId = sent.data().get(0).id();
try (EventStream<SessionEvent> stream = client.sessions().events().streamEvents(
        session.id(), SessionEventStreamParams.builder().lastEventId(inputEventId).build(),
        RequestOptions.builder().totalTimeout(Duration.ofMinutes(5)).build())) {
    for (SessionEvent event : stream) {
        System.out.println(event.type());
        // Inspect event.content() for message blocks.
        if ("session.error".equals(event.type()) || "session.status_terminated".equals(event.type())) {
            throw new IllegalStateException("Session stopped: " + event.type());
        }
        if ("session.status_idle".equals(event.type())) break;
    }
}
```

Reuse an idempotency key when retrying the same logical write. A successful send acknowledges input, not completed execution. An idle event can mean the turn finished or paused for input; inspect the event's stop reason in your application. The example bounds the subscription to five minutes; closing it does not cancel or archive the Session.

Thread subscriptions use `sessions().threads().events()`. For explicit reconnects with the last delivered SSE ID, use `resumableStream(...)`; user messages are not automatically resent. See [Session operations](docs/sessions.md), [streaming and cancellation](docs/session-streaming.md) and the [runnable streaming example](examples/src/main/java/com/qoder/cloudagents/examples/SessionStreamingExample.java).

## Asynchronous calls and cancellation

The async view shares the client's authentication and transport:

```java
import com.qoder.cloudagents.forward.Template;
import java.util.concurrent.CompletableFuture;

CompletableFuture<Template> pending = client.async().templates().retrieve("tmpl_example");
pending.thenAccept(template -> System.out.println(template.id()));
// When the result is no longer needed: pending.cancel(true);
```

Keep the owning client open until requests complete. Cancel the SDK-returned future to stop its HTTP request and retry timer. `ForwardClient.builder().buildAsync()` creates an independently owned `AsyncForwardClient`. Cancelling a request locally does not invoke `sessions().cancel(...)` on the server. See [async ownership and cancellation](docs/client-configuration.md#asynchronous-calls-and-cancellation).

## Pagination

List responses support one-page access and lazy iteration:

```java
import com.qoder.cloudagents.forward.TemplateListParams;
import com.qoder.cloudagents.forward.TemplateListResponse;

TemplateListResponse page = client.templates().list(TemplateListParams.builder().limit(20).build());
for (com.qoder.cloudagents.forward.Template template : page) {
    System.out.println(template.id()); // Fetches subsequent pages as needed.
}
```

Use `page.data()` for only the current page, or `hasNextPage()` and `getNextPage()` / `getNextPageAsync()` for manual pagination. Filters, request options and server order are preserved. Drive uses `entries` / `next_page_token`; Realtime History has its own `next_before` cursor. See the [resource guides](docs/forward-api-coverage.md#使用指南) for those APIs.

## Timeouts, retries and errors

Pass `RequestOptions` as the last argument to customize a request:

```java
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.errors.ApiStatusException;
import java.time.Duration;

try {
    client.templates().retrieve("tmpl_example", RequestOptions.builder()
        .timeout(Duration.ofSeconds(30))
        .totalTimeout(Duration.ofSeconds(60))
        .maxRetries(1)
        .build());
} catch (ApiStatusException error) {
    System.err.println("HTTP " + error.statusCode() + ", request ID: " + error.requestId());
}
```

- **Timeouts:** the connection timeout defaults to 5 seconds; response-header and read/write idle timeouts default to 10 minutes. `timeout` adjusts per-attempt header/idle limits; `totalTimeout` bounds the whole request, including credentials and retries. SSE has separate read-lifecycle rules.
- **Retries:** at most 2 retries by default, subject to method safety, body replayability and server hints. HTTP 409 is never retried; non-idempotent writes only qualify for 429 retries. An idempotency key permits other eligible write retries.
- **Errors:** `ApiException` is the request-error base type. HTTP failures expose status and request ID through `ApiStatusException`; connection, timeout, cancellation and response-validation failures have distinct types.
- **Responses:** models expose `requestId()` and unknown JSON fields via `field(name)`. Use `withResponse()` for HTTP status and headers. Request builders distinguish omitted fields from explicit `null`.

See [client configuration](docs/client-configuration.md) for detailed defaults and [Template configuration](docs/templates.md) for typed configuration and field semantics.

## Realtime

Realtime (Beta) supports conversation creation, text and PCM audio exchange over WebSocket, Work events and persisted History. Use `client.realtime().conversations()` for creation/history and `client.realtime().connect(...)` for connections. The Template must have Realtime enabled and be accessible to the credential.

Microphone capture and audio playback belong to the application. Connections do not automatically reconnect or replay input. See the [Realtime guide](docs/realtime.md) and [runnable example](examples/src/main/java/com/qoder/cloudagents/examples/RealtimeExample.java) for authentication, audio formats and connection lifecycle.

## Examples and documentation

- [Runnable examples](examples/README.md): shared PAT/SA setup, resource lifecycles, Session SSE and Realtime.
- [API coverage](docs/forward-api-coverage.md): supported operations and guides for each resource.
- [Managed API](docs/managed.md): Agents, Deployments, Sessions, resources and self-hosted Work.
- [Client extensions](docs/client-configuration.md#client-extensions): custom HTTP transport, interceptors, generic JSON requests and configuration copies.
- [Files and downloads](docs/environments-files.md) and [Drive, Usage and SAT management](docs/usage-sat-drive.md).
- [Live testing](docs/live-testing.md): opt-in CN/Global verification and coverage boundaries.

## Development

With JDK 17+ and Python 3 installed, run from the repository root:

```sh
make quality
make test
```

These commands check formatting, documentation and maintenance scripts, then build the SDK and test the packaged JAR through the independent examples project. Default tests use local mock services. See [CONTRIBUTING.md](CONTRIBUTING.md), [maintenance](docs/maintaining.md) and [releasing](docs/releasing.md).

## Versioning

Pin the SDK version in your build. Before 1.0, incompatible API changes use a new minor version and include migration notes. See the [changelog](CHANGELOG.md) for upgrading from the RC or earlier internal coordinates.

## License

[MIT](LICENSE).

## Forward Webhooks (next release)

Manage webhook endpoints and verify signed event deliveries. See [Webhook usage and receiver examples](docs/webhooks.md).
