package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class ForwardParityTest {
  private MockWebServer server;
  private ForwardClient client;

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.start();
    client =
        ForwardClient.builder()
            .pat("test-pat")
            .baseUrl(server.url("/api/v1/forward").toString())
            .maxRetries(0)
            .build();
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    server.close();
  }

  private void respond(JsonValue body) {
    server.enqueue(
        new MockResponse().setHeader("X-Request-ID", "req_parity").setBody(body.toJson()));
  }

  private RecordedRequest request() throws Exception {
    RecordedRequest r = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  private static List<JsonValue> fixtures() throws IOException {
    try (BufferedReader r =
        new BufferedReader(
            new InputStreamReader(
                ForwardParityTest.class.getResourceAsStream("/forward-parity-routes.json"),
                StandardCharsets.UTF_8))) {
      return JsonValue.parse(r.lines().collect(Collectors.joining("\n"))).asArray();
    }
  }

  static Stream<Arguments> routes() throws Exception {
    return fixtures().stream()
        .flatMap(
            f ->
                IntStream.range(0, 4)
                    .mapToObj(mode -> Arguments.of(f.get("operation").asString(), mode, f)));
  }

  @ParameterizedTest(name = "{0}, mode={1}")
  @MethodSource("routes")
  void allFifteenOperationsUseOfficialWireContracts(String operation, int mode, JsonValue fixture)
      throws Exception {
    respond(fixture.get("response"));
    Object value = invoke(operation, mode);
    if (value instanceof CompletableFuture)
      value = ((CompletableFuture<?>) value).get(3, TimeUnit.SECONDS);
    if (value instanceof ApiResponse)
      assertEquals("req_parity", ((ApiResponse<?>) value).requestId());
    RecordedRequest r = request();
    assertEquals(fixture.get("method").asString(), r.getMethod());
    assertEquals("/api/v1/forward" + fixture.get("path").asString(), r.getPath());
    assertEquals("Bearer test-pat", r.getHeader("Authorization"));
    assertEquals("java", r.getHeader("X-Qoder-Lang"));
    if (operation.startsWith("search"))
      assertEquals("search-2026-08-31", r.getHeader("x-qoder-beta"));
    String body = r.getBody().readUtf8();
    if (fixture.get("body").isNull()) assertEquals("", body);
    else assertEquals(fixture.get("body").toJson(), JsonValue.parse(body).toJson());
  }

  private DecisionCreateParams decisionParams() {
    return DecisionCreateParams.builder()
        .state(Collections.singletonMap("ready", JsonValue.of(true)))
        .questions(
            Collections.singletonList(
                DecisionQuestion.builder()
                    .type("choice")
                    .instructions("Is it ready?")
                    .choices(
                        Arrays.asList(
                            DecisionChoice.builder().value(true).build(),
                            DecisionChoice.builder().value(false).build()))
                    .build()))
        .build();
  }

  private Object invoke(String operation, int mode) {
    switch (operation) {
      case "searchEnvironment":
        if (mode == 0)
          return client
              .environments()
              .search(
                  EnvironmentSearchParams.builder()
                      .name("dev")
                      .metadata(Collections.singletonMap("team", "sdk"))
                      .createdAtGte("2026-01-01T00:00:00Z")
                      .limit(1)
                      .identityId("idn_owner")
                      .build());
        if (mode == 1)
          return client
              .async()
              .environments()
              .search(
                  EnvironmentSearchParams.builder()
                      .name("dev")
                      .metadata(Collections.singletonMap("team", "sdk"))
                      .createdAtGte("2026-01-01T00:00:00Z")
                      .limit(1)
                      .identityId("idn_owner")
                      .build());
        if (mode == 2)
          return client
              .environments()
              .withResponse()
              .search(
                  EnvironmentSearchParams.builder()
                      .name("dev")
                      .metadata(Collections.singletonMap("team", "sdk"))
                      .createdAtGte("2026-01-01T00:00:00Z")
                      .limit(1)
                      .identityId("idn_owner")
                      .build());
        return client
            .async()
            .environments()
            .withResponse()
            .search(
                EnvironmentSearchParams.builder()
                    .name("dev")
                    .metadata(Collections.singletonMap("team", "sdk"))
                    .createdAtGte("2026-01-01T00:00:00Z")
                    .limit(1)
                    .identityId("idn_owner")
                    .build());
      case "searchFile":
        if (mode == 0)
          return client
              .files()
              .search(
                  FileSearchParams.builder()
                      .name("report")
                      .scopeId("sess_test")
                      .identityId("idn_owner")
                      .build());
        if (mode == 1)
          return client
              .async()
              .files()
              .search(
                  FileSearchParams.builder()
                      .name("report")
                      .scopeId("sess_test")
                      .identityId("idn_owner")
                      .build());
        if (mode == 2)
          return client
              .files()
              .withResponse()
              .search(
                  FileSearchParams.builder()
                      .name("report")
                      .scopeId("sess_test")
                      .identityId("idn_owner")
                      .build());
        return client
            .async()
            .files()
            .withResponse()
            .search(
                FileSearchParams.builder()
                    .name("report")
                    .scopeId("sess_test")
                    .identityId("idn_owner")
                    .build());
      case "searchSkill":
        if (mode == 0)
          return client
              .skills()
              .search(
                  SkillSearchParams.builder()
                      .source("custom")
                      .displayTitle("research")
                      .identityId("idn_owner")
                      .build());
        if (mode == 1)
          return client
              .async()
              .skills()
              .search(
                  SkillSearchParams.builder()
                      .source("custom")
                      .displayTitle("research")
                      .identityId("idn_owner")
                      .build());
        if (mode == 2)
          return client
              .skills()
              .withResponse()
              .search(
                  SkillSearchParams.builder()
                      .source("custom")
                      .displayTitle("research")
                      .identityId("idn_owner")
                      .build());
        return client
            .async()
            .skills()
            .withResponse()
            .search(
                SkillSearchParams.builder()
                    .source("custom")
                    .displayTitle("research")
                    .identityId("idn_owner")
                    .build());
      case "searchVault":
        if (mode == 0)
          return client
              .vaults()
              .search(
                  VaultSearchParams.builder()
                      .name("keys")
                      .includeArchived(true)
                      .identityId("idn_owner")
                      .build());
        if (mode == 1)
          return client
              .async()
              .vaults()
              .search(
                  VaultSearchParams.builder()
                      .name("keys")
                      .includeArchived(true)
                      .identityId("idn_owner")
                      .build());
        if (mode == 2)
          return client
              .vaults()
              .withResponse()
              .search(
                  VaultSearchParams.builder()
                      .name("keys")
                      .includeArchived(true)
                      .identityId("idn_owner")
                      .build());
        return client
            .async()
            .vaults()
            .withResponse()
            .search(
                VaultSearchParams.builder()
                    .name("keys")
                    .includeArchived(true)
                    .identityId("idn_owner")
                    .build());
      case "createDream":
        if (mode == 0)
          return client
              .dreams()
              .create(
                  DreamCreateParams.builder()
                      .inputs(
                          Collections.singletonList(
                              DreamInput.builder()
                                  .type("memory_store")
                                  .memoryStoreId("memstore_test")
                                  .build()))
                      .build());
        if (mode == 1)
          return client
              .async()
              .dreams()
              .create(
                  DreamCreateParams.builder()
                      .inputs(
                          Collections.singletonList(
                              DreamInput.builder()
                                  .type("memory_store")
                                  .memoryStoreId("memstore_test")
                                  .build()))
                      .build());
        if (mode == 2)
          return client
              .dreams()
              .withResponse()
              .create(
                  DreamCreateParams.builder()
                      .inputs(
                          Collections.singletonList(
                              DreamInput.builder()
                                  .type("memory_store")
                                  .memoryStoreId("memstore_test")
                                  .build()))
                      .build());
        return client
            .async()
            .dreams()
            .withResponse()
            .create(
                DreamCreateParams.builder()
                    .inputs(
                        Collections.singletonList(
                            DreamInput.builder()
                                .type("memory_store")
                                .memoryStoreId("memstore_test")
                                .build()))
                    .build());
      case "listDream":
        if (mode == 0)
          return client
              .dreams()
              .list(DreamListParams.builder().includeArchived(true).limit(2).build());
        if (mode == 1)
          return client
              .async()
              .dreams()
              .list(DreamListParams.builder().includeArchived(true).limit(2).build());
        if (mode == 2)
          return client
              .dreams()
              .withResponse()
              .list(DreamListParams.builder().includeArchived(true).limit(2).build());
        return client
            .async()
            .dreams()
            .withResponse()
            .list(DreamListParams.builder().includeArchived(true).limit(2).build());
      case "getDream":
        if (mode == 0) return client.dreams().retrieve("drm/a");
        if (mode == 1) return client.async().dreams().retrieve("drm/a");
        if (mode == 2) return client.dreams().withResponse().retrieve("drm/a");
        return client.async().dreams().withResponse().retrieve("drm/a");
      case "cancelDream":
        if (mode == 0) return client.dreams().cancel("drm/a");
        if (mode == 1) return client.async().dreams().cancel("drm/a");
        if (mode == 2) return client.dreams().withResponse().cancel("drm/a");
        return client.async().dreams().withResponse().cancel("drm/a");
      case "archiveDream":
        if (mode == 0) return client.dreams().archive("drm/a");
        if (mode == 1) return client.async().dreams().archive("drm/a");
        if (mode == 2) return client.dreams().withResponse().archive("drm/a");
        return client.async().dreams().withResponse().archive("drm/a");
      case "listPairing":
        if (mode == 0)
          return client
              .channelPairings()
              .list(
                  ChannelPairingListParams.builder()
                      .channelId("channel_test")
                      .status("active")
                      .targetType("group")
                      .targetExternalId("room")
                      .identityId("idn_test")
                      .templateId("tmpl_test")
                      .limit(2)
                      .build());
        if (mode == 1)
          return client
              .async()
              .channelPairings()
              .list(
                  ChannelPairingListParams.builder()
                      .channelId("channel_test")
                      .status("active")
                      .targetType("group")
                      .targetExternalId("room")
                      .identityId("idn_test")
                      .templateId("tmpl_test")
                      .limit(2)
                      .build());
        if (mode == 2)
          return client
              .channelPairings()
              .withResponse()
              .list(
                  ChannelPairingListParams.builder()
                      .channelId("channel_test")
                      .status("active")
                      .targetType("group")
                      .targetExternalId("room")
                      .identityId("idn_test")
                      .templateId("tmpl_test")
                      .limit(2)
                      .build());
        return client
            .async()
            .channelPairings()
            .withResponse()
            .list(
                ChannelPairingListParams.builder()
                    .channelId("channel_test")
                    .status("active")
                    .targetType("group")
                    .targetExternalId("room")
                    .identityId("idn_test")
                    .templateId("tmpl_test")
                    .limit(2)
                    .build());
      case "getPairing":
        if (mode == 0) return client.channelPairings().retrieve("pair/a");
        if (mode == 1) return client.async().channelPairings().retrieve("pair/a");
        if (mode == 2) return client.channelPairings().withResponse().retrieve("pair/a");
        return client.async().channelPairings().withResponse().retrieve("pair/a");
      case "updatePairing":
        if (mode == 0)
          return client
              .channelPairings()
              .update(
                  "pair/a", ChannelPairingUpdateParams.builder().templateId("tmpl_new").build());
        if (mode == 1)
          return client
              .async()
              .channelPairings()
              .update(
                  "pair/a", ChannelPairingUpdateParams.builder().templateId("tmpl_new").build());
        if (mode == 2)
          return client
              .channelPairings()
              .withResponse()
              .update(
                  "pair/a", ChannelPairingUpdateParams.builder().templateId("tmpl_new").build());
        return client
            .async()
            .channelPairings()
            .withResponse()
            .update("pair/a", ChannelPairingUpdateParams.builder().templateId("tmpl_new").build());
      case "createDecision":
        if (mode == 0) return client.decisions().create(decisionParams());
        if (mode == 1) return client.async().decisions().create(decisionParams());
        if (mode == 2) return client.decisions().withResponse().create(decisionParams());
        return client.async().decisions().withResponse().create(decisionParams());
      case "getDecision":
        if (mode == 0) return client.decisions().retrieve("dec/a");
        if (mode == 1) return client.async().decisions().retrieve("dec/a");
        if (mode == 2) return client.decisions().withResponse().retrieve("dec/a");
        return client.async().decisions().withResponse().retrieve("dec/a");
      case "listDecision":
        if (mode == 0)
          return client
              .decisions()
              .list(
                  DecisionListParams.builder()
                      .sessionId("sess_test")
                      .beforeId("dec_new")
                      .limit(2)
                      .build());
        if (mode == 1)
          return client
              .async()
              .decisions()
              .list(
                  DecisionListParams.builder()
                      .sessionId("sess_test")
                      .beforeId("dec_new")
                      .limit(2)
                      .build());
        if (mode == 2)
          return client
              .decisions()
              .withResponse()
              .list(
                  DecisionListParams.builder()
                      .sessionId("sess_test")
                      .beforeId("dec_new")
                      .limit(2)
                      .build());
        return client
            .async()
            .decisions()
            .withResponse()
            .list(
                DecisionListParams.builder()
                    .sessionId("sess_test")
                    .beforeId("dec_new")
                    .limit(2)
                    .build());
      default:
        throw new AssertionError(operation);
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3})
  void sessionMarkersPreserveCallerMetadataAndCoverEveryCreateEntry(int mode) throws Exception {
    Map<String, JsonValue> metadata = new LinkedHashMap<>();
    metadata.put("ticket", JsonValue.of("example"));
    metadata.put("__qca_sdk_future", JsonValue.of("kept"));
    metadata.put("__qca_sdk_created", JsonValue.of("true"));
    SessionCreateParams params =
        SessionCreateParams.builder().identityId("i").templateId("t").metadata(metadata).build();
    respond(JsonValue.parse("{\"id\":\"sess_test\"}"));
    if (mode == 0) client.sessions().create(params);
    if (mode == 1) client.async().sessions().create(params).get(3, TimeUnit.SECONDS);
    if (mode == 2) client.sessions().withResponse().create(params);
    if (mode == 3) client.async().sessions().withResponse().create(params).get(3, TimeUnit.SECONDS);
    RecordedRequest created = request();
    JsonValue actual = JsonValue.parse(created.getBody().readUtf8()).get("metadata");
    assertEquals("true", actual.get("__qca_sdk_created").asString());
    assertEquals("java", actual.get("__qca_sdk_language").asString());
    assertEquals(
        created.getHeader("X-Qoder-Package-Version"), actual.get("__qca_sdk_version").asString());
    assertEquals("example", actual.get("ticket").asString());
    assertEquals("kept", actual.get("__qca_sdk_future").asString());
    assertEquals(3, metadata.size());
    assertEquals(3, params.metadata().size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"{}", "{\"metadata\":null}"})
  void missingAndNullMetadataAreMarked(String json) throws Exception {
    respond(JsonValue.parse("{\"id\":\"sess_test\"}"));
    client.sessions().create(SessionCreateParams.fromJson(JsonValue.parse(json)));
    assertEquals(
        3, JsonValue.parse(request().getBody().readUtf8()).get("metadata").asObject().size());
  }

  @Test
  void conflictsFailBeforeHttpAndUpdatesAreNotMarked() throws Exception {
    for (String key :
        Arrays.asList("__qca_sdk_created", "__qca_sdk_language", "__qca_sdk_version")) {
      for (JsonValue value :
          Arrays.asList(JsonValue.of("wrong"), JsonValue.of(true), JsonValue.of(null))) {
        SessionCreateParams params =
            SessionCreateParams.builder()
                .identityId("i")
                .templateId("t")
                .metadata(Collections.singletonMap(key, value))
                .build();
        assertThrows(ConfigurationException.class, () -> client.sessions().create(params));
        assertThrows(
            ConfigurationException.class,
            () -> client.async().sessions().withResponse().create(params));
      }
    }
    assertEquals(0, server.getRequestCount());
    respond(JsonValue.parse("{\"id\":\"sess_test\"}"));
    client
        .sessions()
        .update(
            "sess_test",
            SessionUpdateParams.builder()
                .metadata(Collections.singletonMap("ticket", JsonValue.of("new")))
                .build());
    assertEquals("{\"metadata\":{\"ticket\":\"new\"}}", request().getBody().readUtf8());
  }

  @Test
  void fieldsUseCurrentContractAndRetainNullAndEmpty() throws Exception {
    assertEquals(
        "{\"type\":\"text\",\"content\":\"accepted\"}",
        OutcomeRubric.builder().type("text").text("accepted").build().rawJson());
    assertEquals(
        "accepted",
        OutcomeRubric.fromJson(JsonValue.parse("{\"type\":\"text\",\"content\":\"accepted\"}"))
            .content());
    assertEquals(
        "{\"type\":\"file\",\"file_id\":\"file_rubric\"}",
        OutcomeRubric.builder().type("file").fileId("file_rubric").build().rawJson());
    assertTrue(
        IdentityCreateParams.builder()
            .externalId("external")
            .autoMemoryStore(null)
            .build()
            .field("auto_memory_store")
            .isNull());
    assertEquals(
        Boolean.FALSE,
        IdentityUpdateParams.builder().autoMemoryStore(false).build().autoMemoryStore());
    assertEquals(
        Boolean.FALSE,
        new Identity(JsonValue.parse("{\"auto_memory_store\":false}"), null).autoMemoryStore());
    assertTrue(ScheduleUpdateParams.builder().resources(null).build().field("resources").isNull());
    assertEquals(
        "[]",
        ScheduleUpdateParams.builder()
            .resources(Collections.emptyList())
            .build()
            .field("resources")
            .toJson());
    respond(
        JsonValue.parse(
            "{\"id\":\"run_test\",\"initial_events\":[],\"initial_events_version\":0}"));
    ScheduleRun run =
        client
            .scheduleRuns()
            .retrieve(
                "run_test", ScheduleRunRetrieveParams.builder().includeInitialEvents(true).build());
    assertEquals(
        "/api/v1/forward/schedule_runs/run_test?include_initial_events=true", request().getPath());
    assertEquals(Collections.emptyList(), run.initialEvents());
    assertEquals(Long.valueOf(0), run.initialEventsVersion());
    assertNull(new ScheduleRun(JsonValue.parse("{}"), null).initialEvents());
    respond(JsonValue.parse("{\"memory_store_id\":\"m\",\"access\":\"read_write\"}"));
    client
        .identities()
        .memoryStores()
        .mount(
            "i",
            "t",
            IdentityMemoryStoreMountParams.builder()
                .memoryStoreId("m")
                .access("read_write")
                .build());
    JsonValue mount = JsonValue.parse(request().getBody().readUtf8());
    assertEquals("read_write", mount.get("access").asString());
  }

  @Test
  void usageEventsPreserveDecimalPrecisionAndAbsentFields() {
    SessionEvent snapshot =
        new SessionEvent(
            JsonValue.parse(
                "{\"type\":\"session.usage\",\"model_credits\":0,\"sandbox_runtime_credits\":0.25,\"total_credits\":9007199254740993.25}"),
            null);
    assertEquals(BigDecimal.ZERO, snapshot.modelCredits());
    assertEquals(new BigDecimal("0.25"), snapshot.sandboxRuntimeCredits());
    assertEquals(new BigDecimal("9007199254740993.25"), snapshot.totalCredits());
    SessionEvent model =
        new SessionEvent(
            JsonValue.parse(
                "{\"type\":\"span.model_request_end\",\"model_usage\":{\"credits\":1.25}}"),
            null);
    assertEquals(new BigDecimal("1.25"), model.modelUsageCredits());
    SessionEvent compaction =
        new SessionEvent(
            JsonValue.parse(
                "{\"type\":\"agent.thread_context_compacted\",\"usage\":{\"credits\":0.5}}"),
            null);
    assertEquals(new BigDecimal("0.5"), compaction.usageCredits());
    assertNull(model.usageCredits());
    assertNull(model.totalCredits());
    assertNull(
        new SessionEvent(JsonValue.parse("{\"type\":\"future.event\"}"), null).modelUsageCredits());
  }
}
