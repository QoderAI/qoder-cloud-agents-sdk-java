package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import com.qoder.cloudagents.streaming.*;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/** Read-only subscription to an existing Session or Thread, with explicit resumable streaming. */
public final class SessionStreamingExample {
  private SessionStreamingExample() {}

  public static void main(String[] args) throws Exception {
    String sessionId = System.getenv("QODER_SESSION_ID");
    if (sessionId == null || sessionId.isEmpty())
      throw new IllegalArgumentException("Set QODER_SESSION_ID to an existing authorized Session");
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(
          setup.client(),
          sessionId,
          System.getenv("QODER_THREAD_ID"),
          System.getenv("QODER_LAST_EVENT_ID"),
          "true".equals(System.getenv("QODER_STREAM_ASYNC")),
          20,
          System.out);
    }
  }

  public static void run(
      ForwardClient client,
      String sessionId,
      String threadId,
      String cursor,
      boolean asynchronous,
      int maxEvents,
      PrintStream out)
      throws Exception {
    if (maxEvents < 1) throw new IllegalArgumentException("maxEvents must be positive");
    RequestOptions options = RequestOptions.builder().totalTimeout(Duration.ofMinutes(2)).build();
    EventStream<SessionEvent> stream;
    if (threadId == null || threadId.isEmpty()) {
      SessionEventStreamParams.Builder params =
          SessionEventStreamParams.builder()
              .eventDeltas(Collections.singletonList("agent.message"));
      if (cursor != null) params.lastEventId(cursor);
      stream = client.sessions().events().resumableStream(sessionId, params.build(), options);
    } else {
      SessionThreadEventStreamParams.Builder params = SessionThreadEventStreamParams.builder();
      if (cursor != null) params.lastEventId(cursor);
      stream =
          client
              .sessions()
              .threads()
              .events()
              .resumableStream(sessionId, threadId, params.build(), options);
    }
    try (EventStream<SessionEvent> owned = stream) {
      if (asynchronous) {
        AtomicInteger count = new AtomicInteger();
        try (EventSubscription subscription =
            stream.subscribe(
                event -> {
                  out.println("event_id=" + stream.lastEventId() + " type=" + event.type());
                  if (count.incrementAndGet() >= maxEvents) stream.close();
                })) {
          subscription.completion().get();
        }
      } else {
        int count = 0;
        for (SessionEvent event : stream) {
          out.println("event_id=" + stream.lastEventId() + " type=" + event.type());
          if (++count >= maxEvents) break;
        }
      }
    } finally {
      // Persist this checkpoint alongside application processing state when resuming later.
      out.println("last_event_id=" + stream.lastEventId());
      if (stream.response() != null) out.println("request_id=" + stream.response().requestId());
    }
  }
}
