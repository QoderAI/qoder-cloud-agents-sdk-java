package com.qoder.cloudagents;

import static com.qoder.cloudagents.RealtimeContractsTest.*;
import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.realtime.*;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import okio.ByteString;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class RealtimeConnectionTest {
  MockWebServer server;
  ForwardClient client;
  BlockingQueue<WebSocket> peers = new LinkedBlockingQueue<>();
  BlockingQueue<JsonValue> messages = new LinkedBlockingQueue<>();
  List<WebSocket> allPeers = new CopyOnWriteArrayList<>();

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.start();
    client = builder().pat("test-pat").build();
  }

  ForwardClient.Builder builder() {
    return ForwardClient.builder().baseUrl(server.url("/api/v1/forward").toString());
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    allPeers.forEach(ws -> ws.close(1000, "test cleanup"));
    server.close();
  }

  RealtimeConnectParams.Builder params() {
    return RealtimeConnectParams.builder().conversationId("conv_test");
  }

  void upgrade(boolean ready, boolean closeAck) {
    server.enqueue(
        new MockResponse()
            .setHeader("x-request-id", "req-ws")
            .withWebSocketUpgrade(
                new WebSocketListener() {
                  @Override
                  public void onOpen(WebSocket ws, Response response) {
                    allPeers.add(ws);
                    peers.add(ws);
                    if (ready) send(ws, "voice.ready");
                  }

                  @Override
                  public void onMessage(WebSocket ws, String text) {
                    JsonValue value = JsonValue.parse(text);
                    messages.add(value);
                    if (closeAck && value.get("type").asString().equals("connection.close")) {
                      try {
                        ws.send(
                            change(
                                    fixture("connection.closed"),
                                    "payload",
                                    change(
                                        fixture("connection.closed").get("payload"),
                                        "request_id",
                                        value.get("payload").get("request_id")))
                                .toJson());
                      } catch (Exception e) {
                        throw new AssertionError(e);
                      }
                    }
                  }

                  @Override
                  public void onClosing(WebSocket ws, int code, String reason) {
                    ws.close(code, reason);
                  }
                }));
  }

  WebSocket peer() throws Exception {
    WebSocket ws = peers.poll(3, TimeUnit.SECONDS);
    assertNotNull(ws);
    return ws;
  }

  JsonValue message() throws Exception {
    JsonValue value = messages.poll(3, TimeUnit.SECONDS);
    assertNotNull(value);
    return value;
  }

  void send(WebSocket ws, String type) {
    try {
      assertTrue(ws.send(fixture(type).toJson()));
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  RealtimeConnection connect() {
    return client.realtime().connect(params().build());
  }

  static Throwable failed(CompletableFuture<?> f) {
    return assertThrows(ExecutionException.class, () -> f.get(5, TimeUnit.SECONDS)).getCause();
  }

  @Test
  void waitsForReadyAndRetainsEarlierEventsAndHandshakeMetadata() throws Exception {
    upgrade(false, false);
    CompletableFuture<RealtimeConnection> future =
        client.async().realtime().connect(params().build());
    WebSocket ws = peer();
    send(ws, "voice.state");
    assertFalse(future.isDone());
    send(ws, "voice.ready");
    try (RealtimeConnection connection = future.get(3, TimeUnit.SECONDS)) {
      assertEquals("req-ws", connection.requestId());
      assertEquals(101, connection.handshakeResponse().statusCode());
      assertEquals("voice.state", connection.receive().type());
      assertEquals("voice.ready", connection.receive().type());
      assertEquals("longanqian", connection.ready().config().audio().output().voice());
      connection.sendText("hello");
      assertEquals("hello", message().get("payload").get("text").asString());
    }
    RecordedRequest request = server.takeRequest();
    assertEquals("GET", request.getMethod());
    assertEquals("/api/v1/forward/realtime?conversation_id=conv_test", request.getPath());
    assertEquals("Bearer test-pat", request.getHeader("Authorization"));
    assertEquals("java", request.getHeader("X-Qoder-Lang"));
  }

  @Test
  void textAudioWorkAndPlaybackUseJsonTextFrames() throws Exception {
    upgrade(true, true);
    try (RealtimeConnection c = connect()) {
      WebSocket ws = peer();
      assertEquals("voice.ready", c.receive().type());
      c.appendAudio(new byte[] {0, 0, 1, 0});
      assertEquals("AAABAA==", message().get("payload").get("audio").asString());
      c.interrupt();
      assertEquals("interrupt", message().get("type").asString());
      c.ping();
      assertEquals("ping", message().get("type").asString());
      c.playbackStarted("work-1", "announcement-1");
      c.playbackEnded("work-1", "announcement-1");
      c.playbackCancelled(null, null);
      assertEquals("work-1", message().get("payload").get("work_id").asString());
      assertEquals("announcement-1", message().get("payload").get("announcement_id").asString());
      assertEquals("playback.cancelled", message().get("type").asString());
      for (String type :
          Arrays.asList(
              "transcript.final",
              "audio.delta",
              "audio.done",
              "work.running",
              "work.completed",
              "work.failed",
              "future.extension")) {
        send(ws, type);
        assertEquals(type, c.receive(Duration.ofSeconds(2)).type());
      }
      assertFalse(c.isClosed());
      assertNull(messages.poll(50, TimeUnit.MILLISECONDS));
      assertEquals("saved", c.closeGracefully().outcome());
      assertTrue(c.isClosed());
      assertEquals("connection.close", message().get("type").asString());
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {301, 302, 401, 403, 408, 409, 429, 500, 503})
  void upgradeFailureIsNotRetriedOrRedirected(int code) {
    server.enqueue(
        new MockResponse()
            .setResponseCode(code)
            .setHeader("Location", server.url("/stolen"))
            .setHeader("Retry-After", "0")
            .setHeader("x-should-retry", "true")
            .setHeader("x-request-id", "req-fail")
            .setBody("{\"error\":{\"code\":\"denied\"}}"));
    ApiStatusException e = assertThrows(ApiStatusException.class, this::connect);
    assertEquals(code, e.statusCode());
    assertEquals("req-fail", e.requestId());
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void handshakeHasDeadlineWhileWaitingForTokenAndDoesNotCancelSharedRefresh() {
    CompletableFuture<String> token = new CompletableFuture<>();
    client.close();
    client = builder().credential(() -> token).build();
    Throwable error =
        failed(
            client
                .async()
                .realtime()
                .connect(params().handshakeTimeout(Duration.ofMillis(150)).build()));
    assertInstanceOf(ApiTimeoutException.class, error);
    assertFalse(token.isCancelled());
    token.complete("late-token");
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void cancellationDuringTokenResolutionNeverStartsUpgrade() {
    CompletableFuture<String> token = new CompletableFuture<>();
    client.close();
    client = builder().credential(() -> token).build();
    CompletableFuture<RealtimeConnection> f = client.async().realtime().connect(params().build());
    assertTrue(f.cancel(true));
    token.complete("late-token");
    assertEquals(0, server.getRequestCount());
    assertFalse(token.isCancelled());
  }

  @Test
  void sharedCredentialIsResolvedForEachExplicitConnection() throws Exception {
    AtomicInteger count = new AtomicInteger();
    client.close();
    client =
        builder()
            .credential(() -> CompletableFuture.completedFuture("sat-" + count.incrementAndGet()))
            .build();
    upgrade(true, false);
    upgrade(true, false);
    try (RealtimeConnection first = connect()) {
      first.receive();
    }
    try (RealtimeConnection second = connect()) {
      second.receive();
    }
    assertEquals("Bearer sat-1", server.takeRequest().getHeader("Authorization"));
    assertEquals("Bearer sat-2", server.takeRequest().getHeader("Authorization"));
  }

  @Test
  void headerWaitIsBounded() {
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    assertThrows(
        ApiTimeoutException.class,
        () -> client.realtime().connect(params().handshakeTimeout(Duration.ofMillis(150)).build()));
  }

  @Test
  void readyWaitIsIndependentOfHandshake() throws Exception {
    upgrade(false, false);
    CompletableFuture<RealtimeConnection> future =
        client.async().realtime().connect(params().readyTimeout(Duration.ofMillis(150)).build());
    peer();
    assertInstanceOf(ApiTimeoutException.class, failed(future));
  }

  @Test
  void cancellationAfterUpgradeReleasesSocket() throws Exception {
    upgrade(false, false);
    CompletableFuture<RealtimeConnection> future =
        client.async().realtime().connect(params().build());
    peer();
    assertTrue(future.cancel(true));
    client.close();
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void receiveTimeoutAndCancellationLeaveConnectionUsable() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      WebSocket ws = peer();
      c.receive();
      assertThrows(ApiTimeoutException.class, () -> c.receive(Duration.ofMillis(100)));
      CompletableFuture<RealtimeEvent> f = c.receiveAsync();
      assertThrows(ConfigurationException.class, c::receiveAsync);
      assertTrue(f.cancel(true));
      send(ws, "pong");
      assertEquals("pong", c.receive(Duration.ofSeconds(2)).type());
      assertFalse(c.isClosed());
    }
  }

  @Test
  void explicitTotalDeadlineAlsoClosesReadyConnection() {
    upgrade(true, false);
    try (RealtimeConnection c =
        client
            .realtime()
            .connect(
                params().build(),
                RequestOptions.builder().totalTimeout(Duration.ofMillis(500)).build())) {
      c.receive();
      assertThrows(ApiTimeoutException.class, c::receive);
      assertTrue(c.isClosed());
    }
  }

  @Test
  void ordinaryClientTimeoutDoesNotEndEstablishedConnection() throws Exception {
    client.close();
    client = builder().pat("p").timeout(Duration.ofMillis(50)).build();
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      c.receive();
      assertThrows(ApiTimeoutException.class, () -> c.receive(Duration.ofMillis(150)));
      send(peer(), "pong");
      assertEquals("pong", c.receive().type());
    }
  }

  @Test
  void clientCloseUnblocksReceive() {
    upgrade(true, false);
    RealtimeConnection c = connect();
    c.receive();
    CompletableFuture<RealtimeEvent> future = c.receiveAsync();
    client.close();
    assertInstanceOf(ApiUserAbortException.class, failed(future));
    assertTrue(c.isClosed());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "provider_initialization_failed",
        "voice_configuration_failed",
        "context_restore_failed",
        "provider_unavailable",
        "service_restarting",
        "event_persistence_failed"
      })
  void fatalServerErrorsKeepDiagnosticPayload(String code) throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      WebSocket ws = peer();
      c.receive();
      ws.send(
          change(
                  fixture("error"),
                  "payload",
                  new LinkedHashMap<String, Object>() {
                    {
                      put("code", code);
                      put("message", "private details");
                    }
                  })
              .toJson());
      RealtimeException e =
          assertThrows(RealtimeException.class, () -> c.receive(Duration.ofSeconds(2)));
      assertEquals(code, e.event().get("payload").get("code").asString());
      assertFalse(e.getMessage().contains("private details"));
      assertEquals("conv_test", e.conversationId());
      assertEquals("req-ws", e.requestId());
    }
  }

  @Test
  void nonfatalServerErrorAndWorkFailureRemainEvents() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      WebSocket ws = peer();
      c.receive();
      send(ws, "error");
      assertEquals("work_busy", c.receive().error().code());
      send(ws, "work.failed");
      assertNotNull(c.receive().work().error());
      assertFalse(c.isClosed());
    }
  }

  @Test
  void errorBeforeReadyFailsConnect() throws Exception {
    upgrade(false, false);
    CompletableFuture<RealtimeConnection> future =
        client.async().realtime().connect(params().build());
    send(peer(), "error");
    assertInstanceOf(RealtimeException.class, failed(future));
  }

  @Test
  void replacedAndAbnormalClosePreserveBufferedEvents() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      WebSocket ws = peer();
      send(ws, "voice.replaced");
      assertEquals("voice.ready", c.receive().type());
      assertThrows(RealtimeException.class, c::receive);
    }
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      WebSocket ws = peer();
      ws.close(1011, "private reason");
      c.receive();
      RealtimeException error = assertThrows(RealtimeException.class, c::receive);
      assertEquals(1011, error.closeCode());
      assertEquals("private reason", error.closeReason());
      assertFalse(error.getMessage().contains("private reason"));
    }
  }

  @Test
  void abruptDisconnectIsNotReconnected() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      c.receive();
      peer();
      server.close();
      RealtimeException e =
          assertThrows(RealtimeException.class, () -> c.receive(Duration.ofSeconds(3)));
      assertEquals(1006, e.closeCode());
      assertEquals("conv_test", e.conversationId());
      assertEquals("req-ws", e.requestId());
      assertInstanceOf(IOException.class, e.getCause());
      assertEquals("Realtime connection failed", e.getMessage());
      assertEquals(1, server.getRequestCount());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void handshakeDisconnectPreservesTransportCause(boolean async) {
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));
    Throwable error =
        async
            ? failed(client.async().realtime().connect(params().build()))
            : assertThrows(RealtimeException.class, this::connect);
    RealtimeException failure = assertInstanceOf(RealtimeException.class, error);
    assertInstanceOf(IOException.class, failure.getCause());
    assertEquals("conv_test", failure.conversationId());
    assertEquals(1006, failure.closeCode());
    assertEquals("Realtime connection failed", failure.getMessage());
  }

  @Test
  void normalCloseReturnsEndOfStream() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      c.receive();
      peer().close(1000, "done");
      assertNull(c.receive());
    }
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"binary", "invalid-json", "wrong-conversation", "wrong-version", "oversized"})
  void invalidFramesTerminateConnection(String kind) throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = client.realtime().connect(params().readLimit(2048).build())) {
      WebSocket ws = peer();
      c.receive();
      switch (kind) {
        case "binary":
          ws.send(ByteString.of((byte) 1));
          break;
        case "invalid-json":
          ws.send("{bad");
          break;
        case "wrong-conversation":
          ws.send(change(fixture("pong"), "conversation_id", "other").toJson());
          break;
        case "wrong-version":
          ws.send(change(fixture("pong"), "version", "v2").toJson());
          break;
        default:
          ws.send(String.join("", Collections.nCopies(2049, "x")));
      }
      assertThrows(ApiException.class, () -> c.receive(Duration.ofSeconds(3)));
      assertTrue(c.isClosed());
    }
  }

  @Test
  void receiveQueueIsBoundedAndPreservesAcceptedEvents() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = client.realtime().connect(params().queueCapacity(1).build())) {
      WebSocket ws = peer();
      send(ws, "pong");
      long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
      while (!c.isClosed() && System.nanoTime() < end) Thread.yield();
      assertTrue(c.isClosed());
      assertEquals("voice.ready", c.receive().type());
      assertThrows(RealtimeException.class, c::receive);
    }
  }

  @Test
  void gracefulCloseRequiresMatchingAcknowledgement() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c =
        client.realtime().connect(params().closeTimeout(Duration.ofSeconds(2)).build())) {
      WebSocket ws = peer();
      c.receive();
      CompletableFuture<RealtimeCloseResult> future = c.closeGracefullyAsync();
      assertSame(future, c.closeGracefullyAsync());
      JsonValue sent = message();
      send(ws, "connection.closed");
      assertEquals("fixture_close", c.receive().closed().requestId());
      assertFalse(future.isDone());
      ws.send(
          change(
                  fixture("connection.closed"),
                  "payload",
                  change(
                      fixture("connection.closed").get("payload"),
                      "request_id",
                      sent.get("payload").get("request_id")))
              .toJson());
      assertEquals("saved", future.get(3, TimeUnit.SECONDS).outcome());
    }
  }

  @Test
  void gracefulCloseTimeoutDoesNotClaimPersistence() {
    upgrade(true, false);
    try (RealtimeConnection c =
        client.realtime().connect(params().closeTimeout(Duration.ofMillis(150)).build())) {
      assertThrows(ApiTimeoutException.class, c::closeGracefully);
      assertTrue(c.isClosed());
    }
  }

  @Test
  void transportCloseWithoutBusinessAckFailsGracefulClose() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      WebSocket ws = peer();
      c.receive();
      CompletableFuture<RealtimeCloseResult> future = c.closeGracefullyAsync();
      message();
      ws.close(1000, "closed");
      assertInstanceOf(RealtimeException.class, failed(future));
    }
  }

  @Test
  void cancelCloseAbortsConnection() {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      CompletableFuture<RealtimeCloseResult> f = c.closeGracefullyAsync();
      f.cancel(true);
      assertTrue(c.isClosed());
    }
  }

  @Test
  void invalidInputNeverSends() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      assertThrows(ConfigurationException.class, () -> c.sendText(" "));
      assertThrows(ConfigurationException.class, () -> c.sendText("\ud800"));
      assertThrows(ConfigurationException.class, () -> c.sendText("\u00a0"));
      assertThrows(
          ConfigurationException.class,
          () -> c.sendText(String.join("", Collections.nCopies(16385, "x"))));
      assertThrows(ConfigurationException.class, () -> c.appendAudio(new byte[3]));
      assertThrows(ConfigurationException.class, () -> c.appendAudio(new byte[0]));
      assertThrows(ConfigurationException.class, () -> c.appendAudio(new byte[262144]));
      assertThrows(
          ConfigurationException.class, () -> c.sendEvent("future", JsonValue.of("not-object")));
      assertThrows(
          ConfigurationException.class,
          () ->
              c.sendEvent(
                  "future",
                  JsonValue.of(
                      Collections.singletonMap(
                          "text", String.join("", Collections.nCopies(262144, "x"))))));
      assertNull(messages.poll(50, TimeUnit.MILLISECONDS));
    }
  }

  @Test
  void slowPeerTriggersSendDeadlineWithoutReplay() throws Exception {
    CountDownLatch release = new CountDownLatch(1);
    server.enqueue(
        new MockResponse()
            .withWebSocketUpgrade(
                new WebSocketListener() {
                  @Override
                  public void onOpen(WebSocket ws, Response r) {
                    allPeers.add(ws);
                    send(ws, "voice.ready");
                    try {
                      release.await(8, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                      Thread.currentThread().interrupt();
                    }
                  }
                }));
    try (RealtimeConnection c =
        client
            .realtime()
            .connect(
                params()
                    .queueCapacity(256)
                    .queueBytes(64 * 1024 * 1024)
                    .sendTimeout(Duration.ofMillis(400))
                    .build())) {
      List<CompletableFuture<Void>> sends = new ArrayList<>();
      byte[] audio = new byte[180000];
      for (int i = 0; i < 200; i++) sends.add(c.appendAudioAsync(audio));
      Throwable error = failed(CompletableFuture.allOf(sends.toArray(new CompletableFuture<?>[0])));
      assertInstanceOf(ApiTimeoutException.class, error);
      assertTrue(c.isClosed());
      assertEquals(1, server.getRequestCount());
    } finally {
      release.countDown();
    }
  }

  @Test
  void receiveByteLimitAlsoBoundsQueue() throws Exception {
    upgrade(true, false);
    int bytes =
        fixture("voice.ready").toJson().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
    try (RealtimeConnection c = client.realtime().connect(params().queueBytes(bytes).build())) {
      WebSocket ws = peer();
      send(ws, "pong");
      long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
      while (!c.isClosed() && System.nanoTime() < end) Thread.yield();
      assertTrue(c.isClosed());
      assertEquals("voice.ready", c.receive().type());
      assertThrows(RealtimeException.class, c::receive);
    }
  }

  @Test
  void gracefulCloseUnavailableLeavesConnectionOpen() throws Exception {
    upgrade(false, false);
    CompletableFuture<RealtimeConnection> future =
        client.async().realtime().connect(params().build());
    WebSocket ws = peer();
    JsonValue ready = fixture("voice.ready");
    ws.send(
        change(
                ready,
                "payload",
                change(
                    ready.get("payload"),
                    "capabilities",
                    Collections.singletonMap("graceful_close", false)))
            .toJson());
    try (RealtimeConnection c = future.get(3, TimeUnit.SECONDS)) {
      assertThrows(ConfigurationException.class, c::closeGracefully);
      assertFalse(c.isClosed());
    }
  }

  @Test
  void customHandshakeHeadersCannotChangeUpgradeTarget() {
    assertInstanceOf(
        ConfigurationException.class,
        failed(
            client
                .async()
                .realtime()
                .connect(
                    params().build(),
                    RequestOptions.builder().header("Host", "different.example").build())));
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .async()
                .realtime()
                .connect(
                    params().build(),
                    RequestOptions.builder().query("conversation_id", "other").build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void cancellingQueuedSendDoesNotSendOrCloseConnection() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      CompletableFuture<Void> first, last;
      synchronized (c) {
        first = c.sendTextAsync("first");
        CompletableFuture<Void> discarded = c.sendTextAsync("discarded");
        assertTrue(discarded.cancel(true));
        last = c.sendTextAsync("last");
      }
      first.get(3, TimeUnit.SECONDS);
      last.get(3, TimeUnit.SECONDS);
      assertEquals("first", message().get("payload").get("text").asString());
      assertEquals("last", message().get("payload").get("text").asString());
      assertFalse(c.isClosed());
      assertNull(messages.poll(50, TimeUnit.MILLISECONDS));
    }
  }

  @Test
  void sendQueueOverflowRejectsOnlyNewSend() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = client.realtime().connect(params().queueCapacity(1).build())) {
      CompletableFuture<Void> first;
      synchronized (c) {
        first = c.sendTextAsync("first");
        assertInstanceOf(RealtimeException.class, failed(c.sendTextAsync("overflow")));
        assertFalse(c.isClosed());
      }
      first.get(3, TimeUnit.SECONDS);
      assertEquals("first", message().get("payload").get("text").asString());
    }
  }

  @Test
  void cancellingStartedSendClosesRatherThanReplays() {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      synchronized (c) {
        assertTrue(c.sendTextAsync("uncertain delivery").cancel(true));
        assertTrue(c.isClosed());
      }
      assertEquals(1, server.getRequestCount());
    }
  }

  @Test
  void concurrentSendsStayOrdered() throws Exception {
    upgrade(true, false);
    try (RealtimeConnection c = connect()) {
      List<CompletableFuture<Void>> futures = new ArrayList<>();
      for (int i = 0; i < 20; i++) futures.add(c.sendTextAsync("message-" + i));
      CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
          .get(5, TimeUnit.SECONDS);
      for (int i = 0; i < 20; i++)
        assertEquals("message-" + i, message().get("payload").get("text").asString());
    }
  }
}
