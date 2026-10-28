package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Timeout(15)
class ForwardPaginationParityTest {
  private MockWebServer server;
  private ForwardClient client;

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.start();
    client =
        ForwardClient.builder()
            .pat("pat_test")
            .baseUrl(server.url("/api/v1/forward").toString())
            .defaultHeader("X-Qoder-Beta", "other-beta")
            .maxRetries(0)
            .build();
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    server.close();
  }

  private void respond(String body) {
    server.enqueue(new MockResponse().setBody(body));
  }

  private RecordedRequest request() throws Exception {
    RecordedRequest r = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  @ParameterizedTest
  @ValueSource(strings = {"environments", "files", "skills", "vaults"})
  void postSearchPagesRetainBodyScopeAndOptions(String resource) throws Exception {
    respond("{\"data\":[{\"id\":\"first\"}],\"has_more\":true,\"next_page\":\"opaque+/=?\"}");
    respond("{\"data\":[],\"has_more\":false,\"next_page\":null}");
    RequestOptions options =
        RequestOptions.builder()
            .query("identity_id", "idn_override")
            .header("X-Qoder-Beta", "caller-beta")
            .header("X-Trace", "trace")
            .build();
    CursorPage<?, ?> page;
    switch (resource) {
      case "environments":
        page =
            client
                .environments()
                .search(
                    EnvironmentSearchParams.builder()
                        .metadata(Collections.singletonMap("team", "sdk"))
                        .identityId("idn_original")
                        .build(),
                    options);
        break;
      case "files":
        page =
            client
                .files()
                .search(
                    FileSearchParams.builder()
                        .metadata(Collections.singletonMap("team", "sdk"))
                        .scopeId("sess_test")
                        .identityId("idn_original")
                        .build(),
                    options);
        break;
      case "skills":
        page =
            client
                .skills()
                .search(
                    SkillSearchParams.builder()
                        .metadata(Collections.singletonMap("team", "sdk"))
                        .identityId("idn_original")
                        .build(),
                    options);
        break;
      default:
        page =
            client
                .vaults()
                .search(
                    VaultSearchParams.builder()
                        .metadata(Collections.singletonMap("team", "sdk"))
                        .identityId("idn_original")
                        .build(),
                    options);
    }
    assertTrue(page.hasNextPage());
    RecordedRequest first = request();
    CursorPage<?, ?> last = page.getNextPageAsync().get(3, TimeUnit.SECONDS);
    RecordedRequest second = request();
    assertEquals("POST", second.getMethod());
    assertEquals(
        "/api/v1/forward/" + resource + "/search?identity_id=idn_override", second.getPath());
    assertEquals("caller-beta,search-2026-08-31", second.getHeader("X-Qoder-Beta"));
    assertEquals("trace", second.getHeader("X-Trace"));
    Map<String, JsonValue> expected =
        new LinkedHashMap<>(JsonValue.parse(first.getBody().readUtf8()).asObject());
    expected.put("page", JsonValue.of("opaque+/=?"));
    assertEquals(
        JsonValue.of(expected).toJson(), JsonValue.parse(second.getBody().readUtf8()).toJson());
    assertFalse(last.hasNextPage());
    assertThrows(PaginationException.class, last::getNextPage);
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void repeatedSearchCursorCannotLoopAndDefaultBetaIsKept() throws Exception {
    respond("{\"data\":[{\"id\":\"f\"}],\"has_more\":true,\"next_page\":\"same\"}");
    FileListResponse page = client.files().search(FileSearchParams.builder().page("same").build());
    assertEquals("other-beta,search-2026-08-31", request().getHeader("x-qoder-beta"));
    assertThrows(PaginationException.class, page::getNextPage);
    assertEquals(1, server.getRequestCount());
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .files()
                .search(
                    FileSearchParams.fromJson(
                        JsonValue.parse("{\"identity_id\":\"wrong-place\"}"))));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void mountPagingUsesStoreIdsAndKeepsDirection(boolean backwards) throws Exception {
    respond(
        "{\"data\":[{\"memory_store_id\":\"m1\",\"access\":\"read_only\"}],\"first_id\":\"m1\",\"last_id\":\"m2\",\"has_more\":true}");
    respond("{\"data\":[],\"first_id\":null,\"last_id\":null,\"has_more\":false}");
    IdentityMemoryStoreListParams.Builder params = IdentityMemoryStoreListParams.builder().limit(2);
    if (backwards) params.beforeId("m0");
    IdentityMemoryStoreListResponse page =
        client
            .identities()
            .memoryStores()
            .list(
                "i/a",
                "t/b",
                params.build(),
                RequestOptions.builder().header("X-Trace", "mount").build());
    assertEquals(Boolean.TRUE, page.hasMore());
    request();
    IdentityMemoryStoreListResponse last = page.getNextPageAsync().get(3, TimeUnit.SECONDS);
    RecordedRequest next = request();
    assertEquals(
        "/api/v1/forward/identities/i%2Fa/templates/t%2Fb/memory_stores?limit=2&"
            + (backwards ? "before_id=m1" : "after_id=m2"),
        next.getPath());
    assertEquals("mount", next.getHeader("X-Trace"));
    assertFalse(last.hasNextPage());
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .identities()
                .memoryStores()
                .list(
                    "i",
                    "t",
                    params.build(),
                    RequestOptions.builder()
                        .query("after_id", "x")
                        .query("before_id", "y")
                        .build()));
  }

  @ParameterizedTest
  @ValueSource(strings = {"dreams", "decisions", "channel_pairings"})
  void newListsUseCursorPaging(String resource) throws Exception {
    respond(
        "{\"data\":[{\"id\":\"first\"}],\"first_id\":\"first\",\"last_id\":\"last\",\"has_more\":true}");
    respond("{\"data\":[],\"has_more\":false}");
    CursorPage<?, ?> page;
    if (resource.equals("dreams"))
      page = client.dreams().list(DreamListParams.builder().includeArchived(true).limit(1).build());
    else if (resource.equals("decisions"))
      page =
          client
              .decisions()
              .list(DecisionListParams.builder().sessionId("sess_test").limit(1).build());
    else
      page =
          client
              .channelPairings()
              .list(ChannelPairingListParams.builder().status("active").limit(1).build());
    String first = request().getPath();
    page.getNextPageAsync().get(3, TimeUnit.SECONDS);
    assertEquals(first + "&after_id=last", request().getPath());
  }

  @Test
  void decisionValuesAndDreamModelsPreserveTheirWireTypes() {
    Decision decision =
        new Decision(
            JsonValue.parse(
                "{\"answers\":[{\"type\":\"choice\",\"value\":false},{\"type\":\"choice\",\"value\":\"false\"},{\"type\":\"score\",\"score\":9007199254740993.25}],\"context\":{\"mode\":\"session\",\"included_event_count\":0},\"error\":null}"),
            null);
    assertFalse(decision.answers().get(0).value().asBoolean());
    assertEquals("false", decision.answers().get(1).value().asString());
    assertEquals(new BigDecimal("9007199254740993.25"), decision.answers().get(2).score());
    assertEquals(Long.valueOf(0), decision.context().includedEventCount());
    assertNull(decision.error());
    assertThrows(
        ConfigurationException.class,
        () -> DecisionChoice.builder().value(JsonValue.of(1)).build());
    DecisionQuestion question =
        DecisionQuestion.builder()
            .type("score")
            .instructions("grade")
            .levels(
                Arrays.asList(
                    DecisionLevel.builder().label("low").score(new BigDecimal("0.5")).build(),
                    DecisionLevel.builder().label("high").score(new BigDecimal("1.25")).build()))
            .build();
    assertEquals(new BigDecimal("1.25"), question.levels().get(1).score());
    assertThrows(
        ConfigurationException.class,
        () ->
            DecisionCreateParams.builder()
                .state(Collections.singletonMap("x", JsonValue.of(1)))
                .sessionId("s")
                .questions(Collections.singletonList(question))
                .build());
    assertThrows(
        ConfigurationException.class,
        () ->
            DecisionCreateParams.builder()
                .state(Collections.singletonMap("session_context", JsonValue.of("reserved")))
                .questions(Collections.singletonList(question))
                .build());
    DreamCreateParams dream =
        DreamCreateParams.builder()
            .inputs(
                Collections.singletonList(
                    DreamInput.builder()
                        .type("identity_template")
                        .identityId("i")
                        .templateId("t")
                        .build()))
            .model(
                DreamModelConfig.builder().id("auto").effort("low").contextWindow(32768L).build())
            .build();
    assertEquals(
        "{\"id\":\"auto\",\"effort\":\"low\",\"context_window\":32768}",
        dream.model().toJsonValue().toJson());
    assertEquals("", DreamModelConfig.of("").id());
    assertThrows(ConfigurationException.class, () -> ChannelPairingUpdateParams.builder().build());
    assertThrows(
        ConfigurationException.class,
        () -> ChannelPairingUpdateParams.builder().identityId(null).build());
    assertThrows(
        ConfigurationException.class,
        () -> ChannelPairingUpdateParams.builder().templateId("bad\nvalue").build());
  }

  @Test
  void searchContinuationCanBeCancelled() throws Exception {
    respond("{\"data\":[{\"id\":\"v\"}],\"has_more\":true,\"next_page\":\"next\"}");
    VaultListResponse first = client.vaults().search(VaultSearchParams.builder().build());
    request();
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    CompletableFuture<VaultListResponse> pending = first.getNextPageAsync();
    request();
    assertTrue(pending.cancel(true));
    assertTrue(pending.isCancelled());
  }

  @Test
  void emptySearchSendsObjectAndPreservesRequiredBetaOnce() throws Exception {
    respond("{\"data\":[],\"has_more\":false}");
    client
        .files()
        .search(RequestOptions.builder().header("x-qoder-beta", "search-2026-08-31,other").build());
    RecordedRequest sent = request();
    assertEquals("{}", sent.getBody().readUtf8());
    assertEquals("search-2026-08-31,other", sent.getHeader("x-qoder-beta"));
  }

  @Test
  void decisionRetriesKeepTheExactBodyAndDoNotRetryConflict() throws Exception {
    DecisionCreateParams params =
        DecisionCreateParams.builder()
            .sessionId("sess_test")
            .questions(
                Collections.singletonList(
                    DecisionQuestion.builder()
                        .type("choice")
                        .instructions("Is it complete?")
                        .choices(
                            Arrays.asList(
                                DecisionChoice.builder().value("yes").build(),
                                DecisionChoice.builder().value("no").build()))
                        .build()))
            .build();
    try (ForwardClient retrying = client.withOptions(b -> b.maxRetries(1))) {
      server.enqueue(
          new MockResponse()
              .setResponseCode(503)
              .setHeader("retry-after-ms", "1")
              .setBody("{\"error\":{\"message\":\"busy\"}}"));
      respond("{\"id\":\"dec_ok\",\"status\":\"completed\"}");
      assertEquals(
          "dec_ok",
          retrying
              .decisions()
              .create(params, RequestOptions.builder().idempotencyKey("decision-key").build())
              .id());
      RecordedRequest first = request(), second = request();
      assertEquals(first.getBody().readUtf8(), second.getBody().readUtf8());
      assertEquals("decision-key", second.getHeader("Idempotency-Key"));
      server.enqueue(
          new MockResponse()
              .setResponseCode(409)
              .setBody(
                  "{\"error\":{\"code\":\"decision_in_progress\",\"message\":\"pending\",\"decision_id\":\"dec_pending\"}}"));
      assertThrows(
          ApiException.class,
          () ->
              retrying
                  .decisions()
                  .create(params, RequestOptions.builder().idempotencyKey("pending-key").build()));
      request();
      assertEquals(3, server.getRequestCount());
      server.enqueue(
          new MockResponse().setResponseCode(503).setBody("{\"error\":{\"message\":\"busy\"}}"));
      assertThrows(ApiException.class, () -> retrying.decisions().create(params));
      request();
      assertEquals(4, server.getRequestCount());
    }
  }
}
