# Forward Realtime and History

Realtime is a Beta service capability and requires a Template with Realtime enabled. Java exposes the same public conversation and WebSocket routes as the pinned Go SDK. Realtime Template management routes are outside this SDK's scope.

## Create and connect

```java
RealtimeConversation conversation = client.realtime().conversations().create(
    RealtimeConversationCreateParams.builder()
        .identityId(identityId)
        .templateId(templateId)
        .title("Support conversation")
        .config(RealtimeConfig.builder()
            .audio(RealtimeAudioConfig.builder()
                .output(RealtimeAudioOutputConfig.builder().voice("longanqian").build())
                .build())
            .build())
        .build(),
    RequestOptions.builder().idempotencyKey(operationKey).build());

try (RealtimeConnection connection = client.realtime().connect(
    RealtimeConnectParams.builder().conversationId(conversation.id()).build())) {
  connection.sendText("Hello");
  RealtimeEvent event = connection.receive(Duration.ofSeconds(30));
  // Inspect event.type(), transcript(), audio(), work(), error(), or rawJson().
  // Continue processing the turn before closing.
}
```

Imports: `com.qoder.cloudagents.forward.realtime.*`, `com.qoder.cloudagents.core.RequestOptions`, `java.time.Duration`. Use the existing client authentication setup for PAT, direct SAT or `ServiceAccountCredential`. The latter resolves/caches a SAT before each explicit connection; the SA Key is never sent to the WebSocket business endpoint. An established socket is not automatically replaced when a credential refreshes.

Authentication reuse does not grant additional server permissions. For SA live verification, use a **Forward-issued, bound SAT** through `authMode(AuthMode.SA).accessToken(...)`, as with the existing SDKs. `ServiceAccountCredential` exchanges platform SATs; it does not create a Forward binding or silently switch to the Forward Token-management routes. Its WebSocket refresh behavior is verified with mock services, not a claim that every platform SAT can access Realtime.

Creation requires an application-generated stable `Idempotency-Key`; retry the same operation with the same key and body. A conflict remains an error. `withResponse().create(...)` exposes status, headers and `Idempotency-Replayed`; response objects retain the request ID. Closing does not delete the conversation or its history.

`connect` returns only after a valid `voice.ready`. Earlier events, including `voice.ready` itself, remain available in arrival order. Use `ready()` for input/output formats and capabilities, `handshakeResponse()` for HTTP upgrade metadata, and `requestId()` for diagnostics. Unknown event types and JSON fields remain accessible; known envelopes, ready formats, timestamps, PCM and typed payload fields are validated.

## Receive, send and cancellation

- `receive()` blocks; `receive(timeout)` bounds one wait. `receiveAsync()` and `receiveAsync(timeout)` return cancellable futures. Only one receive may be pending. Cancelling/timing out that receive leaves the connection open and does not discard later events.
- `client.async().realtime().connect(...)` returns a native asynchronous future. Cancellation stops token waiting/upgrade/ready waiting without cancelling a shared SA refresh. `sendTextAsync`, `appendAudioAsync`, `sendEventAsync`, `interruptAsync`, `pingAsync` and playback/close counterparts do not create blocking executor tasks.
- Sends are serialized. Successful completion means the transport wrote the message, not that the server processed it. Cancelling a queued send removes it; cancelling or timing out a started send closes the connection because delivery is uncertain. Input is never replayed automatically.
- Async continuations can execute on SDK/transport threads. Use `thenAcceptAsync`/an application executor for blocking work; do not synchronously wait for another event inside a completion callback.
- A normal peer close ends `receive` with `null`. Buffered events drain before a terminal error. Abnormal closes, replacement and fatal server error events terminate the connection; `RealtimeException` preserves conversation/request IDs, close code/reason and the triggering error event through explicit getters. Nonfatal `error` and `work.failed` remain events for the application to handle.
- Closing the parent client stops its connections. Always close a connection or use try-with-resources, including after asynchronous use. Closing a non-owning `client.async()` view does not close its parent.

There is no automatic reconnect or input replay. A caller can explicitly reconnect with the conversation ID and query persisted History to decide how to recover. WS `sequence`/`event_id` are not History or SSE cursors.

## Audio and playback

`appendAudio(byte[])` sends raw **PCM16 little-endian, 16 kHz, mono** as Base64 in a JSON text frame. It rejects empty/partial samples and oversized events. Microphone capture, WAV decoding, resampling, pacing and VAD policy belong to the application. There is no `audio.commit` operation.

`audio.delta` exposes decoded output bytes via `event.audio()`: **PCM16, 24 kHz, mono**. `audio.done` means output generation ended; it does not mean the user heard playback. Send `playbackStarted(workId, announcementId)` and `playbackEnded(...)` only for actual playback, carrying IDs from the received envelope. A client that will not play the output can send `playbackCancelled(...)`. `interrupt()` requests interruption; `playback.interrupt` tells the application to stop its own player.

`sendEvent(type, JsonValue)` is the forward-compatible escape hatch for JSON object payloads. It wraps the fixed protocol version and enforces the 256 KiB wire limit; callers own the payload semantics for custom events. `ping()`/`pong` are application events, separate from WebSocket control frames.

## Limits and close acknowledgement

| Setting | Default | Scope |
| --- | --- | --- |
| `handshakeTimeout` | 30 s | Credential lookup and HTTP upgrade |
| `readyTimeout` | 60 s | From HTTP 101 until validated `voice.ready` |
| `sendTimeout` | 30 s | One send, including time queued |
| `closeTimeout` | 10 s | Matching `connection.closed` acknowledgement |
| `queueCapacity` | 256 | Each of inbound and outbound event queues |
| `queueBytes` | 4 MiB | Each queue's UTF-8 JSON payload budget |
| `readLimit` | 1 MiB | Decoded text message accepted before JSON parsing |

`RequestOptions.timeout` overrides the handshake timeout only. Ordinary client HTTP timeouts do not end an established socket. Optional `RequestOptions.totalTimeout` bounds the whole connection, including credential lookup. One receive can also have its own timeout. Exceeding the incoming queue closes the connection; an outgoing queue overflow rejects the new send without dropping earlier sends.

OkHttp assembles/decompresses a WebSocket message before the SDK callback. Therefore `readLimit` bounds accepted messages and queued JSON, **not peak transport allocation for a single incoming message**. Queue budgets count UTF-8 bytes, not all Java object overhead. Use trusted service endpoints and set limits appropriate for the consumer.

`closeGracefully()` / `closeGracefullyAsync()` send a unique `connection.close` request and wait for a `connection.closed` event with the same request ID. They require `ready().capabilities().gracefulClose()`. The returned outcome is `no_active_response`, `no_content`, `saved` or `saved_interrupted`; interpret that value instead of assuming every close saved a response. A mismatched acknowledgement, transport-only close or timeout is not success. Ordinary `close()` only releases transport resources.

Upgrade failures are attempted once, including redirects, authentication errors and retryable HTTP statuses. HTTP failures expose status/request ID through `ApiStatusException`. For the library's prevented HTTP follow-up (for example 503 with `Retry-After: 0`), status and headers are retained but the already-closed response body may be unavailable.

## History

```java
RealtimeHistoryPage page = client.realtime().conversations().history(
    conversationId,
    RealtimeHistoryParams.builder().limit(50).types("message,work").build());
for (RealtimeHistoryEvent event : page) {
  // Lazily fetch older pages; each page keeps server order.
  System.out.println(event.id() + " " + event.type());
}
```

Use `events()` for one page, `iterPages()` for page-by-page iteration, or `hasNextPage()` followed by `getNextPage()` / `getNextPageAsync()`. The next request uses `page.next_before` as `before`, preserving filters and request options. Empty pages can still continue if a cursor is present. Missing or repeated cursors fail explicitly; calls after exhaustion fail rather than manufacturing a cursor. Requests are lazy, without background prefetch. Per-request timeouts apply separately to each History page.

## Executable example and evidence

See [RealtimeExample](../examples/src/main/java/com/qoder/cloudagents/examples/RealtimeExample.java) for creation, text, paced raw PCM, truthful playback cancellation, graceful close and bounded History iteration. It shares the PAT/SA example setup. The independent examples project consumes the installed JAR, not SDK source.

Default tests use local mock servers. See [live testing](live-testing.md) for opt-in text, PCM and Work checks in CN/Global.
