package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class ChannelsTest {
  MockWebServer api;
  ForwardClient client;
  static final String ID = "opaque/?#%";

  @BeforeEach
  void setup() throws Exception {
    api = new MockWebServer();
    api.start();
    client =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward").toString())
            .pat("test-pat")
            .maxRetries(0)
            .build();
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    api.close();
  }

  MockResponse json(String text) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(text);
  }

  RecordedRequest take() throws Exception {
    RecordedRequest r = api.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  static ChannelCreateParams createParams() {
    return ChannelCreateParams.builder()
        .channelType("feishu")
        .identityResolution(ChannelIdentityResolution.builder().mode("pairing").build())
        .name("test")
        .enabled(false)
        .build();
  }

  static ChannelUpdateParams updateParams() {
    return ChannelUpdateParams.builder()
        .name("updated")
        .enabled(false)
        .channelConfig(
            ChannelConfigParams.builder()
                .responseOptions(
                    ChannelResponseOptions.builder()
                        .includeToolCalls(false)
                        .includeThinking(true)
                        .build())
                .build())
        .build();
  }

  static Stream<Arguments> routes() {
    String[][] rows = {
      {"list", "GET", "/channels", "", "{\"data\":[],\"has_more\":false}"},
      {
        "create",
        "POST",
        "/channels",
        "{\"channel_type\":\"feishu\",\"identity_resolution\":{\"mode\":\"pairing\"},\"name\":\"test\",\"enabled\":false}",
        "{\"id\":\"channel_1\",\"identity_resolution\":{\"mode\":\"pairing\"},\"identity_id\":null,\"template_id\":null,\"enabled\":false}"
      },
      {
        "get",
        "GET",
        "/channels/opaque%2F%3F%23%25",
        "",
        "{\"id\":\"channel_1\",\"binding_status\":\"unbound\"}"
      },
      {
        "update",
        "POST",
        "/channels/opaque%2F%3F%23%25",
        "{\"name\":\"updated\",\"enabled\":false,\"channel_config\":{\"response_options\":{\"include_tool_calls\":false,\"include_thinking\":true}}}",
        "{\"id\":\"channel_1\",\"enabled\":false}"
      },
      {
        "delete",
        "DELETE",
        "/channels/opaque%2F%3F%23%25",
        "",
        "{\"id\":\"channel_1\",\"deleted\":true}"
      },
      {
        "pair",
        "POST",
        "/channel_pairings",
        "{\"code\":\"K7MP92\",\"identity_id\":\"idn_1\",\"template_id\":\"tmpl_1\"}",
        "{\"id\":\"pair_1\",\"scope_type\":\"room\",\"scope_external_id\":\"group_1\",\"scope_display_name\":\"Test"
            + " group\",\"status\":\"active\"}"
      },
      {
        "unpair",
        "DELETE",
        "/channel_pairings/opaque%2F%3F%23%25",
        "",
        "{\"id\":\"pair_1\",\"deleted\":true}"
      },
      {
        "qrCreate",
        "POST",
        "/channels/opaque%2F%3F%23%25/qr_sessions",
        "",
        "{\"session_key\":\"opaque-key\",\"status\":\"waiting\",\"qr_code_content\":\"https://login.example/?secret=qr\",\"qr_code_image_base64\":\"data:image/png;base64,test\"}"
      },
      {
        "qrGet",
        "GET",
        "/qr_sessions/opaque%2F%3F%23%25",
        "",
        "{\"session_key\":\"opaque-key\",\"status\":\"confirmed\",\"channel_id\":\"channel_1\"}"
      }
    };
    return Arrays.stream(rows)
        .flatMap(
            r -> Stream.of(false, true).map(a -> Arguments.of(r[0], r[1], r[2], r[3], r[4], a)));
  }

  @ParameterizedTest
  @MethodSource("routes")
  void wireContracts(
      String op, String method, String path, String body, String reply, boolean async)
      throws Exception {
    int status = op.equals("create") ? 201 : 200;
    api.enqueue(
        json(reply)
            .setResponseCode(status)
            .setHeader("x-request-id", "req-channel")
            .setHeader("X-Contract", "retained"));
    ApiResponse<?> result = invoke(op, async);
    assertEquals(status, result.statusCode());
    assertEquals("req-channel", result.requestId());
    assertEquals("retained", result.header("x-contract"));
    assertEquals(reply, ((JsonModel) result.data()).rawJson());
    assertEquals("req-channel", ((JsonModel) result.data()).requestId());
    RecordedRequest r = take();
    assertEquals(method, r.getMethod());
    assertEquals("/api/v1/forward" + path, r.getRequestUrl().encodedPath());
    assertEquals("Bearer test-pat", r.getHeader("Authorization"));
    assertEquals("idem-operation", r.getHeader("Idempotency-Key"));
    assertEquals("custom", r.getRequestUrl().queryParameter("extension"));
    if (body.isEmpty()) assertEquals(0, r.getBodySize());
    else
      assertEquals(
          JsonValue.parse(body).toJson(), JsonValue.parse(r.getBody().readUtf8()).toJson());
  }

  ApiResponse<?> invoke(String op, boolean a) throws Exception {
    RequestOptions o =
        RequestOptions.builder()
            .idempotencyKey("idem-operation")
            .query("extension", "custom")
            .build();
    ChannelPairingCreateParams pair =
        ChannelPairingCreateParams.builder()
            .code("K7MP92")
            .identityId("idn_1")
            .templateId("tmpl_1")
            .build();
    switch (op) {
      case "list":
        return a
            ? client.async().channels().withResponse().list(o).get()
            : client.channels().withResponse().list(o);
      case "create":
        return a
            ? client.async().channels().withResponse().create(createParams(), o).get()
            : client.channels().withResponse().create(createParams(), o);
      case "get":
        return a
            ? client.async().channels().withResponse().retrieve(ID, o).get()
            : client.channels().withResponse().retrieve(ID, o);
      case "update":
        return a
            ? client.async().channels().withResponse().update(ID, updateParams(), o).get()
            : client.channels().withResponse().update(ID, updateParams(), o);
      case "delete":
        return a
            ? client.async().channels().withResponse().delete(ID, o).get()
            : client.channels().withResponse().delete(ID, o);
      case "pair":
        return a
            ? client.async().channelPairings().withResponse().create(pair, o).get()
            : client.channelPairings().withResponse().create(pair, o);
      case "unpair":
        return a
            ? client.async().channelPairings().withResponse().delete(ID, o).get()
            : client.channelPairings().withResponse().delete(ID, o);
      case "qrCreate":
        return a
            ? client.async().channels().qrSessions().withResponse().create(ID, o).get()
            : client.channels().qrSessions().withResponse().create(ID, o);
      case "qrGet":
        return a
            ? client.async().channels().qrSessions().withResponse().retrieve(ID, o).get()
            : client.channels().qrSessions().withResponse().retrieve(ID, o);
      default:
        throw new AssertionError(op);
    }
  }

  @Test
  void fixedChannelsAndCredentialRotationUseWriteOnlyConfiguration() throws Exception {
    ChannelCredentials credentials =
        ChannelCredentials.builder().appId("app").appSecret("private-secret").build();
    ChannelCreateParams params =
        ChannelCreateParams.builder()
            .channelType("feishu")
            .identityId("idn_1")
            .templateId("tmpl_1")
            .channelConfig(ChannelConfigParams.builder().credentials(credentials).build())
            .build();
    String reply =
        "{\"id\":\"c\",\"channel_config\":{\"response_options\":{\"include_thinking\":false}}}";
    api.enqueue(json(reply));
    api.enqueue(json(reply));
    Channel result = client.channels().create(params);
    assertTrue(result.channelConfig().field("credentials").isMissing());
    assertFalse(result.channelConfig().responseOptions().includeThinking());
    JsonValue create = JsonValue.parse(take().getBody().readUtf8());
    assertTrue(create.get("identity_resolution").isMissing());
    assertEquals(
        "private-secret",
        create.get("channel_config").get("credentials").get("app_secret").asString());
    client
        .async()
        .channels()
        .update(
            "c",
            ChannelUpdateParams.builder()
                .channelConfig(
                    ChannelConfigParams.builder()
                        .credentials(
                            ChannelCredentials.builder().appSecret("rotated-secret").build())
                        .build())
                .build())
        .get();
    JsonValue update = JsonValue.parse(take().getBody().readUtf8());
    assertEquals(
        "rotated-secret",
        update.get("channel_config").get("credentials").get("app_secret").asString());
    assertTrue(update.get("channel_config").get("response_options").isMissing());
    assertTrue(update.get("enabled").isMissing());
    assertFalse(credentials.toString().contains("private-secret"));
    assertFalse(params.toString().contains("private-secret"));
    assertFalse(
        ChannelCredentials.builder()
            .clientSecret("private-secret")
            .toString()
            .contains("private-secret"));
  }

  @Test
  void pairByIdDoesNotSendAnEmptyCode() throws Exception {
    api.enqueue(json("{\"id\":\"pair_1\",\"status\":\"active\",\"paired_at\":\"now\"}"));
    ChannelPairing pair =
        client
            .async()
            .channelPairings()
            .create(
                ChannelPairingCreateParams.builder()
                    .channelPairingId("pair_1")
                    .identityId("i")
                    .templateId("t")
                    .build())
            .get();
    JsonValue body = JsonValue.parse(take().getBody().readUtf8());
    assertTrue(body.get("code").isMissing());
    assertEquals("pair_1", body.get("channel_pairing_id").asString());
    assertEquals("active", pair.status());
    assertEquals("now", pair.pairedAt());
  }

  @Test
  void conditionalRequiredFieldsFailBeforeNetwork() {
    assertThrows(ConfigurationException.class, () -> ChannelCreateParams.builder().build());
    assertThrows(
        ConfigurationException.class,
        () -> ChannelCreateParams.builder().channelType("feishu").build());
    assertThrows(
        ConfigurationException.class,
        () -> ChannelCreateParams.builder().channelType("feishu").identityId("i").build());
    assertThrows(
        ConfigurationException.class,
        () ->
            ChannelCreateParams.builder()
                .channelType("feishu")
                .identityResolution(ChannelIdentityResolution.builder().mode("pairing").build())
                .identityId(null)
                .build());
    assertThrows(
        ConfigurationException.class,
        () -> ChannelPairingCreateParams.builder().identityId("i").templateId("t").build());
    assertThrows(
        ConfigurationException.class,
        () ->
            ChannelPairingCreateParams.builder()
                .code("abc")
                .channelPairingId("p")
                .identityId("i")
                .templateId("t")
                .build());
    assertThrows(
        ConfigurationException.class,
        () ->
            ChannelPairingCreateParams.builder().code(" ").identityId("i").templateId("t").build());
    assertThrows(
        ConfigurationException.class,
        () ->
            ChannelPairingCreateParams.builder()
                .code(null)
                .identityId("i")
                .templateId("t")
                .build());
    assertThrows(
        ConfigurationException.class,
        () -> ChannelPairingCreateParams.builder().code("abc").identityId("i").build());
    assertEquals(0, api.getRequestCount());
  }

  @Test
  void responseFieldsAndUnknownStatesRemainDistinct() {
    Channel c =
        new Channel(
            JsonValue.parse(
                "{\"identity_id\":null,\"template_id\":null,\"identity_resolution\":{\"mode\":\"pairing\"},\"channel_type\":\"future-platform\",\"enabled\":true,\"binding_status\":\"expired\",\"channel_config\":{\"response_options\":{\"include_thinking\":false},\"new_option\":{\"enabled\":true}}}"),
            "req");
    assertNull(c.identityId());
    assertTrue(c.field("identity_id").isNull());
    assertEquals("future-platform", c.channelType());
    assertTrue(c.enabled());
    assertEquals("expired", c.bindingStatus());
    assertNull(c.channelConfig().responseOptions().includeToolCalls());
    assertTrue(c.channelConfig().field("new_option").get("enabled").asBoolean());
    ChannelPairing pairing =
        new ChannelPairing(
            JsonValue.parse(
                "{\"scope_type\":\"room\",\"scope_external_id\":\"opaque-room\",\"scope_display_name\":\"Display"
                    + " only\",\"status\":\"future-status\"}"),
            "r");
    assertEquals("room", pairing.scopeType());
    assertEquals("opaque-room", pairing.scopeExternalId());
    assertEquals("Display only", pairing.scopeDisplayName());
    assertEquals("future-status", pairing.status());
    ChannelQRSession q =
        new ChannelQRSession(
            JsonValue.parse(
                "{\"session_key\":\"secret-key\",\"qr_code_content\":\"private-url\",\"qr_code_image_base64\":\"private-image\",\"status\":\"error\",\"err_code\":\"denied\",\"err_msg\":null,\"poll_interval_seconds\":3}"),
            "r");
    assertEquals("denied", q.errCode());
    assertNull(q.errMsg());
    assertEquals(3L, q.pollIntervalSeconds());
    assertEquals("private-url", q.qrCodeContent());
    assertEquals("private-image", q.qrCodeImageBase64());
    assertEquals("ChannelQRSession", q.toString());
    ChannelQRSession confirmed =
        new ChannelQRSession(
            JsonValue.parse("{\"status\":\"confirmed\",\"channel_id\":\"c\"}"), "r");
    assertNull(confirmed.qrCodeContent());
    assertNull(confirmed.pollIntervalSeconds());
  }

  @Test
  void patchesKeepFalseNullOmittedAndSnapshotValues() {
    Map<String, Object> extension = new LinkedHashMap<>();
    extension.put("value", false);
    ChannelResponseOptions.Builder options =
        ChannelResponseOptions.builder().includeToolCalls(false);
    ChannelUpdateParams p =
        ChannelUpdateParams.builder()
            .name(null)
            .enabled(false)
            .channelConfig(ChannelConfigParams.builder().responseOptions(options.build()).build())
            .additionalField("extension", JsonValue.of(extension))
            .build();
    options.includeToolCalls(true);
    extension.put("value", true);
    assertTrue(p.field("name").isNull());
    assertTrue(p.field("template_id").isMissing());
    assertFalse(p.enabled());
    assertFalse(p.channelConfig().responseOptions().includeToolCalls());
    assertTrue(p.channelConfig().responseOptions().field("include_thinking").isMissing());
    assertFalse(p.field("extension").get("value").asBoolean());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void pagingPreservesDirectionFiltersAndServerOrder(boolean before) throws Exception {
    api.enqueue(
        json(
            "{\"data\":[{\"id\":\"z\"},{\"id\":\"a\"}],\"first_id\":\"z\",\"last_id\":\"a\",\"has_more\":true}"));
    api.enqueue(
        json(
            "{\"data\":[{\"id\":\"b\"}],\"first_id\":\"b\",\"last_id\":\"b\",\"has_more\":false}"));
    ChannelListParams p =
        ChannelListParams.builder()
            .identityId("i")
            .templateId("t")
            .channelType("teams")
            .enabled(false)
            .bindingStatus("expired")
            .limit(2)
            .build();
    ChannelListResponse page =
        client
            .channels()
            .list(
                p,
                RequestOptions.builder().query(before ? "before_id" : "after_id", "start").build());
    assertEquals(1, api.getRequestCount());
    assertEquals(
        Arrays.asList("z", "a"),
        page.data().stream().map(Channel::id).collect(Collectors.toList()));
    ChannelListResponse next = page.getNextPageAsync().get();
    assertEquals("b", next.data().get(0).id());
    assertThrows(PaginationException.class, next::getNextPage);
    take();
    RecordedRequest r = take();
    assertEquals(
        before ? "z" : "a", r.getRequestUrl().queryParameter(before ? "before_id" : "after_id"));
    assertNull(r.getRequestUrl().queryParameter(before ? "after_id" : "before_id"));
    assertEquals("teams", r.getRequestUrl().queryParameter("channel_type"));
    assertEquals("false", r.getRequestUrl().queryParameter("enabled"));
    assertEquals("expired", r.getRequestUrl().queryParameter("binding_status"));
    assertEquals("i", r.getRequestUrl().queryParameter("identity_id"));
    assertEquals("t", r.getRequestUrl().queryParameter("template_id"));
    assertEquals("2", r.getRequestUrl().queryParameter("limit"));
  }

  @Test
  void repeatedCursorAndInvalidSelectorsStopWithoutExtraCalls() throws Exception {
    String repeated =
        "{\"data\":[{\"id\":\"a\"}],\"first_id\":\"a\",\"last_id\":\"a\",\"has_more\":true}";
    api.enqueue(json(repeated));
    api.enqueue(json(repeated));
    ChannelListResponse p = client.async().channels().list().get().getNextPage();
    assertThrows(PaginationException.class, p::getNextPage);
    assertThrows(ConfigurationException.class, () -> ChannelListParams.builder().limit(101));
    assertThrows(ConfigurationException.class, () -> ChannelListParams.builder().limit(0));
    assertThrows(
        ConfigurationException.class,
        () -> ChannelListParams.builder().afterId("a").beforeId("b").build());
    assertThrows(
        ConfigurationException.class,
        () -> client.channels().list(RequestOptions.builder().query("page", "p").build()));
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .channels()
                .list(
                    ChannelListParams.builder().afterId("a").build(),
                    RequestOptions.builder().query("before_id", "b").build()));
    assertEquals(2, api.getRequestCount());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void deletionRetainsEmptyResponseMetadata(boolean pairing) throws Exception {
    api.enqueue(new MockResponse().setResponseCode(204).setHeader("x-request-id", "deleted"));
    ApiResponse<?> response =
        pairing
            ? client.async().channelPairings().withResponse().delete("p").get()
            : client.channels().withResponse().delete("c");
    assertEquals(204, response.statusCode());
    assertEquals("deleted", response.requestId());
    assertNull(response.data());
    assertEquals("DELETE", take().getMethod());
  }

  @Test
  void createDoesNotRetryUnsafe503AndKeyedUpdateCanReplay() throws Exception {
    api.enqueue(
        json("{\"error\":{\"message\":\"gateway unavailable\"}}")
            .setResponseCode(503)
            .setHeader("Retry-After", "0"));
    assertThrows(
        ApiStatusException.class,
        () ->
            client
                .channels()
                .create(createParams(), RequestOptions.builder().maxRetries(1).build()));
    assertEquals(1, api.getRequestCount());
    take();
    api.enqueue(json("{}").setResponseCode(503).setHeader("Retry-After", "0"));
    api.enqueue(json("{\"id\":\"c\",\"enabled\":false}"));
    Channel c =
        client
            .channels()
            .update(
                "c",
                updateParams(),
                RequestOptions.builder().idempotencyKey("update-key").maxRetries(1).build());
    assertFalse(c.enabled());
    RecordedRequest first = take(), retry = take();
    assertEquals(first.getBody().readUtf8(), retry.getBody().readUtf8());
    assertEquals("update-key", retry.getHeader("Idempotency-Key"));
    assertEquals("1", retry.getHeader("X-Qoder-Retry-Count"));
  }

  @Test
  void expiredPairingCodeAndQrNotFoundRemainTypedErrors() throws Exception {
    api.enqueue(
        json("{\"error\":{\"type\":\"invalid_request_error\",\"code\":\"invalid_pairing_code\",\"message\":\"Code"
                 + " expired\"}}")
            .setResponseCode(400)
            .setHeader("x-request-id", "req-pair"));
    ApiStatusException pairing =
        assertThrows(
            ApiStatusException.class,
            () ->
                client
                    .channelPairings()
                    .create(
                        ChannelPairingCreateParams.builder()
                            .code("K7MP92")
                            .identityId("i")
                            .templateId("t")
                            .build(),
                        RequestOptions.builder().idempotencyKey("pair-key").maxRetries(2).build()));
    assertEquals("invalid_pairing_code", pairing.code());
    assertEquals("req-pair", pairing.requestId());
    assertEquals(1, api.getRequestCount());
    take();
    api.enqueue(
        json("{\"error\":{\"type\":\"not_found_error\",\"message\":\"QR not found\"}}")
            .setResponseCode(404)
            .setHeader("request-id", "req-qr"));
    ExecutionException async =
        assertThrows(
            ExecutionException.class,
            () -> client.async().channels().qrSessions().retrieve("expired").get());
    assertTrue(async.getCause() instanceof ApiStatusException);
    assertEquals(404, ((ApiStatusException) async.getCause()).statusCode());
    assertEquals("req-qr", ((ApiStatusException) async.getCause()).requestId());
    assertEquals(2, api.getRequestCount());
  }

  @Test
  void staticSatUsesSharedAuthenticationForQrAndPairing() throws Exception {
    try (ForwardClient sa =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward").toString())
            .authMode(com.qoder.cloudagents.auth.AuthMode.SA)
            .accessToken("test-sat")
            .maxRetries(0)
            .build()) {
      api.enqueue(json("{\"status\":\"waiting\"}"));
      api.enqueue(json("{\"id\":\"p\"}"));
      sa.channels().qrSessions().create("c");
      sa.channelPairings()
          .create(
              ChannelPairingCreateParams.builder()
                  .channelPairingId("p")
                  .identityId("i")
                  .templateId("t")
                  .build());
      assertEquals("Bearer test-sat", take().getHeader("Authorization"));
      assertEquals("Bearer test-sat", take().getHeader("Authorization"));
    }
  }

  @Test
  void asyncQrCancellationPropagatesToTransport() throws Exception {
    api.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    CompletableFuture<ChannelQRSession> pending =
        client.async().channels().qrSessions().retrieve("key");
    take();
    assertTrue(pending.cancel(true));
    assertTrue(pending.isCancelled());
  }
}
