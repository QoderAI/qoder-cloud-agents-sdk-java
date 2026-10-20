package com.qoder.cloudagents.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.core.internal.Authentication;
import com.qoder.cloudagents.core.internal.Transports;
import com.qoder.cloudagents.errors.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class ServiceAccountCredentialTest {
  private MockWebServer issuer;
  private ServiceAccountCredential credential;
  private AtomicLong nanos;
  private static final String KEY = "test-secret-sa-key";
  private static final String TOKEN = "test-issued-sat";

  @BeforeEach
  void setup() throws Exception {
    issuer = new MockWebServer();
    issuer.start();
    nanos = new AtomicLong();
    credential = create(Duration.ofSeconds(2));
  }

  ServiceAccountCredential create(Duration timeout) {
    OkHttpClient transport = Transports.create();
    return new ServiceAccountCredential(
        ServiceAccountCredential.builder()
            .saKey(KEY)
            .region(Region.GLOBAL)
            .scopes("qca.access", "forward.access", "qca.access")
            .timeout(timeout),
        transport,
        issuer.url("/api/v1/serviceToken/exchange"),
        nanos::get,
        Clock.systemUTC(),
        true);
  }

  @AfterEach
  void cleanup() throws Exception {
    credential.close();
    issuer.close();
  }

  MockResponse issued(String token) {
    return new MockResponse()
        .setBody(
            "{\"access_token\":\""
                + token
                + "\",\"token_type\":\"Bearer\",\"expires_in\":100,\"expires_at\":\"2026-10-16T08:00:00Z\"}");
  }

  void second(int value) {
    nanos.set(TimeUnit.SECONDS.toNanos(value));
  }

  Throwable failure(CompletableFuture<?> value) {
    return assertThrows(ExecutionException.class, () -> value.get(3, TimeUnit.SECONDS)).getCause();
  }

  @Test
  void realtimeExplicitReconnectRefreshesSatAndNeverUsesSaKeyOnBusinessSocket() throws Exception {
    issuer.enqueue(issued("sat-first"));
    issuer.enqueue(issued("sat-second"));
    String raw =
        new String(
            java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get(getClass().getResource("/realtime-events.json").toURI())),
            java.nio.charset.StandardCharsets.UTF_8);
    String ready =
        JsonValue.parse(raw).get("events").asArray().stream()
            .filter(v -> v.get("type").asString().equals("voice.ready"))
            .findFirst()
            .get()
            .toJson();
    try (MockWebServer business = new MockWebServer()) {
      business.start();
      for (int i = 0; i < 2; i++)
        business.enqueue(
            new MockResponse()
                .withWebSocketUpgrade(
                    new okhttp3.WebSocketListener() {
                      @Override
                      public void onOpen(okhttp3.WebSocket socket, okhttp3.Response response) {
                        socket.send(ready);
                      }
                    }));
      try (ForwardClient c =
          ForwardClient.builder()
              .baseUrl(business.url("/api/v1/forward").toString())
              .credential(credential)
              .build()) {
        for (int i = 0; i < 2; i++) {
          try (com.qoder.cloudagents.forward.realtime.RealtimeConnection connection =
              c.realtime()
                  .connect(
                      com.qoder.cloudagents.forward.realtime.RealtimeConnectParams.builder()
                          .conversationId("conv_test")
                          .build())) {
            assertEquals("voice.ready", connection.receive().type());
          }
          second(101);
        }
      }
      assertEquals("Bearer sat-first", business.takeRequest().getHeader("Authorization"));
      assertEquals("Bearer sat-second", business.takeRequest().getHeader("Authorization"));
      assertEquals(2, issuer.getRequestCount());
      assertEquals("Bearer " + KEY, issuer.takeRequest().getHeader("Authorization"));
    }
  }

  @Test
  void sseReconnectRefreshesExpiredSatWithoutSendingKeyToBusinessApi() throws Exception {
    issuer.enqueue(issued("sat-first"));
    issuer.enqueue(issued("sat-second"));
    try (MockWebServer business = new MockWebServer()) {
      business.start();
      business.enqueue(
          new MockResponse()
              .setHeader("Content-Type", "text/event-stream")
              .setBody("id: first\ndata: {\"type\":\"agent.message\"}\n\n"));
      business.enqueue(
          new MockResponse()
              .setHeader("Content-Type", "text/event-stream")
              .setBody("id: end\ndata: {\"type\":\"session.deleted\"}\n\n"));
      try (ForwardClient c =
              ForwardClient.builder()
                  .baseUrl(business.url("/api/v1/forward").toString())
                  .credential(credential)
                  .maxRetries(0)
                  .build();
          com.qoder.cloudagents.streaming.EventStream<com.qoder.cloudagents.forward.SessionEvent>
              stream = c.sessions().events().resumableStream("s")) {
        java.util.Iterator<com.qoder.cloudagents.forward.SessionEvent> events = stream.iterator();
        events.next();
        second(101);
        events.next();
        assertFalse(events.hasNext());
      }
      assertEquals("Bearer sat-first", business.takeRequest().getHeader("Authorization"));
      RecordedRequest resumed = business.takeRequest();
      assertEquals("Bearer sat-second", resumed.getHeader("Authorization"));
      assertEquals("first", resumed.getHeader("Last-Event-ID"));
      assertEquals(2, issuer.getRequestCount());
    }
  }

  @Test
  void exchangeIsLazyConcurrentAndCancellationIsPerCaller() throws Exception {
    assertEquals(0, issuer.getRequestCount());
    issuer.enqueue(issued(TOKEN).setBodyDelay(200, TimeUnit.MILLISECONDS));
    List<CompletableFuture<String>> waiting = new ArrayList<>();
    for (int i = 0; i < 20; i++) waiting.add(credential.getToken());
    waiting.get(0).cancel(true);
    for (int i = 1; i < waiting.size(); i++)
      assertEquals(TOKEN, waiting.get(i).get(3, TimeUnit.SECONDS));
    assertEquals(TOKEN, credential.getToken().get());
    assertEquals(1, issuer.getRequestCount());
    RecordedRequest request = issuer.takeRequest();
    assertEquals("POST", request.getMethod());
    assertEquals("/api/v1/serviceToken/exchange", request.getPath());
    assertEquals("Bearer " + KEY, request.getHeader("Authorization"));
    JsonValue body = JsonValue.parse(request.getBody().readUtf8());
    assertEquals("qoder", body.get("audience").asString());
    assertEquals("client_credentials", body.get("grant_type").asString());
    assertEquals("qca.access forward.access", body.get("scope").asString());
    assertEquals(3600, body.get("ttl_seconds").asLong());
    assertFalse(body.toJson().contains(KEY));
    assertFalse(credential.toString().contains(KEY));
  }

  @Test
  void concurrentThreadsShareOneExchange() throws Exception {
    issuer.enqueue(issued(TOKEN).setBodyDelay(100, TimeUnit.MILLISECONDS));
    ExecutorService callers = Executors.newFixedThreadPool(8);
    try {
      List<Future<String>> values = new ArrayList<>();
      for (int i = 0; i < 24; i++) values.add(callers.submit(() -> credential.getToken().get()));
      for (Future<String> value : values) assertEquals(TOKEN, value.get(3, TimeUnit.SECONDS));
      assertEquals(1, issuer.getRequestCount());
    } finally {
      callers.shutdownNow();
    }
  }

  @Test
  void issuedSatReachesBusinessApiAndClientDoesNotOwnSharedCredential() throws Exception {
    issuer.enqueue(issued(TOKEN));
    try (MockWebServer business = new MockWebServer()) {
      business.start();
      business.enqueue(new MockResponse().setBody("{\"data\":[],\"has_more\":false}"));
      try (ForwardClient client =
          ForwardClient.builder()
              .baseUrl(business.url("/api/v1/forward").toString())
              .credential(credential)
              .build()) {
        client.models().list();
        assertEquals("Bearer " + TOKEN, business.takeRequest().getHeader("Authorization"));
      }
      assertEquals(TOKEN, credential.getToken().get());
    }
  }

  @Test
  void refreshHappensBeforeExpiryAndTemporaryFailureUsesOnlyStillSafeToken() throws Exception {
    issuer.enqueue(issued(TOKEN));
    assertEquals(TOKEN, credential.getToken().get());
    second(89);
    assertEquals(TOKEN, credential.getToken().get());
    assertEquals(1, issuer.getRequestCount());
    second(90);
    issuer.enqueue(new MockResponse().setResponseCode(503).setBody(KEY));
    assertEquals(TOKEN, credential.getToken().get());
    assertEquals(2, issuer.getRequestCount());
    second(94);
    assertEquals(TOKEN, credential.getToken().get());
    assertEquals(2, issuer.getRequestCount());
    second(95);
    issuer.enqueue(new MockResponse().setResponseCode(503).setBody(KEY));
    Throwable error = failure(credential.getToken());
    assertTrue(error instanceof ServiceAccountExchangeException);
    assertFalse(error.toString().contains(KEY));
    second(100);
    issuer.enqueue(issued("fresh-sat"));
    assertEquals("fresh-sat", credential.getToken().get());
  }

  @Test
  void permanentAuthFailureNeverFallsBackToOldTokenOrEchoesIssuerSecrets() throws Exception {
    issuer.enqueue(issued(TOKEN));
    credential.getToken().get();
    second(90);
    issuer.enqueue(
        new MockResponse()
            .setResponseCode(403)
            .setHeader("x-request-id", "req-issuer")
            .setBody("{\"message\":\"" + KEY + " " + TOKEN + "\"}"));
    ServiceAccountExchangeException error =
        (ServiceAccountExchangeException) failure(credential.getToken());
    assertEquals(403, error.statusCode());
    assertEquals("req-issuer", error.requestId());
    assertNull(error.getCause());
    assertFalse(error.toString().contains(KEY));
    assertFalse(error.toString().contains(TOKEN));
    assertSame(error, failure(credential.getToken()));
    assertEquals(2, issuer.getRequestCount());
  }

  @Test
  void rateLimitCooldownRespectsRetryAfterWithoutHiddenExchangeRetries() throws Exception {
    issuer.enqueue(
        new MockResponse().setResponseCode(429).setHeader("Retry-After", "120").setBody("{}"));
    assertTrue(failure(credential.getToken()) instanceof ServiceAccountExchangeException);
    second(119);
    assertTrue(failure(credential.getToken()) instanceof ServiceAccountExchangeException);
    assertEquals(1, issuer.getRequestCount());
    second(120);
    issuer.enqueue(issued(TOKEN));
    assertEquals(TOKEN, credential.getToken().get());
  }

  @Test
  void issuerDenialIsNotTurnedIntoTimeoutOrCachedTokenFallbackBySlowBody() throws Exception {
    credential.close();
    credential = create(Duration.ofMillis(150));
    issuer.enqueue(issued(TOKEN));
    credential.getToken().get();
    second(90);
    issuer.enqueue(
        new MockResponse()
            .setResponseCode(403)
            .setHeader("x-request-id", "echo-" + KEY + "-" + TOKEN)
            .setBody(KEY)
            .setBodyDelay(1, TimeUnit.SECONDS));
    ServiceAccountExchangeException error =
        (ServiceAccountExchangeException) failure(credential.getToken());
    assertEquals(403, error.statusCode());
    assertFalse(error.requestId().contains(KEY));
    assertFalse(error.requestId().contains(TOKEN));
  }

  @Test
  void malformedLifetimeAndTokenValuesAreRejected() {
    String[] bodies = {
      "{\"access_token\":\"token\",\"token_type\":\"Bearer\",\"expires_in\":0}",
      "{\"access_token\":\"token\",\"token_type\":\"Bearer\",\"expires_in\":43201}",
      "{\"access_token\":\"token\",\"token_type\":\"Bearer\",\"expires_in\":1.5}",
      "{\"access_token\":\"bad token\",\"token_type\":\"Bearer\",\"expires_in\":100}",
      "{\"access_token\":\"token\",\"token_type\":\"Bearer\",\"expires_in\":100,\"expires_at\":\"invalid\"}"
    };
    for (int i = 0; i < bodies.length; i++) {
      second(i * 10);
      issuer.enqueue(new MockResponse().setBody(bodies[i]));
      assertTrue(failure(credential.getToken()) instanceof ResponseValidationException);
    }
    assertEquals(bodies.length, issuer.getRequestCount());
  }

  @Test
  void exchangeTimeoutIncludesBody() {
    credential.close();
    credential = create(Duration.ofMillis(150));
    issuer.enqueue(issued(TOKEN).setBodyDelay(1, TimeUnit.SECONDS));
    assertTrue(failure(credential.getToken()) instanceof ApiTimeoutException);
  }

  @Test
  void invalidExchangeResponseIsSanitizedAndNeverUsedAsBearerToken() {
    issuer.enqueue(
        new MockResponse()
            .setBody(
                "{\"access_token\":\"" + KEY + "\",\"token_type\":\"wrong\",\"expires_in\":100}"));
    Throwable error = failure(credential.getToken());
    assertTrue(error instanceof ResponseValidationException);
    assertFalse(error.toString().contains(KEY));
    assertNull(error.getCause());
  }

  @Test
  void redirectsDoNotLeakSaKey() throws Exception {
    try (MockWebServer target = new MockWebServer()) {
      target.start();
      issuer.enqueue(
          new MockResponse().setResponseCode(307).setHeader("Location", target.url("/exchange")));
      assertEquals(
          307, ((ServiceAccountExchangeException) failure(credential.getToken())).statusCode());
      assertEquals(0, target.getRequestCount());
    }
  }

  @Test
  void closeAbortsPendingExchangeAndPreventsFutureCalls() throws Exception {
    issuer.enqueue(issued(TOKEN).setHeadersDelay(1, TimeUnit.SECONDS));
    CompletableFuture<String> pending = credential.getToken();
    assertNotNull(issuer.takeRequest(2, TimeUnit.SECONDS));
    credential.close();
    assertTrue(failure(pending) instanceof ConfigurationException);
    assertTrue(failure(credential.getToken()) instanceof ConfigurationException);
  }

  @Test
  void authModeAndEnvironmentPrecedenceAreExplicit() throws Exception {
    Map<String, String> env = new HashMap<>();
    env.put("QODER_PAT", "environment-pat");
    env.put("QODER_SA_KEY", "ignored-key");
    assertEquals(
        "environment-pat",
        Authentication.resolve(null, null, null, null, "localhost", env::get).getToken().get());
    assertTrue(
        failure(
                Authentication.resolve(AuthMode.SA, null, null, null, "localhost", env::get)
                    .getToken())
            instanceof ConfigurationException);
    env.put("QODER_ACCESS_TOKEN", "direct-sat");
    assertEquals(
        "direct-sat",
        Authentication.resolve(AuthMode.SA, null, null, null, "localhost", env::get)
            .getToken()
            .get());
    assertEquals(
        "direct-sat",
        Authentication.resolve(AuthMode.PAT, null, null, null, "localhost", env::get)
            .getToken()
            .get());
    assertEquals(
        "explicit",
        Authentication.resolve(null, "explicit", null, null, "localhost", env::get)
            .getToken()
            .get());
    assertSame(
        credential,
        Authentication.resolve(null, null, null, credential, "api.qoder.com", env::get));
    assertThrows(
        ConfigurationException.class,
        () -> Authentication.resolve(null, null, null, credential, "api.qoder.com.cn", env::get));
    assertThrows(
        ConfigurationException.class,
        () -> Authentication.resolve(AuthMode.PAT, null, null, credential, "localhost", env::get));
    assertThrows(
        ConfigurationException.class,
        () -> Authentication.resolve(AuthMode.SA, "pat", null, null, "localhost", env::get));
    assertThrows(
        ConfigurationException.class,
        () -> Authentication.resolve(null, "pat", null, credential, "localhost", env::get));
    assertEquals(0, issuer.getRequestCount());
  }

  @Test
  void invalidCredentialOptionsFailBeforeAnyExchange() {
    assertThrows(
        ConfigurationException.class,
        () -> ServiceAccountCredential.builder().saKey(KEY).region(Region.CN).build());
    assertThrows(
        ConfigurationException.class,
        () -> ServiceAccountCredential.builder().saKey(KEY).scopes("forward.access").build());
    assertThrows(
        ConfigurationException.class,
        () ->
            ServiceAccountCredential.builder()
                .saKey(KEY)
                .region(Region.CN)
                .scopes("bad scope")
                .build());
    assertThrows(
        ConfigurationException.class,
        () ->
            ServiceAccountCredential.builder()
                .saKey(KEY)
                .region(Region.CN)
                .scopes("forward.access")
                .ttlSeconds(43201)
                .build());
    assertFalse(new PATCredential(KEY).toString().contains(KEY));
    assertEquals(0, issuer.getRequestCount());
  }
}
