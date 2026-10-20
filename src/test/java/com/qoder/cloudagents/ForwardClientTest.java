package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class ForwardClientTest {
  private MockWebServer server;
  private ForwardClient client;
  private static final String MODELS =
      "{\"data\":[{\"id\":\"auto\",\"display_name\":\"Auto\",\"is_enabled\":true,"
          + "\"price_factor\":0.1234567890123456789,\"future_field\":{\"large\":9007199254740993}}],\"has_more\":false}";

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.start();
    client = builder().maxRetries(0).build();
  }

  @AfterEach
  void cleanup() throws Exception {
    client.close();
    server.close();
  }

  ForwardClient.Builder builder() {
    return ForwardClient.builder()
        .baseUrl(server.url("/api/v1/forward").toString())
        .pat("test-pat");
  }

  MockResponse json(String value) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(value);
  }

  @Test
  void modelContractPreservesPrecisionAndRequestMetadata() throws Exception {
    server.enqueue(json(MODELS).setHeader("X-Request-ID", "req-models"));
    ApiResponse<ModelListResponse> response = client.models().withResponse().list();
    Model model = response.data().data().get(0);
    assertEquals("auto", model.id());
    assertEquals("Auto", model.displayName());
    assertTrue(model.isEnabled());
    assertEquals(new BigDecimal("0.1234567890123456789"), model.priceFactor());
    assertEquals(9007199254740993L, model.field("future_field").get("large").asLong());
    assertEquals(200, response.statusCode());
    assertEquals("req-models", model.requestId());
    assertEquals("req-models", response.data().requestId());
    assertFalse(response.data().hasMore());
    assertFalse(model.rawJson().contains("req-models"));
    assertThrows(UnsupportedOperationException.class, () -> response.headers().clear());
    RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(request);
    assertEquals("/api/v1/forward/models", request.getPath());
    assertEquals("Bearer test-pat", request.getHeader("Authorization"));
    assertEquals("java", request.getHeader("X-Qoder-Lang"));
    assertEquals("0", request.getHeader("X-Qoder-Retry-Count"));
  }

  @Test
  void asyncTemplateQueriesAndOpaquePathIdsAreEncoded() throws Exception {
    server.enqueue(
        json(
            "{\"data\":[{\"id\":\"tmpl_1\",\"status\":\"future-status\"}],\"has_more\":false,\"first_id\":\"tmpl_1\",\"last_id\":\"tmpl_1\"}"));
    TemplateListResponse page =
        client
            .async()
            .templates()
            .list(
                TemplateListParams.builder().limit(2).status("active").afterId("cursor +/").build())
            .get(3, TimeUnit.SECONDS);
    assertEquals("future-status", page.data().get(0).status());
    assertEquals("tmpl_1", page.lastId());
    RecordedRequest list = server.takeRequest(2, TimeUnit.SECONDS);
    assertEquals("/api/v1/forward/templates", list.getRequestUrl().encodedPath());
    assertEquals("cursor +/", list.getRequestUrl().queryParameter("after_id"));
    assertEquals("2", list.getRequestUrl().queryParameter("limit"));
    server.enqueue(
        json("{\"id\":\"a/b"
                + " ?#%\",\"name\":\"Template\",\"managed_tool_config\":{\"future_tool\":true}}")
            .setHeader("request-id", "req-template"));
    Template template = client.async().templates().retrieve("a/b ?#%").get(3, TimeUnit.SECONDS);
    assertEquals("a/b ?#%", template.id());
    assertEquals("req-template", template.requestId());
    assertTrue(template.managedToolConfig().field("future_tool").asBoolean());
    assertEquals(
        "/api/v1/forward/templates/a%2Fb%20%3F%23%25",
        server.takeRequest().getRequestUrl().encodedPath());
  }

  @Test
  void apiStatusRetainsStructuredErrorAndRequestId() {
    server.enqueue(
        json("{\"error\":{\"code\":\"not_found\",\"type\":\"resource_error\",\"message\":\"detail\"}}")
            .setResponseCode(404)
            .setHeader("x-request-id", "req-error"));
    ApiStatusException error =
        assertThrows(ApiStatusException.class, () -> client.templates().retrieve("missing"));
    assertEquals(404, error.statusCode());
    assertEquals("not_found", error.code());
    assertEquals("req-error", error.requestId());
    assertEquals("resource_error", error.type());
    assertTrue(error instanceof ApiException);
    assertFalse(error.getMessage().contains("test-pat"));
  }

  @Test
  void invalidSuccessfulJsonIsNotTreatedAsEmptyPage() {
    server.enqueue(json("broken"));
    assertThrows(ResponseValidationException.class, () -> client.models().list());
    server.enqueue(json("{\"data\":{},\"has_more\":false}"));
    assertThrows(ResponseValidationException.class, () -> client.models().list());
    assertThrows(ResponseValidationException.class, () -> JsonValue.parse("{} {}"));
    assertTrue(JsonValue.parse("{\"nullable\":null}").get("nullable").isNull());
    assertTrue(JsonValue.parse("{}").get("nullable").isMissing());
  }

  @Test
  void redirectsNeverForwardAuthorization() throws Exception {
    try (MockWebServer target = new MockWebServer()) {
      target.start();
      server.enqueue(
          new MockResponse().setResponseCode(302).setHeader("Location", target.url("/leak")));
      assertEquals(
          302, assertThrows(ApiStatusException.class, () -> client.models().list()).statusCode());
      assertEquals(0, target.getRequestCount());
    }
  }

  @Test
  void headerAndBodyWaitsRespectTimeout() {
    RequestOptions options = RequestOptions.builder().timeout(Duration.ofMillis(150)).build();
    server.enqueue(json(MODELS).setHeadersDelay(1, TimeUnit.SECONDS));
    assertThrows(ApiTimeoutException.class, () -> client.models().list(options));
    server.enqueue(json(MODELS).setBodyDelay(1, TimeUnit.SECONDS));
    assertThrows(ApiTimeoutException.class, () -> client.models().list(options));
  }

  @Test
  void totalDeadlineIncludesCredentialWaitWithoutCancellingSharedProvider() throws Exception {
    CompletableFuture<String> shared = new CompletableFuture<>();
    try (ForwardClient waiting =
        ForwardClient.builder()
            .baseUrl(server.url("/").toString())
            .credential(() -> shared)
            .build()) {
      CompletableFuture<ModelListResponse> result =
          waiting
              .async()
              .models()
              .list(RequestOptions.builder().totalTimeout(Duration.ofMillis(100)).build());
      ExecutionException error =
          assertThrows(ExecutionException.class, () -> result.get(3, TimeUnit.SECONDS));
      assertTrue(error.getCause() instanceof ApiTimeoutException);
      assertFalse(shared.isCancelled());
      shared.complete("later-token");
      assertEquals(0, server.getRequestCount());
    }
  }

  @Test
  void cancellingFutureStopsMappedRequestAndLeavesClientUsable() throws Exception {
    server.enqueue(json(MODELS).setHeadersDelay(1, TimeUnit.SECONDS));
    CompletableFuture<ModelListResponse> result = client.async().models().list();
    assertNotNull(server.takeRequest(2, TimeUnit.SECONDS));
    assertTrue(result.cancel(true));
    assertThrows(CancellationException.class, result::join);
    server.enqueue(json(MODELS));
    assertEquals("auto", client.models().list().data().get(0).id());
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void cancellationClosesTheActualNetworkSocket() throws Exception {
    try (java.net.ServerSocket socket =
            new java.net.ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress());
        ForwardClient direct =
            ForwardClient.builder()
                .baseUrl("http://localhost:" + socket.getLocalPort())
                .pat("test-pat")
                .maxRetries(0)
                .build()) {
      socket.setSoTimeout(3000);
      CompletableFuture<?> request = direct.async().models().list();
      try (java.net.Socket peer = socket.accept()) {
        peer.setSoTimeout(3000);
        java.io.BufferedReader reader =
            new java.io.BufferedReader(
                new java.io.InputStreamReader(
                    peer.getInputStream(), java.nio.charset.StandardCharsets.US_ASCII));
        String line;
        do {
          line = reader.readLine();
          assertNotNull(line);
        } while (!line.isEmpty());
        request.cancel(true);
        assertEquals(
            -1,
            peer.getInputStream().read(),
            "cancel must close the socket, not only complete the future");
      }
    }
  }

  @Test
  void synchronousInterruptionCancelsAndPreservesInterruptFlag() throws Exception {
    server.enqueue(json(MODELS).setHeadersDelay(1, TimeUnit.SECONDS));
    AtomicReference<Throwable> failure = new AtomicReference<>();
    AtomicBoolean interrupted = new AtomicBoolean();
    Thread thread =
        new Thread(
            () -> {
              try {
                client.models().list();
              } catch (Throwable error) {
                failure.set(error);
                interrupted.set(Thread.currentThread().isInterrupted());
              }
            });
    thread.start();
    assertNotNull(server.takeRequest(2, TimeUnit.SECONDS));
    thread.interrupt();
    thread.join(2000);
    assertFalse(thread.isAlive());
    assertTrue(failure.get() instanceof ApiUserAbortException);
    assertTrue(interrupted.get());
  }

  @Test
  void closingParentCancelsAsyncViewButClosingViewDoesNotCloseParent() throws Exception {
    client.async().close();
    server.enqueue(json(MODELS));
    client.models().list();
    server.takeRequest();
    server.enqueue(json(MODELS).setHeadersDelay(1, TimeUnit.SECONDS));
    CompletableFuture<ModelListResponse> result = client.async().models().list();
    server.takeRequest();
    client.close();
    assertTrue(
        assertThrows(ExecutionException.class, result::get).getCause()
            instanceof ApiUserAbortException);
    assertThrows(ConfigurationException.class, () -> client.models().list());
  }

  @Test
  void queuedRequestTimeoutDoesNotWaitForOtherCallsToFinish() throws Exception {
    for (int i = 0; i < 5; i++) {
      server.enqueue(json(MODELS).setHeadersDelay(3, TimeUnit.SECONDS));
      client.async().models().list();
    }
    for (int i = 0; i < 5; i++) assertNotNull(server.takeRequest(2, TimeUnit.SECONDS));
    CompletableFuture<?> queued =
        client
            .async()
            .models()
            .list(RequestOptions.builder().timeout(Duration.ofMillis(100)).build());
    ExecutionException error =
        assertThrows(ExecutionException.class, () -> queued.get(1, TimeUnit.SECONDS));
    assertTrue(error.getCause() instanceof ApiTimeoutException);
    assertEquals(5, server.getRequestCount());
  }

  @Test
  void independentlyBuiltAsyncClientOwnsItsRequests() throws Exception {
    try (AsyncForwardClient async = builder().buildAsync()) {
      server.enqueue(json(MODELS));
      assertEquals("auto", async.models().list().get().data().get(0).id());
    }
  }

  @Test
  void invalidHeadersAndExplicitNullCredentialsFailWithoutNetworkOrSecretEcho() {
    assertThrows(
        ConfigurationException.class, () -> RequestOptions.builder().header("X Header", "secret"));
    ConfigurationException error =
        assertThrows(
            ConfigurationException.class,
            () -> RequestOptions.builder().header("X-Test", "secret\nvalue"));
    assertFalse(error.getMessage().contains("secret"));
    assertThrows(
        ConfigurationException.class,
        () -> RequestOptions.builder().header("authorization", "secret"));
    assertThrows(ConfigurationException.class, () -> builder().accessToken(null));
    assertThrows(
        ConfigurationException.class,
        () -> TemplateListParams.builder().afterId("a").beforeId("b").build());
    assertThrows(ConfigurationException.class, () -> client.templates().retrieve(".."));
    assertEquals(0, server.getRequestCount());
  }
}
