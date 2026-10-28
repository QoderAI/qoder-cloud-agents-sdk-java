package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class SessionsTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private MockWebServer server;
  private ForwardClient client;
  private static final String SID = "sess/a ?#%", TID = "sthr/b ?#%";

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

  private MockResponse json(String body) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(body);
  }

  private RecordedRequest request() throws Exception {
    RecordedRequest result = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(result);
    return result;
  }

  private String fixture(String name) throws IOException {
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                getClass().getResourceAsStream("/" + name), StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining("\n"));
    }
  }

  static Stream<Arguments> routes() {
    String[][] rows = {
      {
        "mountList",
        "GET",
        "/identities/idn%2F1/templates/tmpl%2F1/memory_stores",
        "",
        "{\"data\":[],\"has_more\":false}"
      },
      {
        "mount",
        "POST",
        "/identities/idn%2F1/templates/tmpl%2F1/memory_stores",
        "{\"memory_store_id\":\"memstore_1\"}",
        "{\"memory_store_id\":\"memstore_1\",\"access\":\"read_only\"}"
      },
      {
        "detach",
        "DELETE",
        "/identities/idn%2F1/templates/tmpl%2F1/memory_stores/memstore%2F1",
        "",
        "{\"id\":\"memstore_1\",\"type\":\"memory_store_binding_deleted\",\"deleted\":true}"
      },
      {"list", "GET", "/sessions", "", "{\"data\":[{\"id\":\"sess_1\"}],\"has_more\":false}"},
      {
        "create",
        "POST",
        "/sessions",
        "{\"identity_id\":\"idn_1\",\"template_id\":\"tmpl_1\",\"title\":\"Hello\"}",
        "{\"id\":\"sess_1\",\"status\":\"idle\"}"
      },
      {"retrieve", "GET", "/sessions/SID", "", "{\"id\":\"sess_1\"}"},
      {
        "update",
        "POST",
        "/sessions/SID",
        "{\"title\":\"Updated\",\"config\":{\"environment_variables\":{}}}",
        "{\"id\":\"sess_1\"}"
      },
      {
        "archive",
        "POST",
        "/sessions/SID/archive",
        "",
        "{\"id\":\"sess_1\",\"archived_at\":\"2026-10-17T00:00:00Z\"}"
      },
      {
        "cancel",
        "POST",
        "/sessions/SID/cancel",
        "",
        "{\"id\":\"sess_1\",\"type\":\"session\",\"status\":\"canceling\"}"
      },
      {
        "eventsList",
        "GET",
        "/sessions/SID/events",
        "",
        "{\"data\":[{\"id\":\"evt_1\",\"type\":\"future.event\"}],\"has_more\":false}"
      },
      {
        "eventsSend",
        "POST",
        "/sessions/SID/events",
        "{\"events\":[{\"type\":\"user.message\",\"content\":[{\"type\":\"text\",\"text\":\"Hello\"}]}]}",
        "{\"data\":[{\"id\":\"evt_1\",\"type\":\"user.message\"}]}"
      },
      {
        "resourceAdd",
        "POST",
        "/sessions/SID/resources",
        "{\"type\":\"file\",\"file_id\":\"file_1\",\"mount_path\":\"/data/workspace/a b.md\"}",
        "{\"id\":\"sesr_1\",\"type\":\"file\",\"file_id\":\"file_1\"}"
      },
      {
        "threadsList",
        "GET",
        "/sessions/SID/threads",
        "",
        "{\"data\":[{\"id\":\"sthr_1\",\"role\":\"child\"}],\"has_more\":false}"
      },
      {
        "threadRetrieve",
        "GET",
        "/sessions/SID/threads/TID",
        "",
        "{\"id\":\"sthr_1\",\"status\":\"idle\"}"
      },
      {
        "threadArchive",
        "POST",
        "/sessions/SID/threads/TID/archive",
        "",
        "{\"id\":\"sthr_1\",\"status\":\"archived\"}"
      },
      {
        "threadEvents",
        "GET",
        "/sessions/SID/threads/TID/events",
        "",
        "{\"data\":[{\"id\":\"evt_1\"}],\"has_more\":false}"
      }
    };
    return Arrays.stream(rows)
        .flatMap(
            r -> Stream.of(false, true).map(a -> Arguments.of(r[0], r[1], r[2], r[3], r[4], a)));
  }

  @ParameterizedTest
  @MethodSource("routes")
  void syncAndAsyncRoutes(
      String operation, String method, String path, String body, String responseBody, boolean async)
      throws Exception {
    int status = operation.equals("cancel") ? 202 : 200;
    server.enqueue(
        json(responseBody).setResponseCode(status).setHeader("x-request-id", "req-" + operation));
    ApiResponse<? extends JsonModel> response =
        invoke(
            operation,
            async,
            RequestOptions.builder()
                .idempotencyKey("logical-operation")
                .header("X-Test", "contract")
                .build());
    assertEquals(status, response.statusCode());
    assertEquals("req-" + operation, response.requestId());
    assertEquals(response.requestId(), response.data().requestId());
    RecordedRequest sent = request();
    assertEquals(method, sent.getMethod());
    assertEquals(
        "/api/v1/forward"
            + path.replace("SID", "sess%2Fa%20%3F%23%25").replace("TID", "sthr%2Fb%20%3F%23%25"),
        sent.getPath());
    assertEquals("Bearer test-pat", sent.getHeader("Authorization"));
    assertEquals("logical-operation", sent.getHeader("Idempotency-Key"));
    assertEquals("contract", sent.getHeader("X-Test"));
    if (operation.equals("create")) {
      com.fasterxml.jackson.databind.node.ObjectNode expected =
          (com.fasterxml.jackson.databind.node.ObjectNode) JSON.readTree(body);
      expected
          .putObject("metadata")
          .put("__qca_sdk_created", "true")
          .put("__qca_sdk_language", "java")
          .put("__qca_sdk_version", sent.getHeader("X-Qoder-Package-Version"));
      body = expected.toString();
    }
    String actual = sent.getBody().readUtf8();
    if (body.isEmpty()) assertEquals("", actual);
    else assertEquals(JSON.readTree(body), JSON.readTree(actual));
    assertEquals(1, server.getRequestCount());
  }

  private ApiResponse<? extends JsonModel> invoke(String op, boolean async, RequestOptions o)
      throws Exception {
    switch (op) {
      case "mountList":
        return async
            ? client
                .async()
                .identities()
                .memoryStores()
                .withResponse()
                .list("idn/1", "tmpl/1", o)
                .get()
            : client.identities().memoryStores().withResponse().list("idn/1", "tmpl/1", o);
      case "mount":
        IdentityMemoryStoreMountParams mount =
            IdentityMemoryStoreMountParams.builder().memoryStoreId("memstore_1").build();
        return async
            ? client
                .async()
                .identities()
                .memoryStores()
                .withResponse()
                .mount("idn/1", "tmpl/1", mount, o)
                .get()
            : client.identities().memoryStores().withResponse().mount("idn/1", "tmpl/1", mount, o);
      case "detach":
        return async
            ? client
                .async()
                .identities()
                .memoryStores()
                .withResponse()
                .detach("idn/1", "tmpl/1", "memstore/1", o)
                .get()
            : client
                .identities()
                .memoryStores()
                .withResponse()
                .detach("idn/1", "tmpl/1", "memstore/1", o);
      case "list":
        SessionListParams list = SessionListParams.builder().build();
        return async
            ? client.async().sessions().withResponse().list(list, o).get()
            : client.sessions().withResponse().list(list, o);
      case "create":
        SessionCreateParams create =
            SessionCreateParams.builder()
                .identityId("idn_1")
                .templateId("tmpl_1")
                .title("Hello")
                .build();
        return async
            ? client.async().sessions().withResponse().create(create, o).get()
            : client.sessions().withResponse().create(create, o);
      case "retrieve":
        return async
            ? client.async().sessions().withResponse().retrieve(SID, o).get()
            : client.sessions().withResponse().retrieve(SID, o);
      case "update":
        SessionUpdateParams update =
            SessionUpdateParams.builder()
                .title("Updated")
                .config(
                    SessionConfig.builder().environmentVariables(Collections.emptyMap()).build())
                .build();
        return async
            ? client.async().sessions().withResponse().update(SID, update, o).get()
            : client.sessions().withResponse().update(SID, update, o);
      case "archive":
        return async
            ? client.async().sessions().withResponse().archive(SID, o).get()
            : client.sessions().withResponse().archive(SID, o);
      case "cancel":
        return async
            ? client.async().sessions().withResponse().cancel(SID, o).get()
            : client.sessions().withResponse().cancel(SID, o);
      case "eventsList":
        SessionEventListParams events = SessionEventListParams.builder().build();
        return async
            ? client.async().sessions().events().withResponse().list(SID, events, o).get()
            : client.sessions().events().withResponse().list(SID, events, o);
      case "eventsSend":
        SessionEventSendParams send = message("Hello");
        return async
            ? client.async().sessions().events().withResponse().send(SID, send, o).get()
            : client.sessions().events().withResponse().send(SID, send, o);
      case "resourceAdd":
        SessionResourceAddParams add =
            SessionResourceAddParams.builder()
                .type("file")
                .fileId("file_1")
                .mountPath("/data/workspace/a b.md")
                .build();
        return async
            ? client.async().sessions().resources().withResponse().add(SID, add, o).get()
            : client.sessions().resources().withResponse().add(SID, add, o);
      case "threadsList":
        SessionThreadListParams threads = SessionThreadListParams.builder().build();
        return async
            ? client.async().sessions().threads().withResponse().list(SID, threads, o).get()
            : client.sessions().threads().withResponse().list(SID, threads, o);
      case "threadRetrieve":
        return async
            ? client.async().sessions().threads().withResponse().retrieve(SID, TID, o).get()
            : client.sessions().threads().withResponse().retrieve(SID, TID, o);
      case "threadArchive":
        return async
            ? client.async().sessions().threads().withResponse().archive(SID, TID, o).get()
            : client.sessions().threads().withResponse().archive(SID, TID, o);
      case "threadEvents":
        SessionThreadEventListParams te = SessionThreadEventListParams.builder().build();
        return async
            ? client
                .async()
                .sessions()
                .threads()
                .events()
                .withResponse()
                .list(SID, TID, te, o)
                .get()
            : client.sessions().threads().events().withResponse().list(SID, TID, te, o);
      default:
        throw new AssertionError(op);
    }
  }

  private SessionEventSendParams message(String text) {
    return SessionEventSendParams.builder()
        .events(
            Collections.singletonList(
                SessionEventParam.builder()
                    .type("user.message")
                    .content(EventContent.text(text))
                    .build()))
        .build();
  }

  @Test
  void typedSessionPreservesPartialResponsesPrecisionAndUnknownFields() throws Exception {
    server.enqueue(json(fixture("session.json")).setHeader("x-request-id", "session-id"));
    Session session = client.sessions().retrieve("sess_1");
    assertEquals("auto", session.template().model().id());
    assertEquals(9007199254740993L, session.template().version());
    assertEquals(new BigDecimal("12.500000000001"), session.usage().totalCredits());
    assertEquals(BigDecimal.ZERO, session.usage().sandboxRuntimeCredits());
    assertEquals(new BigDecimal("30"), session.stats().activeSeconds());
    assertEquals("2026-10-17T01:02:03.123456789Z", session.createdAt());
    assertEquals("session-id", session.resources().get(0).requestId());
    assertEquals("/data/workspace/spec.md", session.resources().get(0).mountPath());
    assertEquals("satisfied", session.outcomeEvaluations().get(0).result());
    assertNull(session.outcomeEvaluations().get(0).explanation());
    assertTrue(session.field("archived_at").isNull());
    assertEquals(9007199254740993L, session.field("future_field").get("count").asLong());
    assertEquals("", session.config().environmentVariables().get("EMPTY"));
    request();
    server.enqueue(json("{\"id\":\"sess_1\",\"status\":\"idle\"}"));
    Session partial = client.sessions().cancel("sess_1");
    assertEquals("idle", partial.status());
    assertNull(partial.template());
    assertNull(partial.config());
    assertNull(partial.usage());
    assertThrows(
        ResponseValidationException.class,
        () ->
            new Session(JsonValue.parse("{\"usage\":{\"total_credits\":\"not a number\"}}"), null)
                .usage()
                .totalCredits());
  }

  @Test
  void allInputEventShapesUseArrayMessagesAndObjectRubric() throws Exception {
    List<SessionEventParam> events =
        Arrays.asList(
            SessionEventParam.builder()
                .type("user.message")
                .content(
                    EventContent.blocks(
                        Arrays.asList(
                            ContentBlock.builder()
                                .type("text")
                                .text("Describe this picture.")
                                .build(),
                            ContentBlock.builder()
                                .type("image")
                                .source(
                                    ImageSource.builder()
                                        .type("base64")
                                        .mediaType("image/png")
                                        .data("test-base64")
                                        .build())
                                .build())))
                .build(),
            SessionEventParam.builder().type("user.interrupt").reason("User stopped").build(),
            SessionEventParam.builder()
                .type("user.tool_confirmation")
                .toolUseId("toolu_1")
                .result("deny")
                .denyMessage("Not needed")
                .build(),
            SessionEventParam.builder()
                .type("user.tool_result")
                .toolUseId("toolu_2")
                .content(EventContent.ofString("tool output"))
                .isError(false)
                .build(),
            SessionEventParam.builder()
                .type("user.custom_tool_result")
                .customToolUseId("custom_1")
                .content(EventContent.text("Done"))
                .isError(true)
                .build(),
            SessionEventParam.builder()
                .type("user.define_outcome")
                .description("Tests pass")
                .rubric(OutcomeRubric.builder().type("text").content("Tests and lint pass").build())
                .maxIterations(3L)
                .build());
    SessionEventSendParams params = SessionEventSendParams.builder().events(events).build();
    assertEquals(
        JSON.readTree(fixture("session-input-events.json")), JSON.readTree(params.rawJson()));
    server.enqueue(
        json("{\"data\":[{\"id\":\"evt_accepted\",\"type\":\"user.message\"}]}")
            .setHeader("x-request-id", "req-input"));
    SessionEventSendResponse response = client.sessions().events().send("sess_1", params);
    assertEquals(
        JSON.readTree(fixture("session-input-events.json")),
        JSON.readTree(request().getBody().readUtf8()));
    assertEquals("evt_accepted", response.data().get(0).id());
    assertEquals("req-input", response.data().get(0).requestId());
  }

  @Test
  void eventVariantsAndFutureTypesArePreserved() throws Exception {
    server.enqueue(json(fixture("session-events.json")));
    List<SessionEvent> events = client.sessions().events().list("sess_1").data();
    assertEquals(8, events.size());
    assertEquals("Done", events.get(0).content().asBlocks().get(0).text());
    assertEquals(
        9007199254740993L,
        events.get(0).content().asBlocks().get(1).field("future_data").get("count").asLong());
    assertEquals("Read", events.get(1).name());
    assertEquals("/data/workspace/spec.md", events.get(1).input().get("file_path").asString());
    assertEquals("file content", events.get(2).content().asString());
    assertFalse(events.get(2).isError());
    assertEquals("end_turn", events.get(3).stopReason().get("type").asString());
    assertEquals("sthr_1", events.get(4).sessionThreadId());
    assertEquals("Hello", events.get(5).delta().get("text").asString());
    assertEquals(3L, events.get(6).iteration());
    assertEquals(
        new BigDecimal("0.00123456789"), events.get(6).usage().get("total_credits").asDecimal());
    assertTrue(events.get(7).field("future_payload").get("keep").asBoolean());
    assertNull(events.get(7).content());
    assertFalse(events.get(0).toString().contains("Done"));
  }

  @Test
  void requestSnapshotsAndRequiredFieldsDoNotChangeConfigSemantics() {
    Map<String, String> env = new LinkedHashMap<>();
    env.put("MODE", "test");
    List<SessionResourceSpec> resources = new ArrayList<>();
    resources.add(SessionResourceSpec.builder().type("file").fileId("file_1").build());
    SessionCreateParams params =
        SessionCreateParams.builder()
            .identityId("idn_1")
            .templateId("tmpl_1")
            .config(SessionConfig.builder().environmentVariables(env).build())
            .resources(resources)
            .build();
    env.put("MODE", "changed");
    resources.clear();
    assertEquals("test", params.config().environmentVariables().get("MODE"));
    assertEquals(1, params.resources().size());
    assertThrows(UnsupportedOperationException.class, () -> params.resources().clear());
    assertTrue(SessionUpdateParams.builder().build().field("config").isMissing());
    assertTrue(SessionUpdateParams.builder().config(null).build().field("config").isNull());
    assertEquals(
        0,
        SessionUpdateParams.builder()
            .config(SessionConfig.builder().environmentVariables(Collections.emptyMap()).build())
            .build()
            .config()
            .environmentVariables()
            .size());
    assertTrue(SessionConfig.builder().build().field("environment_variables").isMissing());
    assertThrows(
        ConfigurationException.class,
        () -> SessionCreateParams.builder().identityId("idn_1").build());
    assertThrows(
        ConfigurationException.class, () -> IdentityMemoryStoreMountParams.builder().build());
    assertThrows(
        ConfigurationException.class,
        () -> SessionResourceAddParams.builder().type("file").build());
    assertThrows(ConfigurationException.class, () -> SessionEventSendParams.builder().build());
    assertThrows(ConfigurationException.class, () -> SessionEventParam.builder().build());
    assertThrows(
        ConfigurationException.class,
        () -> SessionListParams.builder().afterId("a").beforeId("b").build());
    assertThrows(ConfigurationException.class, () -> SessionThreadListParams.builder().limit(101));
    assertThrows(
        ConfigurationException.class,
        () -> SessionEventListParams.builder().types(Arrays.asList("a,b")));
    assertThrows(
        ConfigurationException.class,
        () -> EventContent.blocks(Arrays.asList((ContentBlock) null)));
    assertThrows(ResponseValidationException.class, () -> EventContent.fromJson(JsonValue.of(42)));
  }

  @Test
  void sessionPaginationPreservesEveryFilterAndServerOrder() throws Exception {
    server.enqueue(
        json("{\"data\":[{\"id\":\"sess_b\"}],\"last_id\":\"sess_b\",\"has_more\":true}"));
    server.enqueue(json("{\"data\":[{\"id\":\"sess_a\"}],\"has_more\":false}"));
    List<String> ids = new ArrayList<>(Arrays.asList("idn_1", "idn_2"));
    SessionListParams params =
        SessionListParams.builder()
            .identityIds(ids)
            .templateId("tmpl_1")
            .sourceType("api")
            .search("one & two")
            .createdAtGt("2026-10-17T00:00:00Z")
            .createdAtGte("2026-10-17T00:00:01Z")
            .createdAtLt("2026-10-18T00:00:00Z")
            .createdAtLte("2026-10-18T00:00:01Z")
            .updatedAtGt("2026-10-17T00:00:00Z")
            .updatedAtGte("2026-10-17T00:00:01Z")
            .updatedAtLt("2026-10-18T00:00:00Z")
            .updatedAtLte("2026-10-18T00:00:01Z")
            .includeArchived(true)
            .order("desc")
            .limit(2)
            .build();
    ids.clear();
    SessionListResponse page =
        client.sessions().list(params, RequestOptions.builder().header("X-Trace", "keep").build());
    Iterator<Session> items = page.iterator();
    assertEquals("sess_b", items.next().id());
    assertEquals(1, server.getRequestCount());
    assertEquals("sess_a", items.next().id());
    assertFalse(items.hasNext());
    RecordedRequest first = request(), second = request();
    assertEquals("idn_1,idn_2", first.getRequestUrl().queryParameter("identity_ids"));
    assertEquals("one & two", first.getRequestUrl().queryParameter("search"));
    assertEquals("true", first.getRequestUrl().queryParameter("include_archived"));
    for (String key : first.getRequestUrl().queryParameterNames())
      assertEquals(
          first.getRequestUrl().queryParameter(key),
          second.getRequestUrl().queryParameter(key),
          key);
    assertEquals("sess_b", second.getRequestUrl().queryParameter("after_id"));
    assertEquals("keep", second.getHeader("X-Trace"));
  }

  @Test
  void eventFiltersAndNestedThreadPaginationStayDistinct() throws Exception {
    server.enqueue(
        json("{\"data\":[{\"id\":\"evt_2\"}],\"first_id\":\"evt_2\",\"has_more\":true}"));
    server.enqueue(json("{\"data\":[{\"id\":\"evt_1\"}],\"has_more\":false}"));
    SessionEventListResponse page =
        client
            .sessions()
            .events()
            .list(
                SID,
                SessionEventListParams.builder()
                    .types(Arrays.asList("agent.message", "session.status_idle"))
                    .includeToolCalls(false)
                    .includeThinking(false)
                    .order("desc")
                    .beforeId("evt_3")
                    .limit(1)
                    .build());
    assertEquals("evt_1", page.getNextPageAsync().get().data().get(0).id());
    RecordedRequest first = request(), second = request();
    assertEquals("agent.message,session.status_idle", first.getRequestUrl().queryParameter("type"));
    assertEquals("false", second.getRequestUrl().queryParameter("include_tool_calls"));
    assertEquals("false", second.getRequestUrl().queryParameter("include_thinking"));
    assertEquals("desc", second.getRequestUrl().queryParameter("order"));
    assertEquals("evt_2", second.getRequestUrl().queryParameter("before_id"));
    server.enqueue(
        json(
            "{\"data\":[{\"id\":\"evt_thread_1\"}],\"last_id\":\"evt_thread_1\",\"has_more\":true}"));
    server.enqueue(json("{\"data\":[],\"has_more\":false}"));
    SessionEventListResponse thread =
        client
            .sessions()
            .threads()
            .events()
            .list(SID, TID, SessionThreadEventListParams.builder().limit(1).build());
    assertFalse(thread.getNextPage().hasNextPage());
    first = request();
    second = request();
    assertEquals(
        "/api/v1/forward/sessions/sess%2Fa%20%3F%23%25/threads/sthr%2Fb%20%3F%23%25/events",
        second.getRequestUrl().encodedPath());
    assertEquals(
        new HashSet<>(Arrays.asList("limit")), first.getRequestUrl().queryParameterNames());
    assertEquals("evt_thread_1", second.getRequestUrl().queryParameter("after_id"));
  }

  @Test
  void typedThreadStatsUnknownStateAndPagination() throws Exception {
    server.enqueue(
        json(
            "{\"data\":[{\"id\":\"sthr_1\",\"role\":\"child\",\"status\":\"future_state\",\"parent_thread_id\":\"sthr_main\",\"stop_reason\":{\"type\":\"future_reason\"},\"stats\":{\"active_seconds\":0.25,\"duration_seconds\":20,\"startup_seconds\":1.5},\"usage\":{\"total_credits\":0.125}}],\"last_id\":\"sthr_1\",\"has_more\":true}"));
    server.enqueue(json("{\"data\":[],\"has_more\":false}"));
    SessionThreadListResponse page = client.sessions().threads().list(SID);
    SessionThread thread = page.data().get(0);
    assertEquals("future_state", thread.status());
    assertEquals("future_reason", thread.stopReason().type());
    assertEquals(new BigDecimal("0.25"), thread.stats().activeSeconds());
    assertEquals(new BigDecimal("1.5"), thread.stats().startupSeconds());
    assertEquals(new BigDecimal("0.125"), thread.usage().totalCredits());
    assertEquals("sthr_main", thread.parentThreadId());
    assertFalse(page.getNextPage().hasNextPage());
    request();
    assertEquals("sthr_1", request().getRequestUrl().queryParameter("after_id"));
  }

  @Test
  void memoryMountListIsSinglePageAndDetachDoesNotDeleteStore() throws Exception {
    server.enqueue(
        json(
            "{\"data\":[{\"memory_store_id\":\"memstore_default\",\"access\":\"read_write\",\"system_managed\":true,\"entry_count\":9007199254740993},{\"memory_store_id\":\"memstore_shared\",\"access\":\"read_only\",\"system_managed\":false}],\"has_more\":false,\"first_id\":\"memstore_default\",\"last_id\":\"memstore_shared\"}"));
    IdentityMemoryStoreListResponse list =
        client.identities().memoryStores().list("idn_1", "tmpl_1");
    assertFalse(list.hasMore());
    assertEquals("memstore_default", list.data().get(0).memoryStoreId());
    assertTrue(list.data().get(0).systemManaged());
    assertEquals(9007199254740993L, list.data().get(0).entryCount());
    assertEquals("read_only", list.data().get(1).access());
    assertEquals(1, server.getRequestCount());
    assertNull(request().getRequestUrl().query());
    server.enqueue(
        json(
            "{\"id\":\"memstore_shared\",\"deleted\":true,\"type\":\"memory_store_binding_deleted\"}"));
    DeletedMemoryStoreMount result =
        client.identities().memoryStores().detach("idn_1", "tmpl_1", "memstore_shared");
    assertTrue(result.deleted());
    assertEquals("memory_store_binding_deleted", result.type());
    assertEquals(
        "/api/v1/forward/identities/idn_1/templates/tmpl_1/memory_stores/memstore_shared",
        request().getPath());
  }

  @Test
  void unsafeEventSendDoesNotRetryButExplicitIdempotencyCanRetry() throws Exception {
    server.enqueue(
        json("{\"error\":{\"code\":\"runtime_unavailable\"}}")
            .setResponseCode(503)
            .setHeader("x-request-id", "req-failed"));
    RequestOptions retry = RequestOptions.builder().maxRetries(1).build();
    ApiStatusException error =
        assertThrows(
            ApiStatusException.class,
            () -> client.sessions().events().send("sess_1", message("once"), retry));
    assertEquals(503, error.statusCode());
    assertEquals("req-failed", error.requestId());
    assertEquals(1, server.getRequestCount());
    request();
    server.enqueue(json("{}").setResponseCode(503).setHeader("retry-after-ms", "1"));
    server.enqueue(json("{\"data\":[]}"));
    client
        .sessions()
        .events()
        .send("sess_1", message("with key"), retry.toBuilder().idempotencyKey("key-1").build());
    RecordedRequest first = request(), second = request();
    assertEquals(first.getBody().readUtf8(), second.getBody().readUtf8());
    assertEquals("key-1", second.getHeader("Idempotency-Key"));
    server.enqueue(json("{\"error\":{\"code\":\"thread_not_idle\"}}").setResponseCode(409));
    assertEquals(
        "thread_not_idle",
        assertThrows(
                ApiStatusException.class,
                () -> client.sessions().threads().archive("sess_1", "sthr_1", retry))
            .code());
    assertEquals(4, server.getRequestCount());
  }

  @Test
  void nestedAsyncCallsKeepTimeoutAndCancellation() throws Exception {
    server.enqueue(json("{\"data\":[{\"id\":\"evt_1\"}],\"last_id\":\"evt_1\",\"has_more\":true}"));
    SessionEventListResponse page =
        client
            .sessions()
            .threads()
            .events()
            .list(
                "sess_1",
                "sthr_1",
                SessionThreadEventListParams.builder().build(),
                RequestOptions.builder().totalTimeout(Duration.ofMillis(100)).build());
    request();
    server.enqueue(json("{\"data\":[],\"has_more\":false}").setHeadersDelay(1, TimeUnit.SECONDS));
    assertInstanceOf(
        ApiTimeoutException.class,
        assertThrows(ExecutionException.class, () -> page.getNextPageAsync().get()).getCause());
    request();
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    CompletableFuture<Session> pending = client.async().sessions().retrieve("sess_cancel");
    request();
    assertTrue(pending.cancel(true));
    assertThrows(CancellationException.class, pending::join);
  }
}
