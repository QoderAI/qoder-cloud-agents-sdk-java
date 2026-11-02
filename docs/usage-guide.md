# Usage guide

Resource-specific behavior and examples for the Qoder Cloud Agents SDK. See the [README](../README.md) for installation, authentication and shared client configuration. Run shell commands from the repository root; code fragments use the clients and imports shown in the README unless stated otherwise.

- [API coverage](#api-coverage)
- [Conversations and streaming](#conversations-and-streaming)
- [Realtime](#realtime)
- [Forward Webhooks (next release)](#forward-webhooks-next-release)
- [Pagination](#pagination)
- [Further reading](#further-reading)

## API coverage

| Capability | Resources |
| --- | --- |
| Agent configuration | Forward Template, Identity and per-Identity configuration; Managed Agent and versions |
| Conversations | Session, Thread, event history and SSE subscriptions |
| Execution | Forward Schedule, Schedule Run, Batch and Batch Task; Managed Deployment, Run and self-hosted Work |
| Resources | Environment, File, Skill, Vault, Memory Store and resource Search; Forward Drive |
| Channels and notifications | Forward Channel configuration, Pairing management, QR authorization and Webhooks |
| Authentication and usage | PAT, Service Account credentials, Forward SAT issuance/revocation, Model and Usage |
| Realtime (Forward, Beta) | Text/audio WebSocket conversations and persisted History |

See the [Forward](../docs/forward-api-coverage.md) and [Managed](../docs/managed-api-coverage.md) coverage inventories and resource guides for the supported operations.

The [0.2.0 changelog](../CHANGELOG.md) covers Forward Search, Dreams, Decisions, pairing management, contract updates, Session creation metadata, Webhooks, client extensions and 107 Managed operations. See the [Forward migration guide](../docs/forward-parity.md) for the rubric field and reserved metadata changes.

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

Thread subscriptions use `sessions().threads().events()`. For explicit reconnects with the last delivered SSE ID, use `resumableStream(...)`; user messages are not automatically resent. See [Session operations](../docs/sessions.md), [streaming and cancellation](../docs/session-streaming.md) and the [runnable streaming example](../examples/src/main/java/com/qoder/cloudagents/examples/SessionStreamingExample.java).

## Realtime

Realtime (Beta) supports conversation creation, text and PCM audio exchange over WebSocket, Work events and persisted History. Use `client.realtime().conversations()` for creation/history and `client.realtime().connect(...)` for connections. The Template must have Realtime enabled and be accessible to the credential.

Microphone capture and audio playback belong to the application. Connections do not automatically reconnect or replay input. See the [Realtime guide](../docs/realtime.md) and [runnable example](../examples/src/main/java/com/qoder/cloudagents/examples/RealtimeExample.java) for authentication, audio formats and connection lifecycle.

## Forward Webhooks (next release)

Manage webhook endpoints and verify signed event deliveries. See [Webhook usage and receiver examples](../docs/webhooks.md).

## Pagination

Drive uses `entries` / `next_page_token`; Realtime History has its own `next_before` cursor. See the [resource guides](forward-api-coverage.md#使用指南) for those APIs.

## Further reading

- [Runnable examples](../examples/README.md): shared PAT/SA setup, resource lifecycles, Session SSE and Realtime.
- [API coverage](../docs/forward-api-coverage.md): supported operations and guides for each resource.
- [Managed API](../docs/managed.md): Agents, Deployments, Sessions, resources and self-hosted Work.
- [Client extensions](../docs/client-configuration.md#client-extensions): custom HTTP transport, interceptors, generic JSON requests and configuration copies.
- [Files and downloads](../docs/environments-files.md) and [Drive, Usage and SAT management](../docs/usage-sat-drive.md).
- [Live testing](../docs/live-testing.md): opt-in CN/Global verification and coverage boundaries.
