package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.auth.*;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import com.qoder.cloudagents.streaming.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Timeout(15)
class SessionStreamsTest {
  MockWebServer server;
  ForwardClient client;

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.start();
    client = builder().pat("test-pat").maxRetries(0).build();
  }

  ForwardClient.Builder builder() {
    return ForwardClient.builder().baseUrl(server.url("/api/v1/forward").toString());
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    server.close();
  }

  MockResponse sse(String body) {
    return new MockResponse()
        .setHeader("Content-Type", "text/event-stream; charset=utf-8")
        .setHeader("x-request-id", "req-sse")
        .setBody(body);
  }

  String event(String id, String type) {
    return "id: "
        + id
        + "\nevent: "
        + type
        + "\ndata: {\"id\":\"json-id\",\"type\":\""
        + type
        + "\",\"future\":17}\n\n";
  }

  RecordedRequest take() throws Exception {
    RecordedRequest r = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  RequestOptions deadline(long ms) {
    return RequestOptions.builder().totalTimeout(Duration.ofMillis(ms)).build();
  }

  @Test
  void caseInsensitiveCursorOverrideDoesNotRestoreOriginalOnReconnect() throws Exception {
    server.enqueue(sse(event("yielded", "agent.message")));
    server.enqueue(sse(event("end", "session.deleted")));
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .events()
            .resumableStream(
                "s",
                SessionEventStreamParams.builder().build(),
                RequestOptions.builder()
                    .header("Last-Event-ID", "older")
                    .header("last-event-id", "initial")
                    .build())) {
      Iterator<SessionEvent> it = stream.iterator();
      it.next();
      it.next();
    }
    assertEquals("initial", take().getHeader("Last-Event-ID"));
    assertEquals("yielded", take().getHeader("Last-Event-ID"));
  }

  @Test
  void explicitCloseInReconnectWaitEndsNormallyWithoutAnotherRequest() throws Exception {
    server.enqueue(sse(event("first", "agent.message")));
    try (EventStream<SessionEvent> stream = client.sessions().events().resumableStream("s")) {
      Iterator<SessionEvent> it = stream.iterator();
      it.next();
      ExecutorService worker = Executors.newSingleThreadExecutor();
      CountDownLatch reading = new CountDownLatch(1);
      try {
        Future<Boolean> next =
            worker.submit(
                () -> {
                  reading.countDown();
                  return it.hasNext();
                });
        assertTrue(reading.await(1, TimeUnit.SECONDS));
        stream.close();
        assertFalse(next.get(1, TimeUnit.SECONDS));
        assertEquals(1, server.getRequestCount());
      } finally {
        worker.shutdownNow();
      }
    }
  }

  @Test
  void abruptSocketReadFailureResumes() throws Exception {
    String first = event("first", "agent.message");
    server.enqueue(
        sse(first + event("lost", "agent.message"))
            .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY));
    server.enqueue(sse(event("end", "session.deleted")));
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .events()
            .resumableStream(
                "s",
                SessionEventStreamParams.builder().lastEventId("initial").build(),
                deadline(5000))) {
      for (SessionEvent ignored : stream) {}
      assertEquals("end", stream.lastEventId());
    }
    take();
    RecordedRequest resumed = take();
    // The interrupted frame is never acknowledged; either first was complete or we retain initial.
    assertTrue(Arrays.asList("first", "initial").contains(resumed.getHeader("Last-Event-ID")));
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void readerThreadTerminatesAfterCompletion() throws Exception {
    server.enqueue(sse(event("done", "session.deleted")));
    AtomicReference<Thread> reader = new AtomicReference<>();
    try (EventStream<SessionEvent> stream = client.sessions().events().streamEvents("s")) {
      EventSubscription subscription = stream.subscribe(e -> reader.set(Thread.currentThread()));
      subscription.completion().get(2, TimeUnit.SECONDS);
      reader.get().join(2000);
      assertFalse(reader.get().isAlive());
    }
  }

  @Test
  void authFailureNeverReconnectsOrSendsBusinessRequest() {
    try (ForwardClient c =
            builder()
                .credential(
                    () -> {
                      CompletableFuture<String> failed = new CompletableFuture<>();
                      failed.completeExceptionally(
                          new ConfigurationException("credential unavailable"));
                      return failed;
                    })
                .build();
        EventStream<SessionEvent> stream = c.sessions().events().resumableStream("s")) {
      assertThrows(ConfigurationException.class, () -> stream.iterator().hasNext());
      assertEquals(0, server.getRequestCount());
    }
  }

  @Test
  void unauthorizedRetryHintCannotOverrideSseAuthFailure() {
    server.enqueue(
        new MockResponse().setResponseCode(401).setHeader("x-should-retry", "true").setBody("{}"));
    try (ForwardClient c = builder().pat("invalid").maxRetries(2).build()) {
      assertThrows(ApiStatusException.class, () -> c.sessions().events().streamEvents("s"));
      assertEquals(1, server.getRequestCount());
    }
  }

  @Test
  void sessionSelectorsHeadersMetadataAndUnknownEvents() throws Exception {
    server.enqueue(sse(event("evt-1", "future.event")));
    List<String> deltas = new ArrayList<>(Arrays.asList("agent.message", "agent.thinking"));
    SessionEventStreamParams params =
        SessionEventStreamParams.builder()
            .eventDeltas(deltas)
            .includeThinking(false)
            .includeToolCalls(true)
            .lastEventId("old")
            .build();
    deltas.clear();
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .events()
            .streamEvents(
                "sess/a?",
                params,
                RequestOptions.builder()
                    .header("Last-Event-ID", "override")
                    .header("X-Qoder-Beta", "thinking-event-stream-delta-2026-07-20")
                    .build())) {
      Iterator<SessionEvent> it = stream.iterator();
      assertTrue(it.hasNext());
      assertEquals("override", stream.lastEventId());
      SessionEvent e = it.next();
      assertEquals("future.event", e.type());
      assertEquals(17, e.field("future").asLong());
      assertEquals("req-sse", e.requestId());
      assertEquals("evt-1", stream.lastEventId());
      assertEquals(200, stream.response().statusCode());
      assertFalse(it.hasNext());
      assertThrows(IllegalStateException.class, stream::iterator);
    }
    RecordedRequest r = take();
    assertEquals(
        "/api/v1/forward/sessions/sess%2Fa%3F/events/stream", r.getRequestUrl().encodedPath());
    assertEquals(
        Arrays.asList("agent.message", "agent.thinking"),
        r.getRequestUrl().queryParameterValues("event_deltas[]"));
    assertEquals("false", r.getRequestUrl().queryParameter("include_thinking"));
    assertEquals("true", r.getRequestUrl().queryParameter("include_tool_calls"));
    assertEquals("override", r.getHeader("Last-Event-ID"));
    assertEquals("text/event-stream", r.getHeader("Accept"));
    assertEquals("Bearer test-pat", r.getHeader("Authorization"));
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void threadHasOwnRouteAndNoSessionSelectors() throws Exception {
    server.enqueue(sse(event("evt", "agent.message")));
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .threads()
            .events()
            .streamEvents(
                "sess/a",
                "sthr/b",
                SessionThreadEventStreamParams.builder().lastEventId("thread-cursor").build())) {
      assertEquals("agent.message", stream.iterator().next().type());
    }
    RecordedRequest r = take();
    assertEquals("/api/v1/forward/sessions/sess%2Fa/threads/sthr%2Fb/stream", r.getPath());
    assertEquals("thread-cursor", r.getHeader("Last-Event-ID"));
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .sessions()
                .threads()
                .events()
                .resumableStream(
                    "s",
                    "t",
                    SessionThreadEventStreamParams.builder().build(),
                    RequestOptions.builder().query("include_thinking", "").build()));
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void reconnectUsesOnlyYieldedFrameCursorAndPreservesSelectors() throws Exception {
    server.enqueue(sse(event("evt-1", "event_delta") + "id: partial\ndata: {\"type\":\"lost\"}\n"));
    server.enqueue(sse(event("evt-2", "session.status_terminated")));
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .events()
            .resumableStream(
                "s",
                SessionEventStreamParams.builder()
                    .eventDeltas(Arrays.asList("agent.message"))
                    .includeToolCalls(false)
                    .lastEventId("start")
                    .build(),
                RequestOptions.builder()
                    .header("x-test", "retained")
                    .query("future", "a/b")
                    .totalTimeout(Duration.ofSeconds(5))
                    .build())) {
      Iterator<SessionEvent> it = stream.iterator();
      assertEquals("event_delta", it.next().type());
      assertEquals("evt-1", stream.lastEventId());
      assertEquals("session.status_terminated", it.next().type());
      assertFalse(it.hasNext());
      assertEquals("evt-2", stream.lastEventId());
    }
    RecordedRequest first = take(), second = take();
    assertEquals("start", first.getHeader("Last-Event-ID"));
    assertEquals("evt-1", second.getHeader("Last-Event-ID"));
    assertEquals(first.getPath(), second.getPath());
    assertEquals("retained", second.getHeader("x-test"));
    assertEquals("GET", second.getMethod());
    assertEquals(0, second.getBodySize());
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void emptyCheckpointClearsPreviousCursorOnReconnect() throws Exception {
    server.enqueue(sse(event("", "agent.message")));
    server.enqueue(sse(event("end", "session.deleted")));
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .events()
            .resumableStream(
                "s",
                SessionEventStreamParams.builder().lastEventId("old").build(),
                deadline(5000))) {
      Iterator<SessionEvent> it = stream.iterator();
      it.next();
      assertEquals("", stream.lastEventId());
      it.next();
      assertFalse(it.hasNext());
    }
    take();
    assertEquals("", take().getHeader("Last-Event-ID"));
  }

  @Test
  void threadReconnectAndFreshCredentialForEachConnection() throws Exception {
    AtomicInteger tokens = new AtomicInteger();
    try (ForwardClient c =
        builder()
            .credential(() -> CompletableFuture.completedFuture("sat-" + tokens.incrementAndGet()))
            .maxRetries(0)
            .build()) {
      server.enqueue(sse(event("thread-1", "agent.message")));
      server.enqueue(sse(event("thread-2", "session.status_terminated")));
      try (EventStream<SessionEvent> stream =
          c.sessions()
              .threads()
              .events()
              .resumableStream(
                  "s", "t", SessionThreadEventStreamParams.builder().build(), deadline(5000))) {
        Iterator<SessionEvent> it = stream.iterator();
        it.next();
        it.next();
        assertFalse(it.hasNext());
      }
    }
    assertEquals("Bearer sat-1", take().getHeader("Authorization"));
    RecordedRequest second = take();
    assertEquals("Bearer sat-2", second.getHeader("Authorization"));
    assertEquals("thread-1", second.getHeader("Last-Event-ID"));
  }

  @ParameterizedTest
  @ValueSource(ints = {400, 401, 403, 404, 409})
  void nonRetryableStatusKeepsRequestIdAndDoesNotReconnect(int code) throws Exception {
    server.enqueue(
        new MockResponse()
            .setResponseCode(code)
            .setHeader("x-request-id", "req-error")
            .setBody("{\"error\":{\"code\":\"event_cursor_not_found\"}}"));
    try (EventStream<SessionEvent> stream = client.sessions().events().resumableStream("s")) {
      ApiStatusException error =
          assertThrows(ApiStatusException.class, () -> stream.iterator().hasNext());
      assertEquals(code, error.statusCode());
      assertEquals("req-error", error.requestId());
    }
    assertEquals(1, server.getRequestCount());
  }

  @ParameterizedTest
  @ValueSource(ints = {408, 429, 500, 502})
  void explicitResumeRetriesTransientStatus(int code) throws Exception {
    server.enqueue(new MockResponse().setResponseCode(code).setBody("{}"));
    server.enqueue(sse(event("end", "session.deleted")));
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .events()
            .resumableStream("s", SessionEventStreamParams.builder().build(), deadline(5000))) {
      assertEquals("session.deleted", stream.iterator().next().type());
    }
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void retryHintFalseStopsResume() {
    server.enqueue(
        new MockResponse().setResponseCode(503).setHeader("x-should-retry", "false").setBody("{}"));
    try (EventStream<SessionEvent> stream = client.sessions().events().resumableStream("s")) {
      assertThrows(ApiStatusException.class, () -> stream.iterator().hasNext());
    }
    assertEquals(1, server.getRequestCount());
  }

  @ParameterizedTest
  @ValueSource(strings = {"not-json", "[]", "{\"type\":3}"})
  void invalidPayloadStopsInsteadOfReconnectingOrAdvancingCursor(String json) {
    server.enqueue(sse("id: bad\ndata: " + json + "\n\n"));
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .events()
            .resumableStream("s", SessionEventStreamParams.builder().lastEventId("safe").build())) {
      assertThrows(ResponseValidationException.class, () -> stream.iterator().next());
      assertEquals("safe", stream.lastEventId());
    }
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void errorFrameIsTypedAndPayloadIsNotLogged() {
    server.enqueue(sse("event: error\ndata: {\"error\":{\"message\":\"private-detail\"}}\n\n"));
    try (EventStream<SessionEvent> stream = client.sessions().events().resumableStream("s")) {
      StreamException error = assertThrows(StreamException.class, () -> stream.iterator().next());
      assertEquals("req-sse", error.requestId());
      assertTrue(error.body().toJson().contains("private-detail"));
      assertFalse(error.toString().contains("private-detail"));
    }
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void doneAndPingDoNotYieldOrReconnect() {
    server.enqueue(sse("event: ping\ndata: ignored\n\ndata: [DONE]\n\n"));
    try (EventStream<SessionEvent> stream = client.sessions().events().resumableStream("s")) {
      assertFalse(stream.iterator().hasNext());
    }
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void wrongContentTypeFailsBeforeDeliveringStream() {
    server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{}"));
    assertThrows(
        ResponseValidationException.class, () -> client.sessions().events().streamEvents("s"));
  }

  @Test
  void headerTimeoutDoesNotLimitOpenBodyLifetime() throws Exception {
    server.enqueue(sse(event("late", "agent.message")).setBodyDelay(350, TimeUnit.MILLISECONDS));
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .events()
            .streamEvents(
                "s",
                SessionEventStreamParams.builder().build(),
                RequestOptions.builder().timeout(Duration.ofMillis(150)).build())) {
      assertEquals("agent.message", stream.iterator().next().type());
    }
  }

  @Test
  void headerTimeoutStillApplies() {
    server.enqueue(sse("").setHeadersDelay(500, TimeUnit.MILLISECONDS));
    assertThrows(
        ApiTimeoutException.class,
        () ->
            client
                .sessions()
                .events()
                .streamEvents(
                    "s",
                    SessionEventStreamParams.builder().build(),
                    RequestOptions.builder().timeout(Duration.ofMillis(100)).build()));
  }

  @Test
  void totalDeadlineCoversBodyAndReconnects() throws Exception {
    server.enqueue(sse(event("first", "agent.message")));
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .events()
            .resumableStream("s", SessionEventStreamParams.builder().build(), deadline(150))) {
      Iterator<SessionEvent> it = stream.iterator();
      it.next();
      assertThrows(ApiTimeoutException.class, it::hasNext);
    }
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void totalDeadlineInterruptsActiveRead() throws Exception {
    server.enqueue(sse(event("late", "agent.message")).setBodyDelay(5, TimeUnit.SECONDS));
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .events()
            .streamEvents("s", SessionEventStreamParams.builder().build(), deadline(200))) {
      assertThrows(ApiTimeoutException.class, () -> stream.iterator().hasNext());
    }
  }

  @Test
  void asyncFutureCancellationLeavesSharedCredentialUsable() throws Exception {
    CompletableFuture<String> token = new CompletableFuture<>();
    try (AsyncForwardClient c = builder().credential(() -> token).buildAsync()) {
      CompletableFuture<EventStream<SessionEvent>> future = c.sessions().events().streamEvents("s");
      assertTrue(future.cancel(true));
      assertFalse(token.isCancelled());
      token.complete("sat");
      assertEquals(0, server.getRequestCount());
    }
  }

  @Test
  void closeDuringLazyCredentialLookupWakesConsumer() throws Exception {
    CompletableFuture<String> token = new CompletableFuture<>();
    CountDownLatch requested = new CountDownLatch(1);
    try (ForwardClient c =
            builder()
                .credential(
                    () -> {
                      requested.countDown();
                      return token;
                    })
                .build();
        EventStream<SessionEvent> stream = c.sessions().events().resumableStream("s")) {
      EventSubscription subscription = stream.subscribe(e -> fail("No event expected"));
      assertTrue(requested.await(2, TimeUnit.SECONDS));
      stream.close();
      subscription.completion().get(2, TimeUnit.SECONDS);
      assertFalse(token.isCancelled());
      token.complete("sat");
      assertEquals(0, server.getRequestCount());
    }
  }

  @Test
  void closeDuringBodyReadReleasesReader() throws Exception {
    server.enqueue(sse(event("late", "agent.message")).setBodyDelay(5, TimeUnit.SECONDS));
    try (EventStream<SessionEvent> stream = client.sessions().events().streamEvents("s")) {
      EventSubscription subscription = stream.subscribe(e -> fail("No event expected"));
      stream.close();
      subscription.completion().get(2, TimeUnit.SECONDS);
    }
  }

  @Test
  void clientCloseDuringReconnectWaitStopsFutureRequests() throws Exception {
    server.enqueue(sse(""));
    CountDownLatch received = new CountDownLatch(1);
    try (EventStream<SessionEvent> stream = client.sessions().events().resumableStream("s")) {
      EventSubscription subscription = stream.subscribe(e -> received.countDown());
      take();
      client.close();
      ExecutionException failure =
          assertThrows(
              ExecutionException.class, () -> subscription.completion().get(2, TimeUnit.SECONDS));
      assertInstanceOf(ApiUserAbortException.class, failure.getCause());
      assertEquals(1, server.getRequestCount());
    }
  }

  @Test
  void orderedCallbacksUseSpecifiedExecutorAndApplyBackpressure() throws Exception {
    server.enqueue(
        sse(
            event("1", "agent.message")
                + event("2", "agent.message")
                + event("3", "session.deleted")));
    ExecutorService executor =
        Executors.newSingleThreadExecutor(r -> new Thread(r, "application-events"));
    List<String> ids = new CopyOnWriteArrayList<>();
    CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
    try (EventStream<SessionEvent> stream = client.sessions().events().streamEvents("s")) {
      EventSubscription subscription =
          stream.subscribe(
              e -> {
                assertEquals("application-events", Thread.currentThread().getName());
                ids.add(stream.lastEventId());
                if (ids.size() == 1) {
                  entered.countDown();
                  try {
                    release.await();
                  } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                  }
                }
              },
              executor);
      assertTrue(entered.await(2, TimeUnit.SECONDS));
      assertEquals(Collections.singletonList("1"), ids);
      assertEquals("1", stream.lastEventId());
      release.countDown();
      subscription.completion().get(2, TimeUnit.SECONDS);
      assertEquals(Arrays.asList("1", "2", "3"), ids);
      assertFalse(executor.isShutdown());
    } finally {
      release.countDown();
      executor.shutdownNow();
    }
  }

  @Test
  void callbackFailureClosesConnectionAndIsReported() throws Exception {
    server.enqueue(sse(event("1", "agent.message")));
    try (EventStream<SessionEvent> stream = client.sessions().events().streamEvents("s")) {
      EventSubscription subscription =
          stream.subscribe(
              e -> {
                throw new IllegalStateException("consumer failure");
              });
      ExecutionException error =
          assertThrows(
              ExecutionException.class, () -> subscription.completion().get(2, TimeUnit.SECONDS));
      assertInstanceOf(IllegalStateException.class, error.getCause());
    }
  }

  @Test
  void executorRejectionIsReportedAndClosesStream() throws Exception {
    server.enqueue(sse(event("1", "agent.message")));
    try (EventStream<SessionEvent> stream = client.sessions().events().streamEvents("s")) {
      EventSubscription subscription =
          stream.subscribe(
              e -> {},
              r -> {
                throw new RejectedExecutionException();
              });
      assertThrows(ExecutionException.class, () -> subscription.completion().get());
    }
  }

  @Test
  void asyncThreadOpenUsesSameTransport() throws Exception {
    server.enqueue(sse(event("e", "agent.message")));
    try (AsyncForwardClient c = builder().pat("pat-async").buildAsync();
        EventStream<SessionEvent> stream =
            c.sessions().threads().events().streamEvents("s", "t").get(2, TimeUnit.SECONDS)) {
      assertEquals("agent.message", stream.iterator().next().type());
    }
    assertEquals("Bearer pat-async", take().getHeader("Authorization"));
  }
}
