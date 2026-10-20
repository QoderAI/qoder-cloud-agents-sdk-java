package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.realtime.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(10)
class RealtimeContractsTest {
  MockWebServer server;
  ForwardClient client;

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.start();
    client =
        ForwardClient.builder()
            .baseUrl(server.url("/api/v1/forward").toString())
            .pat("test-pat")
            .maxRetries(0)
            .build();
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    server.close();
  }

  static List<JsonValue> fixtures() throws Exception {
    return JsonValue.parse(
            new String(
                Files.readAllBytes(
                    Paths.get(
                        RealtimeContractsTest.class.getResource("/realtime-events.json").toURI())),
                StandardCharsets.UTF_8))
        .get("events")
        .asArray();
  }

  static Stream<JsonValue> events() throws Exception {
    return fixtures().stream();
  }

  static JsonValue fixture(String type) throws Exception {
    return fixtures().stream().filter(v -> v.get("type").asString().equals(type)).findFirst().get();
  }

  static JsonValue change(JsonValue v, String key, Object value) {
    Map<String, Object> map = new LinkedHashMap<>(v.asObject());
    map.put(key, value);
    return JsonValue.of(map);
  }

  static RealtimeConversationCreateParams createParams() {
    return RealtimeConversationCreateParams.builder().identityId("i").templateId("t").build();
  }

  @ParameterizedTest
  @MethodSource("events")
  void baselineEventsRemainLossless(JsonValue json) {
    RealtimeEvent event = RealtimeEvent.fromJson(json);
    assertEquals(json.toJson(), event.rawJson());
    assertEquals("preserved", event.field("future_envelope").asString());
    if (event.audio() != null) {
      byte[] audio = event.audio();
      audio[0] = 99;
      assertEquals(0, event.audio()[0]);
    }
    if (event.ready() != null) assertEquals(16000L, event.ready().inputAudio().sampleRate());
    assertFalse(event.toString().contains("payload"));
  }

  @ParameterizedTest
  @CsvSource({
    "version,wrong",
    "sequence,-1",
    "sequence,1.2",
    "sequence,9223372036854775808",
    "event_id,''",
    "conversation_id,''",
    "timestamp,2026-10-18",
    "timestamp,2026-99-99T00:00:00Z",
    "payload,null"
  })
  void rejectsInvalidEnvelope(String field, String value) throws Exception {
    Object v =
        Arrays.asList("sequence", "payload").contains(field) ? JsonValue.parse(value) : value;
    assertThrows(
        ResponseValidationException.class,
        () -> RealtimeEvent.fromJson(change(fixture("voice.ready"), field, v)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"%%%", "AAAA", "", "AA", "AA==", "AAABAA==\n"})
  void invalidPcmIsRejected(String audio) throws Exception {
    assertThrows(
        ResponseValidationException.class,
        () ->
            RealtimeEvent.fromJson(
                change(
                    fixture("audio.delta"), "payload", Collections.singletonMap("audio", audio))));
  }

  @Test
  void readyValidatesFormatsAndCapabilities() throws Exception {
    JsonValue ready = fixture("voice.ready");
    for (String name : Arrays.asList("input_audio", "output_audio", "capabilities")) {
      JsonValue payload = change(ready.get("payload"), name, Collections.emptyMap());
      assertThrows(
          ResponseValidationException.class,
          () -> RealtimeEvent.fromJson(change(ready, "payload", payload)));
    }
  }

  @Test
  void createUsesExplicitIdempotencyAndSharedAuth() throws Exception {
    server.enqueue(
        new MockResponse()
            .setResponseCode(201)
            .setHeader("x-request-id", "req-create")
            .setHeader("Idempotency-Replayed", "true")
            .setBody(
                "{\"id\":\"c\",\"future\":17,\"config\":{\"audio\":{\"output\":{\"voice\":\"future-voice\"}}}}"));
    RealtimeConversationCreateParams p =
        RealtimeConversationCreateParams.builder()
            .identityId("i")
            .templateId("t")
            .title(null)
            .config(
                RealtimeConfig.builder()
                    .audio(
                        RealtimeAudioConfig.builder()
                            .output(
                                RealtimeAudioOutputConfig.builder().voice("future-voice").build())
                            .build())
                    .build())
            .build();
    ApiResponse<RealtimeConversation> response =
        client
            .realtime()
            .conversations()
            .withResponse()
            .create(p, RequestOptions.builder().idempotencyKey("create-once").build());
    assertEquals("c", response.data().id());
    assertEquals("req-create", response.data().requestId());
    assertEquals("true", response.header("Idempotency-Replayed"));
    RecordedRequest r = server.takeRequest();
    assertEquals("/api/v1/forward/realtime/conversations", r.getPath());
    assertEquals("POST", r.getMethod());
    assertEquals("Bearer test-pat", r.getHeader("Authorization"));
    assertEquals("create-once", r.getHeader("Idempotency-Key"));
    assertEquals(p.rawJson(), r.getBody().readUtf8());
  }

  @Test
  void titleLimitCountsCharactersAndAllowsServerTrimming() throws Exception {
    String title = " " + String.join("", Collections.nCopies(256, "中")) + " ";
    server.enqueue(new MockResponse().setBody("{\"id\":\"c\"}"));
    client
        .realtime()
        .conversations()
        .create(
            RealtimeConversationCreateParams.builder()
                .identityId("i")
                .templateId("t")
                .title(title)
                .build(),
            RequestOptions.builder().idempotencyKey("key").build());
    assertEquals(
        title, JsonValue.parse(server.takeRequest().getBody().readUtf8()).get("title").asString());
  }

  @Test
  void createRequiresKeyAndAllowsDefaultWithRequestOverride() throws Exception {
    assertThrows(
        ConfigurationException.class,
        () -> client.realtime().conversations().create(createParams()));
    try (ForwardClient c =
        ForwardClient.builder()
            .baseUrl(server.url("/api/v1/forward").toString())
            .pat("p")
            .defaultHeader("Idempotency-Key", "default-key")
            .build()) {
      server.enqueue(new MockResponse().setBody("{\"id\":\"one\"}"));
      server.enqueue(new MockResponse().setBody("{\"id\":\"two\"}"));
      c.realtime().conversations().create(createParams());
      c.async()
          .realtime()
          .conversations()
          .create(
              createParams(),
              RequestOptions.builder().header("idempotency-key", "override-key").build())
          .get();
      assertEquals("default-key", server.takeRequest().getHeader("Idempotency-Key"));
      assertEquals("override-key", server.takeRequest().getHeader("Idempotency-Key"));
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " padded", "padded ", "\t", "x\ny"})
  void invalidKeysFailBeforeNetwork(String key) {
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .realtime()
                .conversations()
                .create(createParams(), RequestOptions.builder().idempotencyKey(key).build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void invalidCreateFieldsFailBeforeNetwork() {
    for (RealtimeConversationCreateParams p :
        Arrays.asList(
            RealtimeConversationCreateParams.builder().identityId(" ").templateId("t").build(),
            RealtimeConversationCreateParams.builder()
                .identityId("i")
                .templateId("t")
                .title("")
                .build(),
            RealtimeConversationCreateParams.builder()
                .identityId("i")
                .templateId("t")
                .config(null)
                .build(),
            RealtimeConversationCreateParams.builder()
                .identityId("i")
                .templateId("t")
                .metadata(Collections.singletonMap("nul", JsonValue.of("a\0b")))
                .build()))
      assertThrows(
          ConfigurationException.class,
          () ->
              client
                  .realtime()
                  .conversations()
                  .create(p, RequestOptions.builder().idempotencyKey("key").build()));
  }

  static String history(boolean more, String cursor, String id) {
    return "{\"conversation\":{\"id\":\"conv_test\",\"initialization_status\":\"ready\"},\"events\":[{\"id\":\""
        + id
        + "\",\"type\":\"message\",\"future\":1}],\"page\":{\"has_more\":"
        + more
        + ",\"next_before\":"
        + (cursor == null ? "null" : "\"" + cursor + "\"")
        + "}}";
  }

  @Test
  void historyUsesNextBeforeAndPreservesFiltersAndOrder() throws Exception {
    server.enqueue(
        new MockResponse()
            .setHeader("request-id", "hist1")
            .setBody(history(true, "older/+=", "newer")));
    server.enqueue(new MockResponse().setBody(history(false, null, "older")));
    RealtimeHistoryPage page =
        client
            .realtime()
            .conversations()
            .history(
                "conv/a",
                RealtimeHistoryParams.builder()
                    .limit(2)
                    .types("message,work")
                    .before("start")
                    .build(),
                RequestOptions.builder().query("limit", "3").header("x-test", "kept").build());
    assertEquals("hist1", page.requestId());
    assertEquals("ready", page.conversation().initializationStatus());
    List<String> ids = new ArrayList<>();
    for (RealtimeHistoryEvent e : page) ids.add(e.id());
    assertEquals(Arrays.asList("newer", "older"), ids);
    RecordedRequest first = server.takeRequest(), second = server.takeRequest();
    assertTrue(
        first.getPath().startsWith("/api/v1/forward/realtime/conversations/conv%2Fa/history?"));
    assertEquals("start", first.getRequestUrl().queryParameter("before"));
    assertEquals("older/+=", second.getRequestUrl().queryParameter("before"));
    assertEquals("message,work", second.getRequestUrl().queryParameter("types"));
    assertEquals("3", second.getRequestUrl().queryParameter("limit"));
    assertNull(second.getRequestUrl().queryParameter("after_id"));
    assertEquals("kept", second.getHeader("x-test"));
  }

  @Test
  void historyRejectsMissingAndRepeatedCursor() {
    server.enqueue(new MockResponse().setBody(history(true, null, "a")));
    assertThrows(PaginationException.class, () -> client.realtime().conversations().history("c"));
    server.enqueue(new MockResponse().setBody(history(true, "same", "a")));
    RealtimeHistoryPage p =
        client
            .realtime()
            .conversations()
            .history("c", RealtimeHistoryParams.builder().before("same").build());
    assertThrows(PaginationException.class, p::getNextPage);
    server.enqueue(new MockResponse().setBody(history(false, null, "b")));
    assertThrows(
        PaginationException.class,
        () -> client.realtime().conversations().history("c").getNextPage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "101", "1.5", "-1"})
  void historyValidatesEffectiveOptions(String limit) {
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .realtime()
                .conversations()
                .history(
                    "c",
                    RealtimeHistoryParams.builder().build(),
                    RequestOptions.builder().query("limit", limit).build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void conflictDoesNotRetryEvenWithKey() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(409)
            .setHeader("x-should-retry", "true")
            .setBody("{\"error\":{\"code\":\"idempotency_conflict\"}}"));
    assertThrows(
        ApiStatusException.class,
        () ->
            client
                .realtime()
                .conversations()
                .create(
                    createParams(),
                    RequestOptions.builder().idempotencyKey("key").maxRetries(3).build()));
    assertEquals(1, server.getRequestCount());
  }
}
