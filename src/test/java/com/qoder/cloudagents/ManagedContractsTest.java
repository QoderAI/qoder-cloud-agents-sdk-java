package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.managed.*;
import com.qoder.cloudagents.streaming.EventStream;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class ManagedContractsTest {
  private MockWebServer server;
  private ManagedClient client;

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.start();
    client =
        ManagedClient.builder()
            .baseUrl(server.url("/api/v1/cloud").toString())
            .pat("test-pat")
            .maxRetries(0)
            .build();
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    server.close();
  }

  void respond(String body) {
    server.enqueue(new MockResponse().setHeader("X-Request-ID", "req_contract").setBody(body));
  }

  RecordedRequest request() throws Exception {
    RecordedRequest r = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  JsonValue body() throws Exception {
    return JsonValue.parse(request().getBody().readUtf8());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0.01", "100.00", "999999999999.99", "1"})
  void budgetUsesAnExactDecimalString(String cost) throws Exception {
    respond("{\"id\":\"sess_test\"}");
    client
        .sessions()
        .create(
            SessionCreateParams.builder()
                .agent("agent_test")
                .environmentId("env_test")
                .budget(SessionBudget.builder().type("limit").maxCreditCost(cost).build())
                .build());
    JsonValue b = body().get("budget");
    assertEquals(cost, b.get("max_credit_cost").asString());
    assertTrue(b.get("max_list_cost").isMissing());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "0.00", "-1", "1.001", "1000000000000", "1e2", "NaN", "", ".5"})
  void invalidBudgetCannotBeSent(String cost) {
    assertThrows(
        ConfigurationException.class,
        () -> SessionBudget.builder().type("limit").maxCreditCost(cost).build());
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void omissionNullAndMergePatchRemainDistinct() throws Exception {
    Map<String, JsonValue> patch = new LinkedHashMap<>();
    patch.put("remove", JsonValue.of(null));
    respond("{\"id\":\"sess_test\"}");
    client
        .sessions()
        .update(
            "sess_test",
            SessionUpdateParams.builder()
                .budget(null)
                .title(null)
                .metadata(patch)
                .environmentVariables(null)
                .build());
    JsonValue b = body();
    assertTrue(b.get("budget").isNull());
    assertTrue(b.get("title").isNull());
    assertTrue(b.get("metadata").get("remove").isNull());
    assertTrue(b.get("environment_variables").isNull());
    assertTrue(SessionUpdateParams.builder().build().field("budget").isMissing());
    assertEquals(1, patch.size());
    DeploymentUpdateParams deployment =
        DeploymentUpdateParams.builder()
            .schedule(null)
            .environmentVariables(null)
            .metadata(Collections.singletonMap("remove", (String) null))
            .build();
    assertTrue(deployment.field("schedule").isNull());
    assertNull(deployment.metadata().get("remove"));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3})
  void creationMarksOnlyReservedKeysWithoutMutatingCallerMetadata(int mode) throws Exception {
    Map<String, JsonValue> custom = new LinkedHashMap<>();
    custom.put("ticket", JsonValue.of("sdk-contract"));
    custom.put("__qca_sdk_future", JsonValue.of("kept"));
    custom.put("__qca_sdk_created", JsonValue.of("true"));
    SessionCreateParams params =
        SessionCreateParams.builder()
            .agent("agent_test")
            .environmentId("env_test")
            .metadata(custom)
            .build();
    respond("{\"id\":\"sess_test\"}");
    if (mode == 0) client.sessions().create(params);
    else if (mode == 1) client.async().sessions().create(params).get();
    else if (mode == 2) client.sessions().withResponse().create(params);
    else client.async().sessions().withResponse().create(params).get();
    RecordedRequest r = request();
    JsonValue m = JsonValue.parse(r.getBody().readUtf8()).get("metadata");
    assertEquals(5, m.asObject().size());
    assertEquals("java", m.get("__qca_sdk_language").asString());
    assertEquals(r.getHeader("X-Qoder-Package-Version"), m.get("__qca_sdk_version").asString());
    assertEquals("kept", m.get("__qca_sdk_future").asString());
    assertEquals(3, custom.size());
    assertEquals(3, params.metadata().size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"__qca_sdk_created", "__qca_sdk_language", "__qca_sdk_version"})
  void conflictingProvenanceFailsBeforeHttp(String key) {
    SessionCreateParams params =
        SessionCreateParams.builder()
            .agent("agent_test")
            .environmentId("env_test")
            .metadata(Collections.singletonMap(key, JsonValue.of(false)))
            .build();
    assertThrows(ConfigurationException.class, () -> client.sessions().create(params));
    assertThrows(ConfigurationException.class, () -> client.async().sessions().create(params));
    assertThrows(
        ConfigurationException.class, () -> client.sessions().withResponse().create(params));
    assertThrows(
        ConfigurationException.class,
        () -> client.async().sessions().withResponse().create(params));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void agentVersionsUseOpaqueCursorNotRepeatedAgentId() throws Exception {
    respond(
        "{\"data\":[{\"id\":\"agent_same\",\"version\":1}],\"last_id\":\"agent_same\",\"has_more\":true,\"next_page\":\"opaque-v1\"}");
    respond(
        "{\"data\":[{\"id\":\"agent_same\",\"version\":2}],\"last_id\":\"agent_same\",\"has_more\":false,\"next_page\":null}");
    AgentVersionListResponse first =
        client
            .agents()
            .versions()
            .list("agent_same", AgentVersionListParams.builder().limit(1).build());
    assertTrue(first.hasNextPage());
    AgentVersionListResponse second = first.getNextPageAsync().get();
    assertEquals(Long.valueOf(2), second.data().get(0).version());
    assertFalse(second.hasNextPage());
    request();
    RecordedRequest next = request();
    assertEquals("opaque-v1", next.getRequestUrl().queryParameter("page"));
    assertEquals("1", next.getRequestUrl().queryParameter("limit"));
    assertNull(next.getRequestUrl().queryParameter("after_id"));
  }

  @Test
  void searchKeepsFiltersAndBetaAcrossPagesAndRejectsRepeatedCursor() throws Exception {
    String page =
        "{\"data\":[{\"id\":\"agent_a\"}],\"last_id\":\"agent_a\",\"has_more\":true,\"next_page\":\"opaque\"}";
    respond(page);
    respond(page);
    AgentListResponse first =
        client
            .agents()
            .search(
                AgentSearchParams.builder()
                    .name("qa")
                    .metadata(Collections.singletonMap("team", "sdk"))
                    .limit(1)
                    .build(),
                RequestOptions.builder()
                    .header("x-qoder-beta", "other-beta")
                    .header("X-Test", "kept")
                    .build());
    AgentListResponse second = first.getNextPage();
    RecordedRequest a = request(), b = request();
    JsonValue body = JsonValue.parse(b.getBody().readUtf8());
    assertEquals("opaque", body.get("page").asString());
    assertEquals("qa", body.get("name").asString());
    assertEquals("sdk", body.get("metadata").get("team").asString());
    assertEquals(0, b.getRequestUrl().querySize());
    assertTrue(b.getHeader("x-qoder-beta").contains("other-beta"));
    assertTrue(b.getHeader("x-qoder-beta").contains("search-2026-08-31"));
    assertEquals("kept", b.getHeader("X-Test"));
    assertThrows(PaginationException.class, second::getNextPage);
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void managedFiltersUseStatusesAndTypesWithDateComparisons() throws Exception {
    respond("{\"data\":[],\"has_more\":false}");
    client
        .sessions()
        .list(
            SessionListParams.builder()
                .statuses(Arrays.asList("idle", "running"))
                .agentVersion(2)
                .createdAtGt("2026-10-01T00:00:00Z")
                .build());
    RecordedRequest sessions = request();
    assertEquals("idle,running", sessions.getRequestUrl().queryParameter("statuses"));
    assertEquals("2", sessions.getRequestUrl().queryParameter("agent_version"));
    respond("{\"data\":[],\"has_more\":false}");
    client
        .sessions()
        .events()
        .list(
            "sess_test",
            SessionEventListParams.builder()
                .types(Arrays.asList("user.message", "session.usage"))
                .createdAtLte("2026-10-28T00:00:00Z")
                .order("desc")
                .build());
    RecordedRequest events = request();
    assertEquals("user.message,session.usage", events.getRequestUrl().queryParameter("types"));
    assertNull(events.getRequestUrl().queryParameter("type"));
    assertNotNull(events.getRequestUrl().queryParameter("created_at[lte]"));
  }

  @Test
  void sessionRuntimePatchUsesFieldsAndExplicitBeta() throws Exception {
    SessionAgentPatch patch =
        SessionAgentPatch.builder()
            .model(
                ModelConfig.builder().id("ultimate").effort("low").contextWindow(200000L).build())
            .system("new prompt")
            .skills(Collections.emptyList())
            .build();
    respond(
        "{\"id\":\"sess_test\",\"agent\":{\"version\":2,\"model\":{\"id\":\"ultimate\",\"effective_context_window\":200000}}}");
    Session s =
        client
            .sessions()
            .update(
                "sess_test",
                SessionUpdateParams.builder().agent(patch).build(),
                RequestOptions.builder()
                    .header("x-qoder-beta", "session-agent-patch-2026-07-21")
                    .build());
    RecordedRequest r = request();
    assertEquals("session-agent-patch-2026-07-21", r.getHeader("x-qoder-beta"));
    JsonValue b = JsonValue.parse(r.getBody().readUtf8()).get("agent");
    assertTrue(b.get("id").isMissing());
    assertTrue(b.get("version").isMissing());
    assertEquals(Long.valueOf(200000), s.agent().model().effectiveContextWindow());
    assertEquals(Long.valueOf(2), s.agent().version());
    assertThrows(
        ConfigurationException.class,
        () ->
            SessionAgentPatch.builder().additionalField("id", JsonValue.of("agent_wrong")).build());
  }

  @Test
  void agentReferencesMultiagentAndVersionConcurrencyAreTyped() {
    assertEquals("\"agent_a\"", AgentReference.of("agent_a").toJsonValue().toJson());
    assertEquals(
        Long.valueOf(0),
        AgentReference.builder().type("agent").id("agent_a").version(0).build().version());
    assertThrows(
        ConfigurationException.class, () -> AgentReference.builder().id("agent_a").build());
    assertThrows(
        ConfigurationException.class, () -> AgentUpdateParams.builder().name("changed").build());
    MultiagentConfig roster =
        MultiagentConfig.builder()
            .type("coordinator")
            .agents(
                Arrays.asList(
                    MultiagentEntry.of("agent_a"),
                    MultiagentEntry.builder().type("agent").id("agent_b").version("latest").build(),
                    MultiagentEntry.builder().type("self").build(),
                    MultiagentEntry.builder().type("advisor").model("ultimate").build()))
            .build();
    assertTrue(roster.agents().get(0).isString());
    assertEquals("latest", roster.agents().get(1).version().asString());
    assertEquals("ultimate", roster.agents().get(3).model().id());
    assertThrows(
        ConfigurationException.class,
        () ->
            ModelConfig.builder()
                .id("ultimate")
                .additionalField("speed", JsonValue.of("fast"))
                .build());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3})
  void bothSseRoutesDecodeCreditsAndResumeHeaders(int mode) throws Exception {
    boolean thread = mode >= 2, async = mode % 2 == 1;
    server.enqueue(
        new MockResponse()
            .setHeader("Content-Type", "text/event-stream")
            .setHeader("X-Request-ID", "req_sse")
            .setBody(
                "id: evt_usage\n"
                    + "data:"
                    + " {\"id\":\"evt_usage\",\"type\":\"session.usage\",\"model_credits\":0.123456789,\"sandbox_runtime_credits\":0.01,\"total_credits\":0.133456789}\n\n"
                    + "id: evt_end\n"
                    + "data: {\"id\":\"evt_end\",\"type\":\"session.status_terminated\"}\n\n"));
    RequestOptions options =
        RequestOptions.builder().header("Last-Event-ID", "evt_override").build();
    SessionEventStreamParams params =
        SessionEventStreamParams.builder()
            .lastEventId("evt_old")
            .eventDeltas(Arrays.asList("agent.message", "agent.thinking"))
            .build();
    SessionThreadEventStreamParams tp =
        SessionThreadEventStreamParams.builder().lastEventId("evt_old").build();
    EventStream<SessionEvent> stream =
        thread
            ? (async
                ? client
                    .async()
                    .sessions()
                    .threads()
                    .events()
                    .streamEvents("sess_test", "sth_test", tp, options)
                    .get()
                : client
                    .sessions()
                    .threads()
                    .events()
                    .streamEvents("sess_test", "sth_test", tp, options))
            : (async
                ? client
                    .async()
                    .sessions()
                    .events()
                    .streamEvents("sess_test", params, options)
                    .get()
                : client.sessions().events().streamEvents("sess_test", params, options));
    try (EventStream<SessionEvent> owned = stream) {
      Iterator<SessionEvent> events = owned.iterator();
      SessionEvent usage = events.next();
      assertEquals(new BigDecimal("0.123456789"), usage.modelCredits());
      assertEquals(new BigDecimal("0.133456789"), usage.totalCredits());
      assertEquals("session.status_terminated", events.next().type());
      assertFalse(events.hasNext());
      assertEquals("req_sse", owned.response().requestId());
      assertEquals("evt_end", owned.lastEventId());
    }
    RecordedRequest r = request();
    assertEquals("evt_override", r.getHeader("Last-Event-ID"));
    assertEquals(
        thread
            ? "/api/v1/cloud/sessions/sess_test/threads/sth_test/stream"
            : "/api/v1/cloud/sessions/sess_test/events/stream",
        r.getRequestUrl().encodedPath());
    assertEquals(thread ? 0 : 2, r.getRequestUrl().querySize());
  }

  @Test
  void usageNestedCreditsAndUnknownFieldsArePreserved() {
    Session s =
        Session.fromJson(
            JsonValue.parse(
                "{\"usage\":{\"model_credits\":1.25,\"sandbox_runtime_credits\":0.5,\"total_credits\":1.75},\"future\":{\"enabled\":true}}"));
    assertEquals(new BigDecimal("1.75"), s.usage().totalCredits());
    assertTrue(s.field("future").get("enabled").asBoolean());
    SessionEvent e =
        new SessionEvent(
            JsonValue.parse(
                "{\"model_usage\":{\"credits\":0.0000123},\"usage\":{\"credits\":0.00123}}"),
            null);
    assertEquals(new BigDecimal("0.0000123"), e.modelUsageCredits());
    assertEquals(new BigDecimal("0.00123"), e.usageCredits());
  }

  @Test
  void selfHostedPollMayReturnNullAndHeartbeatUsesQueryAndWorkerHeader() throws Exception {
    respond("null");
    assertNull(
        client
            .environments()
            .work()
            .poll(
                "env_test",
                WorkPollParams.builder().blockMs(5).reclaimOlderThanMs(1000).build(),
                RequestOptions.builder().header("Worker-ID", "worker_test").build()));
    RecordedRequest poll = request();
    assertEquals("worker_test", poll.getHeader("Worker-ID"));
    assertEquals("5", poll.getRequestUrl().queryParameter("block_ms"));
    respond("{\"type\":\"work_heartbeat\",\"lease_extended\":true,\"ttl_seconds\":30}");
    WorkHeartbeat h =
        client
            .environments()
            .work()
            .heartbeat(
                "env_test",
                "work_test",
                WorkHeartbeatParams.builder()
                    .expectedLastHeartbeat("NO_HEARTBEAT")
                    .desiredTtlSeconds(30)
                    .build());
    RecordedRequest r = request();
    assertEquals("", r.getBody().readUtf8());
    assertEquals("NO_HEARTBEAT", r.getRequestUrl().queryParameter("expected_last_heartbeat"));
    assertTrue(h.leaseExtended());
    assertEquals(Long.valueOf(30), h.ttlSeconds());
  }

  @Test
  void retryCannotReplayWorkerClaimsOrCredentialRotation() throws Exception {
    try (ManagedClient retry = client.withOptions(b -> b.maxRetries(2))) {
      server.enqueue(
          new MockResponse()
              .setResponseCode(500)
              .setBody("{\"error\":{\"type\":\"api_error\",\"message\":\"failed\"}}"));
      assertThrows(ApiStatusException.class, () -> retry.environments().work().poll("env_test"));
      request();
      assertEquals(1, server.getRequestCount());
    }
  }

  @Test
  void oauthSecretStaysInBodyAndResponseProvidesCallbackOrigin() throws Exception {
    respond(
        "{\"authorization_url\":\"https://provider.example.com/authorize\",\"state\":\"test-state\",\"callback_origin\":\"https://api.qoder.com\"}");
    OAuthStartResponse result =
        client
            .oauth()
            .start(
                OAuthStartParams.builder()
                    .vaultId("vault_test")
                    .mcpServerUrl("https://mcp.example.com")
                    .clientId("test-client")
                    .clientSecret("test-secret")
                    .build());
    RecordedRequest r = request();
    assertEquals(0, r.getRequestUrl().querySize());
    assertEquals(
        "test-secret", JsonValue.parse(r.getBody().readUtf8()).get("client_secret").asString());
    assertEquals("https://api.qoder.com", result.callbackOrigin());
    assertFalse(result.toString().contains("test-state"));
  }

  @Test
  void environmentStorageGitCredentialsAndResourceUpdatesHaveCorrectShapes() {
    EnvironmentStorageBucket bucket =
        EnvironmentStorageBucket.builder()
            .name("docs")
            .provider("aliyun-oss")
            .region("cn-hangzhou")
            .bucket("test-bucket")
            .access("read_only")
            .credential(
                EnvironmentStorageCredential.builder()
                    .type("ram_role")
                    .roleArn("acs:ram::test:role/qa")
                    .build())
            .build();
    EnvironmentConfig env =
        EnvironmentConfig.builder()
            .type("cloud")
            .storage(
                EnvironmentStorage.builder().buckets(Collections.singletonList(bucket)).build())
            .build();
    assertEquals(
        "ram_role",
        env.field("storage")
            .get("buckets")
            .asArray()
            .get(0)
            .get("credential")
            .get("type")
            .asString());
    SessionResourceSpec git =
        SessionResourceSpec.builder()
            .type("git_repository")
            .url("https://user@git.example.com/repo.git")
            .password("test-secret")
            .checkout(RepositoryCheckout.builder().type("branch").name("main").build())
            .build();
    assertEquals("test-secret", git.password());
    assertFalse(git.toString().contains("test-secret"));
    assertThrows(
        ConfigurationException.class,
        () ->
            SessionResourceCreateParams.builder()
                .type("git_repository")
                .fileId("file_test")
                .build());
  }

  @Test
  void nonPaginatedModelsAndDreamsDoNotRequireCursorFields() throws Exception {
    respond(
        "{\"data\":[{\"id\":\"ultimate\",\"source\":\"qoder\",\"available_context_windows\":[200000]}]}");
    Model m = client.models().list().data().get(0);
    assertNull(m.priceFactor());
    assertEquals(Long.valueOf(200000), m.availableContextWindows().get(0));
    request();
    respond("{\"data\":[{\"id\":\"drm_test\",\"session_id\":\"sess_dream\"}]}");
    assertEquals("sess_dream", client.dreams().list().data().get(0).sessionId());
    request();
  }

  @Test
  void fileScopeAndMemoryStoreCountersDecodeWithoutStringCoercion() {
    FileMetadata f =
        FileMetadata.fromJson(
            JsonValue.parse(
                "{\"scope\":{\"id\":\"sess_test\",\"type\":\"session\"},\"metadata\":{\"number\":1}}"));
    assertEquals("sess_test", f.scope().id());
    assertEquals(1L, f.metadata().get("number").asLong());
    MemoryStore m =
        MemoryStore.fromJson(
            JsonValue.parse("{\"entry_count\":2,\"total_size\":123,\"status\":\"active\"}"));
    assertEquals(Long.valueOf(2), m.entryCount());
    assertEquals(Long.valueOf(123), m.totalSize());
    assertEquals("active", m.status());
  }

  @Test
  void asyncCancellationAndDerivedClientsKeepOwnership() throws Exception {
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    CompletableFuture<ModelListResponse> pending = client.async().models().list();
    request();
    assertTrue(pending.cancel(true));
    assertTrue(pending.isCancelled());
    client.async().close();
    respond("{\"data\":[]}");
    assertNotNull(client.models().list());
    request();
    try (ManagedClient derived = client.withOptions(b -> b.pat("other-pat"))) {
      respond("{\"data\":[]}");
      derived.models().list();
      assertEquals("Bearer other-pat", request().getHeader("Authorization"));
    }
    respond("{\"data\":[]}");
    client.models().list();
    assertEquals("Bearer test-pat", request().getHeader("Authorization"));
  }

  @Test
  void staticSatAuthenticationAndTypedErrorContextAreRetained() throws Exception {
    try (AsyncManagedClient sat =
        ManagedClient.builder()
            .baseUrl(server.url("/api/v1/cloud").toString())
            .authMode(AuthMode.SA)
            .accessToken("test-sat")
            .maxRetries(0)
            .buildAsync()) {
      server.enqueue(
          new MockResponse()
              .setResponseCode(409)
              .setHeader("X-Request-ID", "req_conflict")
              .setBody(
                  "{\"error\":{\"type\":\"conflict_error\",\"message\":\"version changed\"}}"));
      ExecutionException ex =
          assertThrows(
              ExecutionException.class,
              () ->
                  sat.agents()
                      .update(
                          "agent_test", AgentUpdateParams.builder().version(1).name("new").build())
                      .get());
      assertTrue(ex.getCause() instanceof ApiStatusException);
      assertEquals("Bearer test-sat", request().getHeader("Authorization"));
    }
  }

  @ParameterizedTest
  @ValueSource(longs = {0, -1, 1000, 5000})
  void invalidLongPollingWindowIsRejected(long value) {
    assertThrows(
        ConfigurationException.class, () -> WorkPollParams.builder().blockMs(value).build());
  }

  @Test
  void storageDownloadDoesNotForwardApiAuthenticationOrInterceptors() throws Exception {
    try (MockWebServer storage = new MockWebServer()) {
      storage.start();
      respond("{\"url\":\"" + storage.url("/file?signature=test") + "\"}");
      storage.enqueue(new MockResponse().setBody("contents"));
      try (ManagedClient custom =
              client.withOptions(
                  b ->
                      b.defaultHeader("X-Private", "private")
                          .interceptor(
                              (r, chain) -> chain.proceed(r.withHeader("X-Intercepted", "api"))));
          FileDownload download = custom.files().download("file_test")) {
        assertEquals('c', download.body().read());
      }
      RecordedRequest api = request();
      assertEquals("Bearer test-pat", api.getHeader("Authorization"));
      assertEquals("api", api.getHeader("X-Intercepted"));
      RecordedRequest unsigned = storage.takeRequest(2, TimeUnit.SECONDS);
      assertNotNull(unsigned);
      assertNull(unsigned.getHeader("Authorization"));
      assertNull(unsigned.getHeader("X-Private"));
      assertNull(unsigned.getHeader("X-Intercepted"));
    }
  }
}
