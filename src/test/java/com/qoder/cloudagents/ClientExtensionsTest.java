package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.http.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import com.qoder.cloudagents.streaming.EventStream;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class ClientExtensionsTest {
  private MockWebServer server;

  @BeforeEach
  void start() throws Exception {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void stop() throws Exception {
    server.close();
  }

  ForwardClient.Builder builder() {
    return ForwardClient.builder()
        .baseUrl(server.url("/api/v1/forward").toString())
        .pat("test-pat")
        .maxRetries(0);
  }

  static HttpResponse response(int code, String body) {
    return new HttpResponse(
        code,
        Collections.singletonMap("content-type", Collections.singletonList("application/json")),
        new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
  }

  static CompletableFuture<HttpResponse> json(String body) {
    return CompletableFuture.completedFuture(response(200, body));
  }

  static ApiRequest get() {
    return ApiRequest.builder("GET", "models").build();
  }

  @Test
  void genericJsonUsesAuthOptionsMetadataAndAsyncErrorMapping() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody("{\"value\":9007199254740993}")
            .setHeader("X-Request-ID", "req_generic"));
    server.enqueue(
        new MockResponse()
            .setResponseCode(403)
            .setBody("{\"error\":{\"code\":\"denied\",\"message\":\"No\"}}"));
    try (ForwardClient client = builder().build()) {
      ApiResponse<JsonValue> reply =
          client.request(
              ApiRequest.builder("POST", "new-resource", "id +")
                  .body(JsonValue.parse("{\"enabled\":false,\"limit\":0,\"clear\":null}"))
                  .build(),
              RequestOptions.builder()
                  .idempotencyKey("generic-key")
                  .query("filter", "a+b")
                  .build());
      assertEquals(9007199254740993L, reply.data().get("value").asLong());
      assertEquals("req_generic", reply.requestId());
      RecordedRequest wire = server.takeRequest();
      assertEquals("Bearer test-pat", wire.getHeader("Authorization"));
      assertEquals("generic-key", wire.getHeader("Idempotency-Key"));
      assertEquals("/api/v1/forward/new-resource/id%20+", wire.getRequestUrl().encodedPath());
      assertEquals("a+b", wire.getRequestUrl().queryParameter("filter"));
      assertTrue(JsonValue.parse(wire.getBody().readUtf8()).get("clear").isNull());
      ExecutionException error =
          assertThrows(
              ExecutionException.class,
              () -> client.async().request(get()).get(3, TimeUnit.SECONDS));
      assertEquals(403, ((ApiStatusException) error.getCause()).statusCode());
    }
  }

  @Test
  void genericPathsCannotEscapeTheBaseAndHeadAllowsAnEmptyBody() {
    for (String path :
        Arrays.asList("https://other.test", "//other.test", "..", ".", "a/b", "a?b", "a#b", "a\\b"))
      assertThrows(ConfigurationException.class, () -> ApiRequest.builder("GET", path));
    assertThrows(
        ConfigurationException.class,
        () -> ApiRequest.builder("GET", "x").body(JsonValue.of(null)));
    server.enqueue(new MockResponse().setResponseCode(200));
    try (ForwardClient client = builder().build()) {
      assertTrue(client.request(ApiRequest.builder("HEAD", "models").build()).data().isNull());
    }
  }

  @Test
  void genericRequestsPreserveSafeRetryRules() {
    AtomicInteger calls = new AtomicInteger();
    HttpTransport failing =
        request -> {
          calls.incrementAndGet();
          return CompletableFuture.completedFuture(response(503, "{}"));
        };
    try (ForwardClient client = builder().transport(failing).maxRetries(1).build()) {
      assertThrows(
          ApiStatusException.class,
          () -> client.request(ApiRequest.builder("POST", "actions").build()));
      assertEquals(1, calls.get());
      assertThrows(
          ApiStatusException.class,
          () ->
              client.request(
                  ApiRequest.builder("POST", "actions").build(),
                  RequestOptions.builder().idempotencyKey("safe-to-retry").build()));
      assertEquals(3, calls.get());
      assertThrows(ApiStatusException.class, () -> client.request(get()));
      assertEquals(5, calls.get());
    }
  }

  @Test
  void interceptorsWrapEachDefaultTransportAttemptInOrder() throws Exception {
    server.enqueue(
        new MockResponse().setResponseCode(503).setBody("{}").setHeader("Retry-After-Ms", "1"));
    server.enqueue(new MockResponse().setBody("{\"data\":[]}"));
    List<String> seen = new CopyOnWriteArrayList<>();
    HttpInterceptor first =
        (request, chain) -> {
          seen.add("first:" + request.header("X-Qoder-Retry-Count"));
          return chain.proceed(request.withHeader("X-Trace", "trace-1"));
        };
    HttpInterceptor second =
        (request, chain) -> {
          seen.add("second:" + request.header("X-Trace"));
          return chain.proceed(request);
        };
    try (ForwardClient client =
        builder().maxRetries(1).interceptor(first).interceptor(second).build()) {
      client.models().list();
      assertEquals(Arrays.asList("first:0", "second:trace-1", "first:1", "second:trace-1"), seen);
      assertEquals("Bearer test-pat", server.takeRequest().getHeader("Authorization"));
      assertEquals("trace-1", server.takeRequest().getHeader("X-Trace"));
    }
  }

  @Test
  void interceptorCannotRewriteRoutingOrReplayOneAttempt() {
    AtomicInteger calls = new AtomicInteger();
    HttpTransport transport =
        request -> {
          calls.incrementAndGet();
          return json("{}");
        };
    try (ForwardClient client =
        builder()
            .transport(transport)
            .interceptor(
                (request, chain) ->
                    chain.proceed(
                        new HttpRequest(
                            request.method(),
                            java.net.URI.create("https://other.test"),
                            request.headers(),
                            request.body(),
                            request.writeTimeout(),
                            false)))
            .build()) {
      assertThrows(ApiConnectionException.class, () -> client.request(get()));
      assertEquals(0, calls.get());
    }
    try (ForwardClient client =
        builder()
            .transport(transport)
            .interceptor(
                (request, chain) -> {
                  chain.proceed(request);
                  return chain.proceed(request);
                })
            .build()) {
      assertThrows(ApiConnectionException.class, () -> client.request(get()));
      assertEquals(1, calls.get());
    }
  }

  @Test
  void customTransportGetsStreamingBodyAndResolvedCredentialOnEachAttempt() throws Exception {
    AtomicInteger tokens = new AtomicInteger();
    List<HttpRequest> seen = new CopyOnWriteArrayList<>();
    HttpTransport transport =
        request -> {
          seen.add(request);
          return json("{\"id\":\"tmpl_example\"}");
        };
    try (ForwardClient client =
        ForwardClient.builder()
            .credential(
                () -> CompletableFuture.completedFuture("token-" + tokens.incrementAndGet()))
            .transport(transport)
            .build()) {
      client
          .templates()
          .create(
              TemplateCreateParams.builder()
                  .name("template")
                  .modelId("auto")
                  .environmentId("env_example")
                  .build());
      client.async().templates().retrieve("tmpl_example").get(3, TimeUnit.SECONDS);
      assertEquals("Bearer token-1", seen.get(0).header("Authorization"));
      assertEquals("Bearer token-2", seen.get(1).header("Authorization"));
      assertEquals(Duration.ofSeconds(5), seen.get(0).connectTimeout());
      assertTrue(seen.get(0).body().replayable());
      ByteArrayOutputStream body = new ByteArrayOutputStream();
      seen.get(0).body().writeTo(body);
      assertEquals("template", JsonValue.parse(body.toString("UTF-8")).get("name").asString());
      assertFalse(seen.get(0).toString().contains("token-1"));
    }
  }

  @Test
  void interceptorFailureClosesTheReceivedBodyBeforeRetry() {
    assertResponseCleanupBeforeRetry(false);
  }

  @Test
  void interceptorReplacementClosesBothBodiesBeforeRetry() {
    assertResponseCleanupBeforeRetry(true);
  }

  private void assertResponseCleanupBeforeRetry(boolean replace) {
    AtomicInteger receivedCloses = new AtomicInteger();
    AtomicInteger replacementCloses = new AtomicInteger();
    AtomicInteger attempts = new AtomicInteger();
    AtomicBoolean first = new AtomicBoolean(true);
    AtomicBoolean cleanedBeforeRetry = new AtomicBoolean();
    HttpResponse received = closingResponse(200, receivedCloses);
    HttpResponse replacement = closingResponse(503, replacementCloses);
    try (ForwardClient client =
        builder()
            .maxRetries(1)
            .transport(
                request -> {
                  if (attempts.incrementAndGet() == 1)
                    return CompletableFuture.completedFuture(received);
                  cleanedBeforeRetry.set(
                      receivedCloses.get() == 1 && (!replace || replacementCloses.get() == 1));
                  return json("{\"data\":[]}");
                })
            .interceptor(
                (request, chain) ->
                    chain
                        .proceed(request)
                        .thenApply(
                            response -> {
                              if (first.getAndSet(false)) {
                                if (replace) return replacement;
                                throw new IllegalStateException(
                                    "Interceptor failed after receiving headers");
                              }
                              return response;
                            }))
            .build()) {
      client.request(get());
      assertEquals(2, attempts.get());
      assertTrue(cleanedBeforeRetry.get());
      assertEquals(1, receivedCloses.get());
      assertEquals(replace ? 1 : 0, replacementCloses.get());
    }
  }

  private static HttpResponse closingResponse(int status, AtomicInteger closes) {
    return new HttpResponse(
        status,
        Collections.emptyMap(),
        new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)) {
          @Override
          public void close() {
            closes.incrementAndGet();
          }
        });
  }

  @Test
  void cancellationPropagatesThroughDerivedInterceptorFutures() throws Exception {
    CountDownLatch entered = new CountDownLatch(1);
    CompletableFuture<HttpResponse> network = new CompletableFuture<>();
    try (ForwardClient client =
        builder()
            .transport(
                request -> {
                  entered.countDown();
                  return network;
                })
            .interceptor((request, chain) -> chain.proceed(request).thenApply(value -> value))
            .build()) {
      CompletableFuture<?> pending = client.async().request(get());
      assertTrue(entered.await(2, TimeUnit.SECONDS));
      pending.cancel(true);
      assertTrue(network.isCancelled());
    }
  }

  @Test
  void headerTimeoutCancelsCustomIo() throws Exception {
    CompletableFuture<HttpResponse> network = new CompletableFuture<>();
    try (ForwardClient client =
        builder().transport(request -> network).timeout(Duration.ofMillis(100)).build()) {
      assertThrows(ApiTimeoutException.class, () -> client.request(get()));
      assertTrue(network.isCancelled());
    }
  }

  @Test
  void lateCustomResponseAfterCancellationIsClosed() throws Exception {
    CountDownLatch entered = new CountDownLatch(1);
    AtomicBoolean closed = new AtomicBoolean();
    CompletableFuture<HttpResponse> network =
        new CompletableFuture<HttpResponse>() {
          @Override
          public boolean cancel(boolean interrupt) {
            return false;
          }
        };
    try (ForwardClient client =
        builder()
            .transport(
                request -> {
                  entered.countDown();
                  return network;
                })
            .build()) {
      CompletableFuture<?> pending = client.async().request(get());
      assertTrue(entered.await(2, TimeUnit.SECONDS));
      pending.cancel(true);
      network.complete(
          new HttpResponse(
              200,
              Collections.emptyMap(),
              new ByteArrayInputStream(new byte[0]) {
                @Override
                public void close() {
                  closed.set(true);
                }
              }));
      assertTrue(closed.get());
    }
  }

  @Test
  void customTransportPreservesSseAndClosesItsBody() throws Exception {
    AtomicBoolean closed = new AtomicBoolean();
    AtomicReference<HttpRequest> seen = new AtomicReference<>();
    byte[] bytes =
        "id: checkpoint\ndata: {\"id\":\"evt_1\",\"type\":\"assistant.message\",\"text\":\"hello\"}\n\n"
            .getBytes(StandardCharsets.UTF_8);
    try (ForwardClient client =
        builder()
            .transport(
                request -> {
                  seen.set(request);
                  return CompletableFuture.completedFuture(
                      new HttpResponse(
                          200,
                          Collections.singletonMap(
                              "content-type", Collections.singletonList("text/event-stream")),
                          new ByteArrayInputStream(bytes) {
                            @Override
                            public void close() {
                              closed.set(true);
                            }
                          }));
                })
            .build()) {
      try (EventStream<SessionEvent> events =
          client.sessions().events().streamEvents("sess_example")) {
        assertEquals("hello", events.iterator().next().text());
        assertEquals(Duration.ZERO, seen.get().readTimeout());
        assertEquals("Bearer test-pat", seen.get().header("Authorization"));
      }
      assertTrue(closed.get());
    }
  }

  @Test
  void storageDownloadUsesCustomTransportWithoutApiHeadersOrInterceptors() throws Exception {
    List<HttpRequest> requests = new CopyOnWriteArrayList<>();
    AtomicInteger intercepted = new AtomicInteger();
    try (ForwardClient client =
        builder()
            .defaultHeader("X-Private", "private")
            .transport(
                request -> {
                  requests.add(request);
                  return request.uri().getHost().equals("storage.test")
                      ? json("file-bytes")
                      : json("{\"url\":\"https://storage.test/blob?sig=grant\"}");
                })
            .interceptor(
                (request, chain) -> {
                  intercepted.incrementAndGet();
                  return chain.proceed(request.withHeader("X-Trace", "trace"));
                })
            .build()) {
      try (FileDownload download = client.files().download("file_example")) {
        assertEquals('f', download.body().read());
      }
      assertEquals(2, requests.size());
      assertEquals(1, intercepted.get());
      assertNull(requests.get(1).header("Authorization"));
      assertNull(requests.get(1).header("X-Private"));
      assertNull(requests.get(1).header("X-Trace"));
    }
  }

  @Test
  void clientCopiesHaveIndependentDefaultsAuthAndLifetimes() throws Exception {
    List<HttpRequest> seen = new CopyOnWriteArrayList<>();
    HttpTransport transport =
        request -> {
          seen.add(request);
          return json("{}");
        };
    ForwardClient original =
        builder().defaultHeader("X-Default", "original").transport(transport).build();
    try (ForwardClient copy =
            original.withOptions(
                options ->
                    options.timeout(Duration.ofSeconds(2)).defaultHeader("X-Default", "copy"));
        AsyncForwardClient other =
            original
                .async()
                .withOptions(options -> options.authMode(AuthMode.SA).accessToken("test-sat"))) {
      original.request(get());
      copy.request(get());
      original.close();
      copy.request(get());
      other.request(get()).get(3, TimeUnit.SECONDS);
      assertEquals("original", seen.get(0).header("X-Default"));
      assertEquals(Duration.ofMinutes(10), seen.get(0).writeTimeout());
      assertEquals("copy", seen.get(1).header("X-Default"));
      assertEquals(Duration.ofSeconds(2), seen.get(1).writeTimeout());
      assertEquals("Bearer test-pat", seen.get(2).header("Authorization"));
      assertEquals("Bearer test-sat", seen.get(3).header("Authorization"));
    } finally {
      original.close();
    }
  }

  @Test
  void closingCopyDoesNotAffectParentOrSharedCredential() {
    AtomicInteger lookups = new AtomicInteger();
    try (ForwardClient client =
        ForwardClient.builder()
            .credential(
                () -> {
                  lookups.incrementAndGet();
                  return CompletableFuture.completedFuture("custom-pat");
                })
            .transport(request -> json("{}"))
            .build()) {
      try (ForwardClient copy = client.withOptions(options -> options.maxRetries(1))) {
        copy.request(get());
      }
      client.request(get());
      assertEquals(2, lookups.get());
      try (ForwardClient replaced = client.withOptions(options -> options.pat("replacement"))) {
        replaced.request(get());
      }
      assertEquals(2, lookups.get());
    }
  }

  @Test
  void copyRetainsExplicitPatSemanticsAndAllowsCollectionReplacement() {
    AtomicInteger intercepted = new AtomicInteger();
    List<HttpRequest> requests = new CopyOnWriteArrayList<>();
    try (ForwardClient parent =
        builder()
            .defaultHeader("X-Original", "value")
            .transport(
                request -> {
                  requests.add(request);
                  return json("{}");
                })
            .interceptor(
                (request, chain) -> {
                  intercepted.incrementAndGet();
                  return chain.proceed(request);
                })
            .build()) {
      assertThrows(
          ConfigurationException.class,
          () -> parent.withOptions(options -> options.authMode(AuthMode.SA)));
      try (ForwardClient copy =
          parent.withOptions(options -> options.clearInterceptors().clearDefaultHeaders())) {
        copy.request(get());
        assertEquals(0, intercepted.get());
        assertNull(requests.get(0).header("X-Original"));
      }
      parent.request(get());
      assertEquals(1, intercepted.get());
      assertEquals("value", requests.get(1).header("X-Original"));
    }
  }

  @Test
  void sseResumptionUsesTransportAndInterceptorsForEachConnection() {
    List<HttpRequest> requests = new CopyOnWriteArrayList<>();
    AtomicInteger intercepted = new AtomicInteger();
    try (ForwardClient client =
            builder()
                .transport(
                    request -> {
                      requests.add(request);
                      String id = requests.size() == 1 ? "first" : "second";
                      return CompletableFuture.completedFuture(
                          new HttpResponse(
                              200,
                              Collections.singletonMap(
                                  "content-type", Collections.singletonList("text/event-stream")),
                              new ByteArrayInputStream(
                                  ("id: "
                                          + id
                                          + "\ndata: {\"id\":\"evt_"
                                          + id
                                          + "\",\"type\":\"assistant.message\"}\n\n")
                                      .getBytes(StandardCharsets.UTF_8))));
                    })
                .interceptor(
                    (request, chain) -> {
                      intercepted.incrementAndGet();
                      return chain.proceed(request);
                    })
                .build();
        EventStream<SessionEvent> stream =
            client.sessions().events().resumableStream("sess_example")) {
      Iterator<SessionEvent> events = stream.iterator();
      assertEquals("evt_first", events.next().id());
      assertEquals("evt_second", events.next().id());
      assertEquals("first", requests.get(1).header("Last-Event-ID"));
      assertEquals(2, intercepted.get());
    }
  }

  @Test
  void satManagementRetainsItsPerCallKeyAndZeroRetries() {
    AtomicInteger calls = new AtomicInteger();
    AtomicInteger businessTokens = new AtomicInteger();
    try (ForwardClient client =
        ForwardClient.builder()
            .credential(
                () -> {
                  businessTokens.incrementAndGet();
                  return CompletableFuture.completedFuture("business-sat");
                })
            .maxRetries(5)
            .transport(
                request -> {
                  calls.incrementAndGet();
                  assertEquals("Bearer test-sa-key", request.header("Authorization"));
                  return CompletableFuture.completedFuture(response(503, "{}"));
                })
            .build()) {
      assertThrows(
          ApiStatusException.class,
          () ->
              client
                  .serviceAccountTokens()
                  .create(
                      "test-sa-key",
                      ServiceAccountTokenCreateParams.builder().build(),
                      RequestOptions.builder().maxRetries(8).build()));
      assertEquals(1, calls.get());
      assertEquals(0, businessTokens.get());
    }
  }

  @Test
  void defaultTransportStillSuppressesHiddenRetriesWhenIntercepted() throws Exception {
    server.enqueue(
        new MockResponse().setResponseCode(503).setHeader("Retry-After", "0").setBody("{}"));
    server.enqueue(new MockResponse().setBody("{}"));
    try (ForwardClient client =
        builder().interceptor((request, chain) -> chain.proceed(request)).build()) {
      assertThrows(ApiStatusException.class, () -> client.request(get()));
      assertEquals(1, server.getRequestCount());
    }
  }

  @Test
  void closeCancelsPendingCustomIoButDoesNotCloseTransportObject() throws Exception {
    CountDownLatch entered = new CountDownLatch(1);
    CompletableFuture<HttpResponse> network = new CompletableFuture<>();
    class OwnedTransport implements HttpTransport, AutoCloseable {
      boolean closed;

      public CompletableFuture<HttpResponse> execute(HttpRequest request) {
        entered.countDown();
        return network;
      }

      public void close() {
        closed = true;
      }
    }
    OwnedTransport transport = new OwnedTransport();
    ForwardClient client = builder().transport(transport).build();
    try {
      CompletableFuture<?> result = client.async().request(get());
      assertTrue(entered.await(2, TimeUnit.SECONDS));
      client.close();
      assertTrue(network.isCancelled());
      assertTrue(result.isCompletedExceptionally());
      assertFalse(transport.closed);
    } finally {
      client.close();
      transport.close();
    }
  }
}
