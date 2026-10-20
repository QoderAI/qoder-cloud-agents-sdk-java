package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class SchedulesBatchesTest {
  MockWebServer api;
  ForwardClient client;
  static final String ID = "id/?#%";
  static final String PAGE = "{\"data\":[],\"has_more\":false}";

  @BeforeEach
  void setup() throws Exception {
    api = new MockWebServer();
    api.start();
    client =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward").toString())
            .pat("test-token")
            .maxRetries(0)
            .build();
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    api.close();
  }

  MockResponse json(String body) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(body);
  }

  RecordedRequest take() throws Exception {
    RecordedRequest r = api.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  static Stream<Arguments> routes() {
    String[][] rows = {
      {"scheduleList", "GET", "/schedules", "", PAGE},
      {
        "scheduleCreate",
        "POST",
        "/schedules",
        "{\"identity_id\":\"idn_1\",\"template_id\":\"tmpl_1\",\"name\":\"manual\",\"environment_id\":\"env_1\",\"initial_events\":[{\"type\":\"user.message\",\"content\":\"hello\"}],\"trigger_policy\":{\"type\":\"manual\"}}",
        "{\"id\":\"sched_1\"}"
      },
      {
        "archiveMany",
        "POST",
        "/schedules/archive",
        "{\"schedule_ids\":[\"sched_1\",\"sched_2\"]}",
        "{\"archived_count\":2}"
      },
      {"scheduleGet", "GET", "/schedules/id%2F%3F%23%25", "", "{\"id\":\"sched_1\"}"},
      {
        "scheduleUpdate",
        "POST",
        "/schedules/id%2F%3F%23%25",
        "{\"trigger_policy\":null,\"sinks\":[],\"metadata\":{\"remove\":null},\"execution\":{\"max_attempts\":2}}",
        "{\"id\":\"sched_1\"}"
      },
      {
        "archive",
        "POST",
        "/schedules/id%2F%3F%23%25/archive",
        "",
        "{\"id\":\"sched_1\",\"archived_at\":\"2026-10-17T00:00:00Z\"}"
      },
      {"pause", "POST", "/schedules/id%2F%3F%23%25/pause", "", "{\"status\":\"paused\"}"},
      {
        "run",
        "POST",
        "/schedules/id%2F%3F%23%25/run",
        "",
        "{\"id\":\"srun_1\",\"status\":\"pending\"}"
      },
      {"unpause", "POST", "/schedules/id%2F%3F%23%25/unpause", "", "{\"status\":\"active\"}"},
      {"runList", "GET", "/schedule_runs", "", PAGE},
      {"runGet", "GET", "/schedule_runs/id%2F%3F%23%25", "", "{\"id\":\"srun_1\"}"},
      {"batchList", "GET", "/batches", "", PAGE},
      {
        "batchCreate",
        "POST",
        "/batches",
        "{\"input_file_id\":\"file_1\",\"completion_window\":\"24h\",\"ignore_idle_window\":true}",
        "{\"id\":\"batch_1\",\"status\":\"validating\"}"
      },
      {"batchGet", "GET", "/batches/id%2F%3F%23%25", "", "{\"id\":\"batch_1\"}"},
      {"cancel", "POST", "/batches/id%2F%3F%23%25/cancel", "", "{\"status\":\"cancelling\"}"},
      {
        "output",
        "GET",
        "/batches/id%2F%3F%23%25/output",
        "",
        "{\"url\":\"https://storage.example/output?secret=signature\",\"expires_at\":\"2026-10-17T00:30:00Z\"}"
      },
      {
        "error",
        "GET",
        "/batches/id%2F%3F%23%25/error",
        "",
        "{\"url\":\"https://storage.example/error?secret=signature\"}"
      },
      {"tasks", "GET", "/batches/id%2F%3F%23%25/tasks", "", PAGE}
    };
    return Arrays.stream(rows)
        .flatMap(
            r -> Stream.of(false, true).map(a -> Arguments.of(r[0], r[1], r[2], r[3], r[4], a)));
  }

  static ScheduleCreateParams createParams() {
    return ScheduleCreateParams.builder()
        .identityId("idn_1")
        .templateId("tmpl_1")
        .name("manual")
        .environmentId("env_1")
        .initialEvents(
            Collections.singletonList(
                ScheduleInitialEvent.builder().type("user.message").content("hello").build()))
        .triggerPolicy(ScheduleTriggerPolicyParams.builder().type("manual").build())
        .build();
  }

  @ParameterizedTest
  @MethodSource("routes")
  void wireContracts(
      String op, String method, String path, String body, String reply, boolean async)
      throws Exception {
    api.enqueue(
        json(reply)
            .setHeader("x-request-id", "req-contract")
            .setHeader("X-Diagnostic", "preserved"));
    ApiResponse<?> response = invoke(op, async);
    assertEquals(200, response.statusCode());
    assertEquals("req-contract", response.requestId());
    assertEquals("preserved", response.header("x-diagnostic"));
    JsonModel model = (JsonModel) response.data();
    assertEquals(reply, model.rawJson());
    assertEquals("req-contract", model.requestId());
    RecordedRequest r = take();
    assertEquals(method, r.getMethod());
    assertEquals("/api/v1/forward" + path, r.getRequestUrl().encodedPath());
    assertEquals("Bearer test-token", r.getHeader("Authorization"));
    assertEquals("operation-key", r.getHeader("Idempotency-Key"));
    if (body.isEmpty()) assertEquals(0, r.getBodySize());
    else
      assertEquals(
          JsonValue.parse(body).toJson(), JsonValue.parse(r.getBody().readUtf8()).toJson());
    if (op.equals("runList") || op.equals("runGet"))
      assertEquals("idn_1", r.getRequestUrl().queryParameter("identity_id"));
    if (op.equals("output") || op.equals("error"))
      assertFalse(model.toString().contains("signature"));
  }

  ApiResponse<?> invoke(String op, boolean a) throws Exception {
    RequestOptions o = RequestOptions.builder().idempotencyKey("operation-key").build();
    ScheduleRunListParams rl = ScheduleRunListParams.builder().identityId("idn_1").build();
    ScheduleRunRetrieveParams rr = ScheduleRunRetrieveParams.builder().identityId("idn_1").build();
    ScheduleArchiveManyParams am =
        ScheduleArchiveManyParams.builder()
            .scheduleIds(Arrays.asList("sched_1", "sched_2"))
            .build();
    ScheduleUpdateParams su =
        ScheduleUpdateParams.builder()
            .triggerPolicy(null)
            .sinks(Collections.emptyList())
            .metadata(Collections.singletonMap("remove", null))
            .execution(ScheduleExecution.builder().maxAttempts(2L).build())
            .build();
    BatchCreateParams bc =
        BatchCreateParams.builder()
            .inputFileId("file_1")
            .completionWindow("24h")
            .ignoreIdleWindow(true)
            .build();
    switch (op) {
      case "scheduleList":
        return a
            ? client.async().schedules().withResponse().list(o).get()
            : client.schedules().withResponse().list(o);
      case "scheduleCreate":
        return a
            ? client.async().schedules().withResponse().create(createParams(), o).get()
            : client.schedules().withResponse().create(createParams(), o);
      case "archiveMany":
        return a
            ? client.async().schedules().withResponse().archiveMany(am, o).get()
            : client.schedules().withResponse().archiveMany(am, o);
      case "scheduleGet":
        return a
            ? client.async().schedules().withResponse().retrieve(ID, o).get()
            : client.schedules().withResponse().retrieve(ID, o);
      case "scheduleUpdate":
        return a
            ? client.async().schedules().withResponse().update(ID, su, o).get()
            : client.schedules().withResponse().update(ID, su, o);
      case "archive":
        return a
            ? client.async().schedules().withResponse().archive(ID, o).get()
            : client.schedules().withResponse().archive(ID, o);
      case "pause":
        return a
            ? client.async().schedules().withResponse().pause(ID, o).get()
            : client.schedules().withResponse().pause(ID, o);
      case "run":
        return a
            ? client.async().schedules().withResponse().run(ID, o).get()
            : client.schedules().withResponse().run(ID, o);
      case "unpause":
        return a
            ? client.async().schedules().withResponse().unpause(ID, o).get()
            : client.schedules().withResponse().unpause(ID, o);
      case "runList":
        return a
            ? client.async().scheduleRuns().withResponse().list(rl, o).get()
            : client.scheduleRuns().withResponse().list(rl, o);
      case "runGet":
        return a
            ? client.async().scheduleRuns().withResponse().retrieve(ID, rr, o).get()
            : client.scheduleRuns().withResponse().retrieve(ID, rr, o);
      case "batchList":
        return a
            ? client.async().batches().withResponse().list(o).get()
            : client.batches().withResponse().list(o);
      case "batchCreate":
        return a
            ? client.async().batches().withResponse().create(bc, o).get()
            : client.batches().withResponse().create(bc, o);
      case "batchGet":
        return a
            ? client.async().batches().withResponse().retrieve(ID, o).get()
            : client.batches().withResponse().retrieve(ID, o);
      case "cancel":
        return a
            ? client.async().batches().withResponse().cancel(ID, o).get()
            : client.batches().withResponse().cancel(ID, o);
      case "output":
        return a
            ? client.async().batches().withResponse().getOutput(ID, o).get()
            : client.batches().withResponse().getOutput(ID, o);
      case "error":
        return a
            ? client.async().batches().withResponse().getError(ID, o).get()
            : client.batches().withResponse().getError(ID, o);
      case "tasks":
        return a
            ? client.async().batches().tasks().withResponse().list(ID, o).get()
            : client.batches().tasks().withResponse().list(ID, o);
      default:
        throw new AssertionError(op);
    }
  }

  @Test
  void mergePatchAndResponseFieldsStayDistinctAndImmutable() {
    Map<String, JsonValue> metadata = new LinkedHashMap<>();
    metadata.put("remove", null);
    List<ScheduleSink> sinks = new ArrayList<>();
    sinks.add(ScheduleSink.builder().type("im_channel").channelPairingId("pair_1").build());
    ScheduleUpdateParams p =
        ScheduleUpdateParams.builder()
            .execution(ScheduleExecution.builder().maxAttempts(2L).build())
            .triggerPolicy(null)
            .sinks(sinks)
            .metadata(metadata)
            .build();
    metadata.put("later", JsonValue.of(true));
    sinks.clear();
    assertTrue(p.field("name").isMissing());
    assertTrue(p.field("trigger_policy").isNull());
    assertTrue(p.field("metadata").get("remove").isNull());
    assertTrue(p.field("metadata").get("later").isMissing());
    assertTrue(p.execution().field("session_mode").isMissing());
    assertEquals("pair_1", p.sinks().get(0).channelPairingId());
    assertThrows(UnsupportedOperationException.class, () -> p.sinks().clear());
    Schedule v =
        new Schedule(
            JsonValue.parse(
                "{\"source\":\"tool\",\"source_session_id\":\"sess_source\",\"status\":\"future_status\",\"archived_at\":null,\"trigger_policy\":{\"type\":\"cron\",\"start_at\":\"start\",\"stop_at\":\"stop\",\"upcoming_runs_at\":[\"next\"],\"last_run_at\":null},\"future\":{\"enabled\":true}}"),
            "req-1");
    assertEquals("tool", v.source());
    assertEquals("sess_source", v.sourceSessionId());
    assertEquals("future_status", v.status());
    assertNull(v.archivedAt());
    assertEquals("next", v.triggerPolicy().upcomingRunsAt().get(0));
    assertEquals("start", v.triggerPolicy().startAt());
    assertTrue(v.field("future").get("enabled").asBoolean());
    ScheduleRun run =
        new ScheduleRun(
            JsonValue.parse(
                "{\"source\":\"tool\",\"source_session_id\":\"sess_source\",\"session_id\":\"sess_execution\",\"status\":\"completed\",\"push_status\":\"failed\",\"attempt\":2,\"duration_ms\":0,\"error\":null,\"trigger_context\":{\"type\":\"manual\"}}"),
            "req-2");
    assertEquals("sess_execution", run.sessionId());
    assertEquals("sess_source", run.sourceSessionId());
    assertEquals("completed", run.status());
    assertEquals("failed", run.pushStatus());
    assertEquals(2L, run.attempt());
    assertEquals(0L, run.durationMs());
    assertNull(run.error());
    assertEquals("manual", run.triggerContext().type());
  }

  @Test
  void boundedArchiveAndRequiredParametersFailBeforeSending() {
    assertThrows(ConfigurationException.class, () -> ScheduleCreateParams.builder().build());
    assertThrows(ConfigurationException.class, () -> ScheduleRunListParams.builder().build());
    assertThrows(ConfigurationException.class, () -> BatchCreateParams.builder().build());
    assertThrows(
        ConfigurationException.class,
        () ->
            BatchCreateParams.builder()
                .inputFileId("f")
                .completionWindow("24h")
                .ignoreIdleWindow(null)
                .build());
    assertThrows(
        ConfigurationException.class, () -> ScheduleExecution.builder().maxAttempts(0L).build());
    assertThrows(
        ConfigurationException.class, () -> ScheduleExecution.builder().maxAttempts(3L).build());
    assertThrows(
        ConfigurationException.class, () -> ScheduleExecution.builder().maxAttempts(null).build());
    assertThrows(
        ConfigurationException.class,
        () -> ScheduleArchiveManyParams.builder().scheduleIds(Collections.emptyList()).build());
    assertThrows(
        ConfigurationException.class,
        () -> ScheduleArchiveManyParams.builder().scheduleIds(Arrays.asList("s", null)).build());
    assertThrows(
        ConfigurationException.class,
        () ->
            ScheduleArchiveManyParams.builder()
                .scheduleIds(Collections.singletonList(" "))
                .build());
    assertThrows(
        ConfigurationException.class,
        () ->
            ScheduleArchiveManyParams.builder()
                .scheduleIds(
                    IntStream.range(0, 51).mapToObj(i -> "s" + i).collect(Collectors.toList()))
                .build());
    List<String> ids = new ArrayList<>(Arrays.asList("s1", "s1", "s2"));
    ScheduleArchiveManyParams.Builder b = ScheduleArchiveManyParams.builder().scheduleIds(ids);
    ids.clear();
    ScheduleArchiveManyParams p = b.build();
    assertEquals(Arrays.asList("s1", "s2"), p.scheduleIds());
    assertEquals(Collections.singleton("schedule_ids"), p.toJsonValue().asObject().keySet());
    assertThrows(UnsupportedOperationException.class, () -> p.scheduleIds().clear());
    ScheduleSink sink = ScheduleSink.builder().type("im_channel").build();
    assertThrows(
        ConfigurationException.class,
        () -> ScheduleUpdateParams.builder().sinks(Arrays.asList(sink, sink)).build());
    assertEquals(0, api.getRequestCount());
  }

  @Test
  void schedulesPageInServerOrderWithPreservedFilters() throws Exception {
    api.enqueue(
        json(
            "{\"data\":[{\"id\":\"z\"},{\"id\":\"a\"}],\"first_id\":\"z\",\"last_id\":\"a\",\"has_more\":true}"));
    api.enqueue(
        json(
            "{\"data\":[{\"id\":\"b\"}],\"first_id\":\"b\",\"last_id\":\"b\",\"has_more\":false}"));
    ScheduleListResponse page =
        client
            .schedules()
            .list(
                ScheduleListParams.builder()
                    .identityId("idn_1")
                    .templateId("tmpl_1")
                    .status("active")
                    .sortBy("upcoming_runs_at")
                    .order("asc")
                    .includeArchived(true)
                    .limit(2)
                    .build(),
                RequestOptions.builder().query("after_id", "start").build());
    assertEquals(1, api.getRequestCount());
    List<String> ids = new ArrayList<>();
    for (Schedule s : page) ids.add(s.id());
    assertEquals(Arrays.asList("z", "a", "b"), ids);
    assertEquals("start", take().getRequestUrl().queryParameter("after_id"));
    RecordedRequest next = take();
    assertEquals("a", next.getRequestUrl().queryParameter("after_id"));
    assertEquals("idn_1", next.getRequestUrl().queryParameter("identity_id"));
    assertEquals("upcoming_runs_at", next.getRequestUrl().queryParameter("sort_by"));
    assertEquals("true", next.getRequestUrl().queryParameter("include_archived"));
    assertEquals("2", next.getRequestUrl().queryParameter("limit"));
  }

  @Test
  void runsPageBackwardAndRejectRepeatedCursor() throws Exception {
    String reply =
        "{\"data\":[{\"id\":\"r1\"}],\"first_id\":\"r1\",\"last_id\":\"r1\",\"has_more\":true}";
    api.enqueue(json(reply));
    api.enqueue(json(reply));
    ScheduleRunListResponse p =
        client
            .async()
            .scheduleRuns()
            .list(
                ScheduleRunListParams.builder()
                    .identityId("idn_1")
                    .scheduleId("s")
                    .hasError(false)
                    .triggerType("manual")
                    .status("completed")
                    .sortBy("triggered_at")
                    .order("desc")
                    .beforeId("r0")
                    .build())
            .get();
    ScheduleRunListResponse q = p.getNextPageAsync().get();
    take();
    RecordedRequest req = take();
    assertEquals("r1", req.getRequestUrl().queryParameter("before_id"));
    assertNull(req.getRequestUrl().queryParameter("after_id"));
    assertEquals("false", req.getRequestUrl().queryParameter("has_error"));
    assertEquals("idn_1", req.getRequestUrl().queryParameter("identity_id"));
    assertThrows(PaginationException.class, q::getNextPage);
  }

  @Test
  void batchTasksUseCustomIdCursorAndKeepDecimalCredits() throws Exception {
    api.enqueue(
        json(
            "{\"data\":[{\"custom_id\":\"task-Z\",\"status\":\"completed\",\"usage\":{\"total_credits\":0.123456789012345678901},\"artifacts\":[{\"file_id\":\"opaque\",\"name\":\"out.txt\",\"size\":0,\"content_type\":\"text/plain\"}],\"error\":null}],\"first_id\":\"task-Z\",\"last_id\":\"task-Z\",\"has_more\":true}"));
    api.enqueue(
        json(
            "{\"data\":[{\"custom_id\":\"task-A\",\"status\":\"failed\",\"usage\":null,\"error\":{\"code\":\"config_error\",\"message\":\"resource"
                + " missing\"}}],\"has_more\":false}"));
    BatchTaskListResponse p =
        client
            .batches()
            .tasks()
            .list(
                "b",
                BatchTaskListParams.builder()
                    .status("completed")
                    .customId("task-Z")
                    .limit(1)
                    .build());
    BatchTask t = p.data().get(0);
    assertEquals(new BigDecimal("0.123456789012345678901"), t.usage().totalCredits());
    assertEquals(0L, t.artifacts().get(0).size());
    assertNull(t.error());
    BatchTaskListResponse next = p.getNextPage();
    assertEquals("config_error", next.data().get(0).error().code());
    assertNull(next.data().get(0).usage());
    assertThrows(PaginationException.class, next::getNextPage);
    take();
    RecordedRequest r = take();
    assertEquals("task-Z", r.getRequestUrl().queryParameter("after_id"));
    assertEquals("task-Z", r.getRequestUrl().queryParameter("custom_id"));
    assertEquals("completed", r.getRequestUrl().queryParameter("status"));
  }

  @Test
  void batchStatusDoesNotInventReadinessOrUsage() {
    Batch b =
        new Batch(
            JsonValue.parse(
                "{\"id\":\"b\",\"status\":\"validating\",\"ignore_idle_window\":true,\"queue_reason\":\"global_capacity\",\"usage\":null,\"request_counts\":{\"total\":3,\"pending\":2,\"failed\":1,\"running\":0}}"),
            "r");
    assertTrue(b.ignoreIdleWindow());
    assertEquals("global_capacity", b.queueReason());
    assertEquals(3L, b.requestCounts().total());
    assertEquals(1L, b.requestCounts().failed());
    assertNull(b.usage());
    assertNull(b.outputFileId());
    assertNull(b.total());
    Batch completed =
        new Batch(
            JsonValue.parse(
                "{\"status\":\"completed\",\"usage\":{\"total_credits\":0},\"total\":3,\"completed\":2,\"failed\":1}"),
            "r");
    assertEquals(BigDecimal.ZERO, completed.usage().totalCredits());
    assertEquals(3L, completed.total());
    assertNull(completed.requestCounts());
    assertTrue(
        BatchCreateParams.builder()
            .inputFileId("f")
            .completionWindow("24h")
            .build()
            .field("ignore_idle_window")
            .isMissing());
  }

  @Test
  void queryBoundariesAndRequestOverridesAreValidated() {
    assertThrows(
        ConfigurationException.class,
        () -> ScheduleListParams.builder().afterId("a").beforeId("b").build());
    assertThrows(ConfigurationException.class, () -> BatchTaskListParams.builder().limit(101));
    assertThrows(ConfigurationException.class, () -> BatchListParams.builder().limit(0));
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .batches()
                .tasks()
                .list("b", RequestOptions.builder().query("before_id", "a").build()));
    assertThrows(
        ConfigurationException.class,
        () -> client.schedules().list(RequestOptions.builder().query("page", "x").build()));
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .batches()
                .list(
                    BatchListParams.builder().afterId("a").build(),
                    RequestOptions.builder().query("before_id", "b").build()));
    assertEquals(0, api.getRequestCount());
  }

  @Test
  void batchesHaveCursorPaginationAndFilters() throws Exception {
    api.enqueue(
        json(
            "{\"data\":[{\"id\":\"b2\"}],\"first_id\":\"b2\",\"last_id\":\"b2\",\"has_more\":true}"));
    api.enqueue(json(PAGE));
    BatchListResponse p =
        client
            .async()
            .batches()
            .list(BatchListParams.builder().status("queued").limit(1).beforeId("b3").build())
            .get();
    p.getNextPage();
    take();
    RecordedRequest r = take();
    assertEquals("b2", r.getRequestUrl().queryParameter("before_id"));
    assertEquals("queued", r.getRequestUrl().queryParameter("status"));
  }

  @Test
  void outputNotReadyIsTypedAndNeverReplayedWithoutSafety() throws Exception {
    api.enqueue(
        json("{\"error\":{\"type\":\"invalid_request_error\",\"code\":\"batch_not_ready\",\"message\":\"Not"
                 + " ready\"}}")
            .setResponseCode(400)
            .setHeader("x-request-id", "req-not-ready"));
    ApiStatusException e =
        assertThrows(ApiStatusException.class, () -> client.batches().getOutput("b"));
    assertEquals("batch_not_ready", e.code());
    assertEquals("req-not-ready", e.requestId());
    take();
    api.enqueue(
        json("{\"error\":{\"message\":\"temporary\"}}")
            .setResponseCode(503)
            .setHeader("Retry-After", "0"));
    assertThrows(
        ApiStatusException.class,
        () -> client.schedules().run("s", RequestOptions.builder().maxRetries(1).build()));
    take();
    assertEquals(2, api.getRequestCount());
  }

  @Test
  void idempotencyAllowsSafeBatchCreateRetry() throws Exception {
    api.enqueue(json("{}").setResponseCode(503).setHeader("Retry-After", "0"));
    api.enqueue(json("{\"id\":\"b\"}"));
    Batch b =
        client
            .batches()
            .create(
                BatchCreateParams.builder().inputFileId("f").completionWindow("24h").build(),
                RequestOptions.builder().maxRetries(1).idempotencyKey("batch-key").build());
    assertEquals("b", b.id());
    RecordedRequest a = take(), r = take();
    assertEquals(a.getBody().readUtf8(), r.getBody().readUtf8());
    assertEquals("batch-key", r.getHeader("Idempotency-Key"));
    assertEquals("1", r.getHeader("X-Qoder-Retry-Count"));
  }

  @Test
  void cancellingAsyncListCancelsNetworkCall() throws Exception {
    api.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    CompletableFuture<ScheduleListResponse> pending = client.async().schedules().list();
    take();
    assertTrue(pending.cancel(true));
    assertTrue(pending.isCancelled());
  }
}
