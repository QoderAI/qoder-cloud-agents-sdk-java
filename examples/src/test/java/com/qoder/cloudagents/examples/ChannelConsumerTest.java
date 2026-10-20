package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.errors.ApiStatusException;
import com.qoder.cloudagents.forward.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Timeout(15)
class ChannelConsumerTest {
  MockWebServer server;
  ForwardClient client;
  ByteArrayOutputStream bytes;
  PrintStream out;

  @BeforeEach
  void setup() throws Exception {
    assertTrue(
        ForwardClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()
            .endsWith(".jar"));
    server = new MockWebServer();
    server.start();
    client =
        ForwardClient.builder()
            .baseUrl(server.url("/api/v1/forward").toString())
            .pat("private-pat")
            .maxRetries(0)
            .build();
    bytes = new ByteArrayOutputStream();
    out = new PrintStream(bytes, true, "UTF-8");
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    server.close();
    out.close();
  }

  MockResponse json(String body) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(body);
  }

  RecordedRequest take() throws Exception {
    RecordedRequest r = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  @Test
  void lifecycleNeverEnablesAuthorizesOrPairsAndDeletesOnlyCreatedChannel() throws Exception {
    server.enqueue(json("{\"id\":\"owned\",\"enabled\":false}").setResponseCode(201));
    server.enqueue(json("{\"id\":\"owned\",\"enabled\":false}"));
    server.enqueue(json("{\"id\":\"owned\",\"enabled\":false}"));
    server.enqueue(json("{\"data\":[{\"id\":\"unrelated\"}],\"has_more\":false}"));
    server.enqueue(json("{\"id\":\"owned\",\"deleted\":true}"));
    ChannelLifecycleExample.run(client, "feishu", out);
    assertEquals(5, server.getRequestCount());
    RecordedRequest create = take();
    assertEquals("/api/v1/forward/channels", create.getPath());
    assertEquals("POST", create.getMethod());
    JsonValue body = JsonValue.parse(create.getBody().readUtf8());
    assertFalse(body.get("enabled").asBoolean());
    assertEquals("pairing", body.get("identity_resolution").get("mode").asString());
    assertTrue(body.get("identity_id").isMissing());
    assertTrue(body.get("template_id").isMissing());
    assertTrue(body.get("channel_config").isMissing());
    assertEquals("/api/v1/forward/channels/owned", take().getPath());
    RecordedRequest update = take();
    JsonValue patch = JsonValue.parse(update.getBody().readUtf8());
    assertFalse(patch.get("enabled").asBoolean());
    assertTrue(patch.get("channel_config").get("credentials").isMissing());
    assertFalse(
        patch.get("channel_config").get("response_options").get("include_tool_calls").asBoolean());
    RecordedRequest list = take();
    assertEquals("false", list.getRequestUrl().queryParameter("enabled"));
    RecordedRequest delete = take();
    assertEquals("DELETE", delete.getMethod());
    assertEquals("/api/v1/forward/channels/owned", delete.getPath());
    assertNotEquals(create.getHeader("Idempotency-Key"), update.getHeader("Idempotency-Key"));
    assertNotEquals(update.getHeader("Idempotency-Key"), delete.getHeader("Idempotency-Key"));
    String text = bytes.toString("UTF-8");
    assertTrue(text.contains("deleted_channel=owned"));
    assertFalse(text.contains("private-pat"));
    assertFalse(text.contains("unrelated"));
  }

  @Test
  void cleanupKeepsTheOriginalFailureAndRecoveryId() throws Exception {
    server.enqueue(json("{\"id\":\"owned\",\"enabled\":false}"));
    server.enqueue(json("{}"));
    server.enqueue(json("{\"error\":{\"message\":\"update failed\"}}").setResponseCode(400));
    server.enqueue(json("{\"error\":{\"message\":\"delete failed\"}}").setResponseCode(502));
    ApiStatusException e =
        assertThrows(
            ApiStatusException.class, () -> ChannelLifecycleExample.run(client, "dingtalk", out));
    assertEquals(400, e.statusCode());
    assertEquals(1, e.getSuppressed().length);
    assertEquals(502, ((ApiStatusException) e.getSuppressed()[0]).statusCode());
    assertEquals(4, server.getRequestCount());
    assertTrue(bytes.toString("UTF-8").contains("cleanup_failed_channel=owned"));
  }

  @Test
  void qrConfirmationRefreshesChannelWithoutConfusingItWithPairing() throws Exception {
    server.enqueue(
        json(
            "{\"session_key\":\"secret/key\",\"channel_id\":\"c\",\"status\":\"waiting\",\"qr_code_content\":\"private-url\",\"qr_code_image_base64\":\"private-image\",\"expires_at\":\"soon\"}"));
    server.enqueue(
        json("{\"session_key\":\"secret/key\",\"channel_id\":\"c\",\"status\":\"scanned\"}"));
    server.enqueue(
        json("{\"session_key\":\"secret/key\",\"channel_id\":\"c\",\"status\":\"confirmed\"}"));
    server.enqueue(
        json(
            "{\"id\":\"c\",\"enabled\":true,\"binding_status\":\"bound\",\"identity_resolution\":{\"mode\":\"pairing\"}}"));
    ChannelQRSession initial = ChannelAuthorizationExample.beginQr(client, "c", "qr-create-key");
    assertEquals("private-url", initial.qrCodeContent());
    assertEquals("private-image", initial.qrCodeImageBase64());
    assertEquals(
        "scanned",
        ChannelAuthorizationExample.inspectQr(client, initial.sessionKey(), out).status());
    assertEquals(2, server.getRequestCount());
    assertEquals(
        "confirmed",
        ChannelAuthorizationExample.inspectQr(client, initial.sessionKey(), out).status());
    assertEquals(4, server.getRequestCount());
    RecordedRequest create = take();
    assertEquals("/api/v1/forward/channels/c/qr_sessions", create.getPath());
    assertEquals(0, create.getBodySize());
    assertEquals("qr-create-key", create.getHeader("Idempotency-Key"));
    assertEquals("/api/v1/forward/qr_sessions/secret%2Fkey", take().getPath());
    take();
    assertEquals("/api/v1/forward/channels/c", take().getPath());
    String text = bytes.toString("UTF-8");
    assertTrue(text.contains("binding_status=bound"));
    for (String secret : Arrays.asList("private-pat", "secret/key", "private-url", "private-image"))
      assertFalse(text.contains(secret));
  }

  @Test
  void expiredQrDoesNotSilentlyRestartAuthorization() throws Exception {
    server.enqueue(json("{\"status\":\"expired\",\"session_key\":\"key\",\"channel_id\":\"c\"}"));
    assertEquals("expired", ChannelAuthorizationExample.inspectQr(client, "key", out).status());
    assertEquals(1, server.getRequestCount());
    assertEquals("GET", take().getMethod());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void pairingIsExplicitAndNotAutomaticallyRevoked(boolean byId) throws Exception {
    server.enqueue(
        json(
            "{\"id\":\"p\",\"channel_id\":\"c\",\"status\":\"active\",\"scope_type\":\"room\",\"scope_external_id\":\"private-room\"}"));
    server.enqueue(json("{\"id\":\"p\",\"deleted\":true}"));
    ChannelPairingCreateParams.Builder params =
        ChannelPairingCreateParams.builder().identityId("i").templateId("t");
    if (byId) params.channelPairingId("p");
    else params.code("SECRET");
    ChannelPairing pair = ChannelAuthorizationExample.pair(client, params.build(), "pair-key", out);
    assertEquals("p", pair.id());
    assertEquals(1, server.getRequestCount());
    RecordedRequest create = take();
    assertEquals("/api/v1/forward/channel_pairings", create.getPath());
    assertEquals("pair-key", create.getHeader("Idempotency-Key"));
    JsonValue body = JsonValue.parse(create.getBody().readUtf8());
    assertEquals(byId ? "p" : "SECRET", body.get(byId ? "channel_pairing_id" : "code").asString());
    assertTrue(body.get(byId ? "code" : "channel_pairing_id").isMissing());
    ChannelAuthorizationExample.unpair(client, pair.id(), out);
    RecordedRequest delete = take();
    assertEquals("DELETE", delete.getMethod());
    assertEquals("/api/v1/forward/channel_pairings/p", delete.getPath());
    String text = bytes.toString("UTF-8");
    assertTrue(text.contains("unpaired=p"));
    assertFalse(text.contains("SECRET"));
    assertFalse(text.contains("private-room"));
    assertFalse(text.contains("private-pat"));
  }
}
