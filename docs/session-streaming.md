# Session / Thread SSE

Java 8 synchronous iteration, asynchronous connection establishment and ordered callbacks share the same authentication and transport as ordinary HTTP resources. These APIs subscribe to existing Sessions; closing a stream does not cancel the server-side Session.

## Single connection

```java
import com.qoder.cloudagents.forward.*;
import com.qoder.cloudagents.streaming.*;
import java.util.Arrays;

SessionEventStreamParams params = SessionEventStreamParams.builder()
    .eventDeltas(Arrays.asList("agent.message"))
    .includeToolCalls(true)
    .lastEventId("evt_from_history")
    .build();

try (EventStream<SessionEvent> stream = client.sessions().events()
    .streamEvents("sess_existing", params)) {
  for (SessionEvent event : stream) {
    // Preserve unknown types and inspect delta/content according to event.type().
    process(event);
    saveCheckpoint(stream.lastEventId());
  }
}
```

`streamEvents` opens one accepted SSE connection. HTTP handshake retries follow request/client settings; EOF ends iteration, and read/protocol failures are reported. `client.async().sessions().events().streamEvents(...)` returns a cancellable `CompletableFuture<EventStream<SessionEvent>>` that completes after accepted response headers. Reading that stream still blocks unless you use `subscribe` on a reader executor.

Use `stream.response()` for the latest successful connection's status, headers and request ID; each `SessionEvent.requestId()` also identifies its connection. `stream.response()` is null before a lazy stream has connected. No server payload is automatically logged.

The Session endpoint is `/sessions/{session_id}/events/stream`. It sends repeated `event_deltas[]` plus optional `include_tool_calls` and `include_thinking`. Values are copied at build time. Thinking deltas require the documented beta header in `RequestOptions`; the SDK does not silently opt into that beta.

## Explicit reconnecting subscription

```java
import com.qoder.cloudagents.core.RequestOptions;
import java.time.Duration;

try (EventStream<SessionEvent> stream = client.sessions().events().resumableStream(
    "sess_existing", params,
    RequestOptions.builder().totalTimeout(Duration.ofMinutes(5)).build())) {
  for (SessionEvent event : stream) {
    process(event);
    saveCheckpoint(stream.lastEventId());
  }
}
```

`resumableStream` is lazy and reconnects after EOF, transport failures and retryable HTTP errors. Reconnect waits use jittered exponential backoff from 500 ms to a 10-second cap. This continues until closed, the total deadline expires, a terminal event arrives, or a nonretryable error occurs. `maxRetries` governs each HTTP handshake, not the lifetime of this explicitly persistent subscription. A connection that yields events after five seconds resets its backoff.

- Reconnect retains the path, filters and request options, but replaces `Last-Event-ID` with the last fully **yielded** frame's ID. Calling `hasNext()` alone does not advance the public checkpoint. An incomplete or invalid frame is not acknowledged. An empty SSE ID clears the cursor. The SSE frame ID is used, not an assumed JSON payload ID.
- It calls the shared Credential again for each connection. `ServiceAccountCredential` reuses or refreshes SAT according to expiry; it does not send an SA Key to business endpoints. There is no mid-connection token refresh or forced reconnect just because a token expires.
- HTTP 401/403, invalid/missing cursors, malformed payloads and SSE `event: error` stop the subscription. Error frames raise `StreamException` with explicit `body()` and `requestId()` access; HTTP failures remain `ApiStatusException`.
- `session.status_terminated`, `session.deleted` and `[DONE]` end the stream. `session.status_idle` does not end a subscription. Parent-stream child Thread lifecycle events remain ordinary events.
- Resume never resends user messages and does not claim exactly-once delivery. Deltas can share an event ID: do not deduplicate all frames solely by ID. Application processing/checkpoint persistence must address replay and partial incremental output.
- With no cursor the service starts at the current tail, not historical events. To recover older events, first use the appropriate Session/Thread history endpoint and supply its last ID. A 404 `event_cursor_not_found` requires deliberate history recovery, not an automatic cursor reset.

## Thread events

```java
try (EventStream<SessionEvent> stream = client.sessions().threads().events()
    .resumableStream("sess_existing", "sthr_child",
        SessionThreadEventStreamParams.builder().lastEventId("evt_thread_cursor").build(),
        RequestOptions.builder().totalTimeout(Duration.ofMinutes(5)).build())) {
  for (SessionEvent event : stream) process(event);
}
```

Thread SSE uses `/sessions/{session_id}/threads/{thread_id}/stream`, not the Thread history `/events` path. It does not accept Session query filters; the Java entry point rejects query overrides. The caller must use a cursor valid for this Thread. A terminated/archived Thread that cannot reopen is surfaced as the service's HTTP error; no automatic re-creation occurs.

## Asynchronous callbacks and cancellation

```java
try (EventStream<SessionEvent> stream = client.async().sessions().events()
         .resumableStream("sess_existing", params);
     EventSubscription subscription = stream.subscribe(event -> process(event))) {
  subscription.completion().get();
}
```

`subscribe(onEvent)` owns a daemon reader thread, released at completion. `subscribe(onEvent, executor)` runs blocking reads and callbacks sequentially on the supplied executor; that executor remains caller-owned. Use a suitable dedicated executor rather than an event-loop thread. A callback exception ends consumption and fails `completion()`.

There is no event queue: one consumer reads and processes one event at a time, so a slow callback applies backpressure rather than accumulating events. One decoded event may be prefetched by iteration. The incremental parser bounds each frame to 8,388,608 UTF-16 code units and rejects oversized frames/lines, malformed UTF-8 and malformed JSON. Network/reader buffers remain bounded. Heartbeats are consumed without callbacks, unknown event types/fields are preserved, and both LF/CRLF/CR separators and multi-line data are supported.

`stream.close()`, `subscription.close()` or cancellation of `subscription.completion()` release the network read and stop reconnects. Cancelling the asynchronous opening future cancels only that request, not shared credential refresh. Client close aborts all its streams, including credential and reconnect waits. User callbacks already running must return cooperatively; the SDK does not interrupt application logic. Callback subscription close reports cancellation through its future; normal EOF/terminal completion succeeds.

`RequestOptions.timeout` limits response-header waiting for each SSE connection, not the lifetime of an accepted body. There is no default SSE read-idle timer. `totalTimeout` covers the whole subscription from construction, including credential lookup, reconnect waits and all body reads. Always close early-exited streams and set an application deadline where appropriate.

To cancel the actual Session execution, use the existing `client.sessions().cancel(...)` operation separately. Subscription cleanup never performs that write operation.

## Runnable example and validation

[SessionStreamingExample](../examples/src/main/java/com/qoder/cloudagents/examples/SessionStreamingExample.java) supports existing Session or Thread, shared PAT/SA setup, sync/async consumption, a 20-event limit and a two-minute total deadline. It logs only event IDs/types, checkpoint and request ID. Tests run it as an independent consumer of the installed JAR.

Protocol behavior follows the [shared SDK baseline](forward-api-coverage.md). Offline checks do not establish CN/Global production compatibility; use [live testing](live-testing.md) to exercise Session execution and streaming in a selected environment.
