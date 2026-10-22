package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class WebhooksTest {
  MockWebServer api;
  ForwardClient client;

  @BeforeEach
  void setup() throws Exception {
    api = new MockWebServer();
    api.start();
    client =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward/").toString())
            .authMode(AuthMode.SA)
            .accessToken("test-sat")
            .maxRetries(0)
            .build();
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    api.close();
  }

  MockResponse json(String body, int status) {
    return new MockResponse()
        .setResponseCode(status)
        .setHeader("Content-Type", "application/json")
        .setHeader("X-Request-Id", "req-webhook")
        .setBody(body);
  }

  static Stream<Arguments> routes() {
    return Stream.of(
        Arguments.of(
            "create",
            "POST",
            "/api/v1/forward/webhook/endpoints",
            "{\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\"],\"metadata\":{\"team\":\"sdk\"}}",
            "{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"signing_secret\":\"whsec_MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=\"}",
            201,
            false),
        Arguments.of(
            "create",
            "POST",
            "/api/v1/forward/webhook/endpoints",
            "{\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\"],\"metadata\":{\"team\":\"sdk\"}}",
            "{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"signing_secret\":\"whsec_MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=\"}",
            201,
            true),
        Arguments.of(
            "list",
            "GET",
            "/api/v1/forward/webhook/endpoints",
            "",
            "{\"data\":[{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"updated_at\":\"2026-10-22T00:01:00Z\",\"last_success_at\":null,\"last_failure_at\":\"2026-10-22T00:01:00Z\",\"consecutive_fail\":0,\"disabled_reason\":\"manual\",\"future_field\":{\"preserved\":true}}],\"first_id\":\"endpoint_1\",\"last_id\":\"endpoint_1\",\"has_more\":false}",
            200,
            false),
        Arguments.of(
            "list",
            "GET",
            "/api/v1/forward/webhook/endpoints",
            "",
            "{\"data\":[{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"updated_at\":\"2026-10-22T00:01:00Z\",\"last_success_at\":null,\"last_failure_at\":\"2026-10-22T00:01:00Z\",\"consecutive_fail\":0,\"disabled_reason\":\"manual\",\"future_field\":{\"preserved\":true}}],\"first_id\":\"endpoint_1\",\"last_id\":\"endpoint_1\",\"has_more\":false}",
            200,
            true),
        Arguments.of(
            "retrieve",
            "GET",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25",
            "",
            "{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"updated_at\":\"2026-10-22T00:01:00Z\",\"last_success_at\":null,\"last_failure_at\":\"2026-10-22T00:01:00Z\",\"consecutive_fail\":0,\"disabled_reason\":\"manual\",\"future_field\":{\"preserved\":true}}",
            200,
            false),
        Arguments.of(
            "retrieve",
            "GET",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25",
            "",
            "{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"updated_at\":\"2026-10-22T00:01:00Z\",\"last_success_at\":null,\"last_failure_at\":\"2026-10-22T00:01:00Z\",\"consecutive_fail\":0,\"disabled_reason\":\"manual\",\"future_field\":{\"preserved\":true}}",
            200,
            true),
        Arguments.of(
            "update",
            "PUT",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25",
            "{\"active\":false,\"events\":[\"future.event\"],\"metadata\":{\"remove\":null,\"team\":\"sdk\"}}",
            "{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"updated_at\":\"2026-10-22T00:01:00Z\",\"last_success_at\":null,\"last_failure_at\":\"2026-10-22T00:01:00Z\",\"consecutive_fail\":0,\"disabled_reason\":\"manual\",\"future_field\":{\"preserved\":true}}",
            200,
            false),
        Arguments.of(
            "update",
            "PUT",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25",
            "{\"active\":false,\"events\":[\"future.event\"],\"metadata\":{\"remove\":null,\"team\":\"sdk\"}}",
            "{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"updated_at\":\"2026-10-22T00:01:00Z\",\"last_success_at\":null,\"last_failure_at\":\"2026-10-22T00:01:00Z\",\"consecutive_fail\":0,\"disabled_reason\":\"manual\",\"future_field\":{\"preserved\":true}}",
            200,
            true),
        Arguments.of(
            "delete",
            "DELETE",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25",
            "",
            "",
            204,
            false),
        Arguments.of(
            "delete",
            "DELETE",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25",
            "",
            "",
            204,
            true),
        Arguments.of(
            "enable",
            "POST",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25/enable",
            "",
            "{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"updated_at\":\"2026-10-22T00:01:00Z\",\"last_success_at\":null,\"last_failure_at\":\"2026-10-22T00:01:00Z\",\"consecutive_fail\":0,\"disabled_reason\":\"manual\",\"future_field\":{\"preserved\":true}}",
            200,
            false),
        Arguments.of(
            "enable",
            "POST",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25/enable",
            "",
            "{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"updated_at\":\"2026-10-22T00:01:00Z\",\"last_success_at\":null,\"last_failure_at\":\"2026-10-22T00:01:00Z\",\"consecutive_fail\":0,\"disabled_reason\":\"manual\",\"future_field\":{\"preserved\":true}}",
            200,
            true),
        Arguments.of(
            "disable",
            "POST",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25/disable",
            "",
            "{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"updated_at\":\"2026-10-22T00:01:00Z\",\"last_success_at\":null,\"last_failure_at\":\"2026-10-22T00:01:00Z\",\"consecutive_fail\":0,\"disabled_reason\":\"manual\",\"future_field\":{\"preserved\":true}}",
            200,
            false),
        Arguments.of(
            "disable",
            "POST",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25/disable",
            "",
            "{\"id\":\"endpoint_1\",\"url\":\"https://hooks.example.com/qca\",\"events\":[\"forward.schedule.archived\",\"future.event\"],\"description\":\"test\",\"metadata\":{\"team\":\"sdk\",\"created_by\":\"platform\"},\"active\":false,\"created_at\":\"2026-10-22T00:00:00Z\",\"updated_at\":\"2026-10-22T00:01:00Z\",\"last_success_at\":null,\"last_failure_at\":\"2026-10-22T00:01:00Z\",\"consecutive_fail\":0,\"disabled_reason\":\"manual\",\"future_field\":{\"preserved\":true}}",
            200,
            true),
        Arguments.of(
            "test",
            "POST",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25/test",
            "",
            "{\"event_id\":7,\"delivery_rows\":0}",
            202,
            false),
        Arguments.of(
            "test",
            "POST",
            "/api/v1/forward/webhook/endpoints/opaque%2F%3F%23%25/test",
            "",
            "{\"event_id\":7,\"delivery_rows\":0}",
            202,
            true));
  }

  CompletableFuture<?> asyncCall(String method) {
    AsyncWebhookEndpoints r = client.async().webhooks().endpoints();
    switch (method) {
      case "list":
        return r.withResponse().list();
      case "create":
        return r.withResponse()
            .create(
                WebhookEndpointCreateParams.builder()
                    .url("https://hooks.example.com/qca")
                    .events(Arrays.asList("forward.schedule.archived"))
                    .metadata(Collections.singletonMap("team", "sdk"))
                    .build(),
                RequestOptions.builder().idempotencyKey("once").build());
      case "update":
        Map<String, String> patch = new LinkedHashMap<>();
        patch.put("remove", null);
        patch.put("team", "sdk");
        return r.withResponse()
            .update(
                "opaque/?#%",
                WebhookEndpointUpdateParams.builder()
                    .active(false)
                    .events(Arrays.asList("future.event"))
                    .metadata(patch)
                    .build());
      case "retrieve":
        return r.withResponse().retrieve("opaque/?#%");
      case "enable":
        return r.withResponse().enable("opaque/?#%");
      case "disable":
        return r.withResponse().disable("opaque/?#%");
      case "test":
        return r.withResponse().test("opaque/?#%");
      default:
        return r.withResponse().delete("opaque/?#%");
    }
  }

  Object syncCall(String method) {
    WebhookEndpoints r = client.webhooks().endpoints();
    switch (method) {
      case "list":
        return r.withResponse().list();
      case "create":
        return r.withResponse()
            .create(
                WebhookEndpointCreateParams.builder()
                    .url("https://hooks.example.com/qca")
                    .events(Arrays.asList("forward.schedule.archived"))
                    .metadata(Collections.singletonMap("team", "sdk"))
                    .build(),
                RequestOptions.builder().idempotencyKey("once").build());
      case "update":
        Map<String, String> patch = new LinkedHashMap<>();
        patch.put("remove", null);
        patch.put("team", "sdk");
        return r.withResponse()
            .update(
                "opaque/?#%",
                WebhookEndpointUpdateParams.builder()
                    .active(false)
                    .events(Arrays.asList("future.event"))
                    .metadata(patch)
                    .build());
      case "retrieve":
        return r.withResponse().retrieve("opaque/?#%");
      case "enable":
        return r.withResponse().enable("opaque/?#%");
      case "disable":
        return r.withResponse().disable("opaque/?#%");
      case "test":
        return r.withResponse().test("opaque/?#%");
      default:
        return r.withResponse().delete("opaque/?#%");
    }
  }

  @ParameterizedTest
  @MethodSource("routes")
  void contracts(
      String method,
      String verb,
      String path,
      String body,
      String response,
      int status,
      boolean async)
      throws Exception {
    api.enqueue(json(response, status));
    ApiResponse<?> result =
        (ApiResponse<?>) (async ? asyncCall(method).get(5, TimeUnit.SECONDS) : syncCall(method));
    RecordedRequest req = api.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(req);
    assertEquals(verb, req.getMethod());
    assertEquals(path, req.getPath());
    assertEquals("Bearer test-sat", req.getHeader("Authorization"));
    if (body.isEmpty()) assertEquals(0, req.getBodySize());
    else
      assertEquals(
          JsonValue.parse(body).toJson(), JsonValue.parse(req.getBody().readUtf8()).toJson());
    assertEquals("req-webhook", result.requestId());
    if (method.equals("delete")) assertNull(result.data());
    else
      assertEquals(
          JsonValue.parse(response).toJson(), ((JsonObject) result.data()).toJsonValue().toJson());
    if (method.equals("create")) {
      assertEquals("once", req.getHeader("Idempotency-Key"));
      assertFalse(result.data().toString().contains("whsec_"));
    }
  }

  @ParameterizedTest
  @MethodSource("routes")
  void errors(
      String method,
      String verb,
      String path,
      String body,
      String response,
      int status,
      boolean async)
      throws Exception {
    api.enqueue(json("{\"error\":{\"message\":\"forbidden\"}}", 403));
    if (async) assertThrows(ApiStatusException.class, () -> Futures.await(asyncCall(method)));
    else assertThrows(ApiStatusException.class, () -> syncCall(method));
  }

  @Test
  void pagination() throws Exception {
    api.enqueue(
        json(
            "{\"data\":[{\"id\":\"z\"},{\"id\":\"a\"}],\"last_id\":\"opaque/+?\",\"has_more\":true}",
            200));
    api.enqueue(json("{\"data\":[],\"last_id\":null,\"first_id\":null,\"has_more\":false}", 200));
    WebhookEndpointListResponse page =
        client.webhooks().endpoints().list(WebhookEndpointListParams.builder().limit(2).build());
    assertEquals("z", page.data().get(0).id());
    assertEquals("a", page.data().get(1).id());
    WebhookEndpointListResponse next = page.getNextPage();
    assertTrue(next.data().isEmpty());
    assertFalse(next.hasNextPage());
    assertEquals("/api/v1/forward/webhook/endpoints?limit=2", api.takeRequest().getPath());
    RecordedRequest request = api.takeRequest();
    assertEquals("opaque/+?", request.getRequestUrl().queryParameter("after_id"));
    assertNull(request.getRequestUrl().queryParameter("page"));
  }

  @Test
  void asyncCancellation() throws Exception {
    api.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    CompletableFuture<WebhookEndpoint> future =
        client.async().webhooks().endpoints().retrieve("endpoint");
    assertNotNull(api.takeRequest(2, TimeUnit.SECONDS));
    assertTrue(future.cancel(true));
    assertTrue(future.isCancelled());
  }

  @Test
  void testIsNotReplayed() {
    try (ForwardClient retry =
        ForwardClient.builder()
            .baseUrl(api.url("/").toString())
            .pat("test")
            .maxRetries(2)
            .build()) {
      api.enqueue(json("{\"error\":{\"message\":\"unavailable\"}}", 503));
      assertThrows(ApiException.class, () -> retry.webhooks().endpoints().test("id"));
      assertEquals(1, api.getRequestCount());
    }
  }
}
