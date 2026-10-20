package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.core.JsonValue;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Timeout(15)
class RealtimeConsumerTest {
  MockWebServer server;
  BlockingQueue<JsonValue> sent = new LinkedBlockingQueue<>();

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
  }

  @AfterEach
  void close() throws Exception {
    server.close();
  }

  ForwardClient client(boolean sa) {
    ForwardClient.Builder b =
        ForwardClient.builder().baseUrl(server.url("/api/v1/forward").toString()).maxRetries(0);
    return (sa ? b.authMode(AuthMode.SA).accessToken("direct-sat") : b.pat("test-pat")).build();
  }

  String event(String type, JsonValue payload) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("version", "voice.realtime.v1");
    map.put("type", type);
    map.put("event_id", "evt-" + type);
    map.put("sequence", 1);
    map.put("conversation_id", "c");
    map.put("timestamp", "2026-10-18T00:00:00Z");
    map.put("payload", payload);
    return JsonValue.of(map).toJson();
  }

  String event(String type, String payload) {
    return event(type, JsonValue.parse(payload));
  }

  void upgrade(boolean audioInput) {
    server.enqueue(
        new MockResponse()
            .setHeader("x-request-id", "req-consumer")
            .withWebSocketUpgrade(
                new WebSocketListener() {
                  boolean answered;

                  @Override
                  public void onOpen(WebSocket ws, Response r) {
                    ws.send(
                        event(
                            "voice.ready",
                            "{\"provider\":\"qoder\",\"config\":{\"audio\":{\"output\":{\"voice\":\"longanqian\"}}},\"input_audio\":{\"format\":\"pcm16\",\"sample_rate\":16000,\"channels\":1},\"output_audio\":{\"format\":\"pcm16\",\"sample_rate\":24000,\"channels\":1},\"capabilities\":{\"graceful_close\":true}}"));
                  }

                  @Override
                  public void onMessage(WebSocket ws, String text) {
                    JsonValue json = JsonValue.parse(text);
                    sent.add(json);
                    String type = json.get("type").asString();
                    if (!answered && (type.equals("text.message") || type.equals("audio.append"))) {
                      answered = true;
                      if (audioInput)
                        ws.send(
                            event(
                                "transcript.final",
                                "{\"role\":\"user\",\"text\":\"input speech\"}"));
                      ws.send(
                          event(
                              "transcript.final", "{\"role\":\"assistant\",\"text\":\"answer\"}"));
                      ws.send(event("audio.delta", "{\"audio\":\"AAABAA==\"}"));
                      ws.send(event("audio.done", "{}"));
                    }
                    if (type.equals("ping")) ws.send(event("pong", "{}"));
                    if (type.equals("connection.close")) {
                      Map<String, Object> payload = new LinkedHashMap<>();
                      payload.put("request_id", json.get("payload").get("request_id"));
                      payload.put("outcome", "no_active_response");
                      ws.send(event("connection.closed", JsonValue.of(payload)));
                    }
                  }
                }));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void textExampleConsumesPackagedJar(boolean sa) throws Exception {
    upgrade(false);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ForwardClient c = client(sa);
        PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
      RealtimeExample.run(c, "c", "hello", null, out);
    }
    String output = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    assertTrue(output.contains("close_outcome=no_active_response"));
    assertTrue(output.contains("output_pcm_bytes=4"));
    assertEquals(
        "Bearer " + (sa ? "direct-sat" : "test-pat"),
        server.takeRequest().getHeader("Authorization"));
    List<String> types = new ArrayList<>();
    for (JsonValue value : sent) types.add(value.get("type").asString());
    assertTrue(types.contains("playback.cancelled"));
    assertFalse(types.contains("playback.started"));
    assertFalse(types.contains("playback.ended"));
    assertFalse(output.contains("answer"));
    assertFalse(output.contains("test-pat"));
  }

  @Test
  void pcmExampleSendsWholeRawSamplesAndUserTranscriptIsRequired() throws Exception {
    upgrade(true);
    byte[] pcm = new byte[1280];
    try (ForwardClient c = client(false)) {
      RealtimeExample.run(c, "c", null, pcm, new PrintStream(new ByteArrayOutputStream()));
    }
    int total = 0;
    for (JsonValue value : sent)
      if (value.get("type").asString().equals("audio.append"))
        total += Base64.getDecoder().decode(value.get("payload").get("audio").asString()).length;
    assertEquals(pcm.length, total);
  }

  @Test
  void noInputOnlyPingsAndCloses() throws Exception {
    upgrade(false);
    try (ForwardClient c = client(false)) {
      RealtimeExample.run(c, "c", null, null, new PrintStream(new ByteArrayOutputStream()));
    }
    List<String> types = new ArrayList<>();
    for (JsonValue value : sent) types.add(value.get("type").asString());
    assertEquals(Arrays.asList("ping", "connection.close"), types);
  }

  @Test
  void creationAndHistoryUseIndependentCursor() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(201).setBody("{\"id\":\"c\"}"));
    server.enqueue(
        new MockResponse()
            .setBody(
                "{\"events\":[{\"id\":\"vevt_new\",\"type\":\"message\"}],\"page\":{\"has_more\":true,\"next_before\":\"older\"}}"));
    server.enqueue(
        new MockResponse()
            .setBody(
                "{\"events\":[{\"id\":\"vevt_old\",\"type\":\"work\"}],\"page\":{\"has_more\":false,\"next_before\":null}}"));
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ForwardClient c = client(false)) {
      assertEquals("c", RealtimeExample.create(c, "identity", "template", "stable-key"));
      RealtimeExample.history(c, "c", 2, new PrintStream(bytes, true, "UTF-8"));
    }
    assertEquals("stable-key", server.takeRequest().getHeader("Idempotency-Key"));
    server.takeRequest();
    assertEquals("older", server.takeRequest().getRequestUrl().queryParameter("before"));
    String text = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    assertTrue(text.indexOf("vevt_new") < text.indexOf("vevt_old"));
  }

  @Test
  void invalidAudioDoesNotConnect() {
    try (ForwardClient c = client(false)) {
      assertThrows(
          IllegalArgumentException.class,
          () -> RealtimeExample.run(c, "c", null, new byte[3], System.out));
      assertThrows(
          IllegalArgumentException.class,
          () ->
              RealtimeExample.run(
                  c, "c", null, "RIFF0000WAVE".getBytes(StandardCharsets.UTF_8), System.out));
      assertThrows(
          IllegalArgumentException.class,
          () -> RealtimeExample.run(c, "c", "text", new byte[2], System.out));
      assertEquals(0, server.getRequestCount());
    }
  }
}
