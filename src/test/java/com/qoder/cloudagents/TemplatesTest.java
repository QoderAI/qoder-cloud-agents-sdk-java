package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Timeout(15)
class TemplatesTest {
  private MockWebServer server;
  private ForwardClient client;
  private static final String TEMPLATE =
      "{\"id\":\"tmpl_1\",\"type\":\"template\",\"name\":\"Sample\",\"status\":\"active\"}";
  private static final ObjectMapper JSON = new ObjectMapper();

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.start();
    client =
        ForwardClient.builder()
            .baseUrl(server.url("/api/v1/forward").toString())
            .pat("test-token")
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

  private String fixture() throws IOException {
    try (BufferedReader in =
        new BufferedReader(
            new InputStreamReader(
                getClass().getResourceAsStream("/template-create.json"), StandardCharsets.UTF_8))) {
      return in.lines().collect(Collectors.joining("\n"));
    }
  }

  private TemplateCreateParams createParams() {
    return TemplateCreateParams.builder()
        .name("demo")
        .modelId("auto")
        .environmentId("env_test")
        .build();
  }

  private String page(String first, String last, boolean more, String... ids) {
    return "{\"data\":["
        + Arrays.stream(ids).map(id -> "{\"id\":\"" + id + "\"}").collect(Collectors.joining(","))
        + "],\"has_more\":"
        + more
        + ",\"first_id\":"
        + (first == null ? "null" : "\"" + first + "\"")
        + ",\"last_id\":"
        + (last == null ? "null" : "\"" + last + "\"")
        + "}";
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void allTemplateRoutesExposeResponseMetadataAndRequestOptions(boolean async) throws Exception {
    RequestOptions options =
        RequestOptions.builder()
            .idempotencyKey("operation-key")
            .header("X-Qoder-Beta", "browser-use-2026-07-14")
            .build();
    for (int i = 0; i < 5; i++)
      server.enqueue(json(TEMPLATE).setHeader("x-request-id", "req-" + i));
    ApiResponse<Template> created =
        async
            ? client.async().templates().withResponse().create(createParams(), options).get()
            : client.templates().withResponse().create(createParams(), options);
    assertEquals("req-0", created.data().requestId());
    assertEquals(200, created.statusCode());
    RecordedRequest create = request();
    assertEquals("POST", create.getMethod());
    assertEquals("/api/v1/forward/templates", create.getPath());
    assertEquals("operation-key", create.getHeader("Idempotency-Key"));
    assertEquals("browser-use-2026-07-14", create.getHeader("X-Qoder-Beta"));
    assertEquals("Bearer test-token", create.getHeader("Authorization"));
    assertEquals(
        JSON.readTree("{\"name\":\"demo\",\"model\":\"auto\",\"environment_id\":\"env_test\"}"),
        JSON.readTree(create.getBody().readUtf8()));

    String id = "a/b ?#%";
    ApiResponse<Template> retrieved =
        async
            ? client.async().templates().withResponse().retrieve(id).get()
            : client.templates().withResponse().retrieve(id);
    assertEquals("req-1", retrieved.requestId());
    RecordedRequest get = request();
    assertEquals("GET", get.getMethod());
    assertEquals("/api/v1/forward/templates/a%2Fb%20%3F%23%25", get.getPath());

    TemplateUpdateParams update =
        TemplateUpdateParams.builder().description(null).tools(Collections.emptyList()).build();
    ApiResponse<Template> updated =
        async
            ? client.async().templates().withResponse().update(id, update).get()
            : client.templates().withResponse().update(id, update);
    assertEquals("req-2", updated.requestId());
    RecordedRequest patch = request();
    assertEquals("POST", patch.getMethod());
    assertEquals(get.getPath(), patch.getPath());
    assertEquals(
        JSON.readTree("{\"description\":null,\"tools\":[]}"),
        JSON.readTree(patch.getBody().readUtf8()));

    TemplateCloneParams clone = TemplateCloneParams.builder().name("copy").build();
    ApiResponse<Template> cloned =
        async
            ? client.async().templates().withResponse().clone(id, clone).get()
            : client.templates().withResponse().clone(id, clone);
    assertEquals("req-3", cloned.requestId());
    RecordedRequest copy = request();
    assertEquals("POST", copy.getMethod());
    assertEquals(get.getPath() + "/clone", copy.getPath());
    assertEquals(JSON.readTree("{\"name\":\"copy\"}"), JSON.readTree(copy.getBody().readUtf8()));

    ApiResponse<Template> archived =
        async
            ? client.async().templates().withResponse().archive(id).get()
            : client.templates().withResponse().archive(id);
    assertEquals("req-4", archived.requestId());
    RecordedRequest archive = request();
    assertEquals("POST", archive.getMethod());
    assertEquals(get.getPath() + "/archive", archive.getPath());
    assertEquals(0, archive.getBodySize());
  }

  @Test
  void typedConfigurationMatchesIndependentWireFixture() throws Exception {
    TemplateCreateParams params =
        TemplateCreateParams.builder()
            .name("SDK template")
            .model(
                ModelConfig.builder()
                    .id("ultimate")
                    .effort("high")
                    .speed("standard")
                    .contextWindow(400000L)
                    .build())
            .description("Contract test")
            .system("Help with the supplied task.")
            .maxToolRounds(12L)
            .environmentId("env_test")
            .tools(
                Arrays.asList(
                    Tool.builder()
                        .type("custom")
                        .name("lookup")
                        .description("Lookup")
                        .inputSchema(
                            JsonValue.parse(
                                "{\"type\":\"object\",\"properties\":{\"q\":{\"type\":\"string\"}}}"))
                        .build(),
                    Tool.builder()
                        .type("agent_toolset_20260401")
                        .enabledTools(Collections.singletonList("Read"))
                        .disallowedTools(Collections.singletonList("Write"))
                        .configs(
                            Collections.singletonList(
                                ToolConfig.builder()
                                    .name("Read")
                                    .enabled(true)
                                    .permissionPolicy(
                                        PermissionPolicy.builder().type("always_allow").build())
                                    .build()))
                        .build(),
                    Tool.builder().type("mcp_toolset").mcpServerName("docs").build()))
            .managedToolConfig(
                ManagedToolConfig.builder()
                    .enabledTools(Arrays.asList("list_drive_entries", "future_tool"))
                    .build())
            .mcpServers(
                Collections.singletonList(
                    MCPServer.builder()
                        .name("docs")
                        .url("https://example.com/mcp")
                        .type("http")
                        .build()))
            .skills(
                Collections.singletonList(
                    SkillBinding.builder()
                        .type("skill")
                        .skillId("skill_test")
                        .version("latest")
                        .enabled(true)
                        .build()))
            .multiagent(
                MultiagentConfig.builder()
                    .type("coordinator")
                    .agents(
                        Arrays.asList(
                            MultiagentEntry.builder().type("self").build(),
                            MultiagentEntry.builder()
                                .type("agent")
                                .templateId("tmpl_helper")
                                .name("helper")
                                .build(),
                            MultiagentEntry.builder().type("advisor").model("ultimate").build()))
                    .build())
            .vaults(
                Collections.singletonMap(
                    "vault_test", ResourceBinding.builder().enabled(true).build()))
            .files(
                Collections.singletonMap(
                    "file_test", ResourceBinding.builder().enabled(false).build()))
            .githubRepositories(
                Collections.singletonMap(
                    "repo",
                    GitHubRepositoryConfig.builder()
                        .url("https://github.com/example/demo")
                        .mountPath("/repo")
                        .enabled(true)
                        .authorizationToken("test_write_only_secret")
                        .build()))
            .environmentVariables(EnvironmentVariables.of(Collections.singletonMap("MODE", "test")))
            .metadata(
                new LinkedHashMap<String, JsonValue>() {
                  {
                    put("nested", JsonValue.parse("{\"precision\":9007199254740993}"));
                    put("nullable", JsonValue.of(null));
                  }
                })
            .additionalField("future_option", JsonValue.parse("{\"enabled\":true}"))
            .build();
    server.enqueue(json(TEMPLATE));
    client.templates().create(params);
    assertEquals(JSON.readTree(fixture()), JSON.readTree(request().getBody().readUtf8()));
    assertFalse(params.toString().contains("test_write_only_secret"));
  }

  @Test
  void fullResponseAndSummaryHaveTypedFieldsWithoutLosingUnknowns() throws Exception {
    ObjectNode response = (ObjectNode) JSON.readTree(fixture());
    ((ObjectNode) response.get("github_repositories").get("repo")).remove("authorization_token");
    response.put("id", "tmpl_full");
    response.put("type", "template");
    response.put("status", "future-state");
    server.enqueue(json(response.toString()).setHeader("x-request-id", "req-full"));
    Template template = client.templates().retrieve("tmpl_full");
    assertEquals("req-full", template.requestId());
    assertEquals("ultimate", template.model().id());
    assertFalse(template.model().isString());
    assertEquals("standard", template.model().speed());
    assertEquals(400000L, template.model().contextWindow());
    assertEquals(12L, template.maxToolRounds());
    assertEquals("Help with the supplied task.", template.system());
    assertEquals(
        "always_allow", template.tools().get(1).configs().get(0).permissionPolicy().type());
    assertEquals("object", template.tools().get(0).inputSchema().get("type").asString());
    assertEquals("docs", template.tools().get(2).mcpServerName());
    assertEquals("https://example.com/mcp", template.mcpServers().get(0).url());
    assertEquals("latest", template.skills().get(0).version());
    assertEquals("ultimate", template.multiagent().agents().get(2).model());
    assertTrue(template.vaults().get("vault_test").enabled());
    assertFalse(template.files().get("file_test").enabled());
    assertEquals("/repo", template.githubRepositories().get("repo").mountPath());
    assertTrue(template.githubRepositories().get("repo").field("authorization_token").isMissing());
    assertEquals("test", template.environmentVariables().asMap().get("MODE"));
    assertEquals(9007199254740993L, template.metadata().get("nested").get("precision").asLong());
    assertTrue(template.field("future_option").get("enabled").asBoolean());
    assertEquals("future_tool", template.managedToolConfig().enabledTools().get(1));
    assertThrows(UnsupportedOperationException.class, () -> template.tools().clear());
    Template summary = new Template(JsonValue.parse(TEMPLATE), null);
    assertNull(summary.model());
    assertNull(summary.tools());
    assertNull(summary.environmentVariables());
    Template nullable =
        new Template(JsonValue.parse("{\"multiagent\":null,\"max_tool_rounds\":null}"), null);
    assertNull(nullable.multiagent());
    assertTrue(nullable.field("multiagent").isNull());
  }

  @Test
  void omissionNullEmptyAndUnknownFieldsRemainDistinctAndImmutable() {
    List<Tool> tools = new ArrayList<>();
    Map<String, JsonValue> metadata = new LinkedHashMap<>();
    metadata.put("value", JsonValue.of("original"));
    TemplateUpdateParams.Builder builder =
        TemplateUpdateParams.builder()
            .description(null)
            .tools(tools)
            .files(Collections.emptyMap())
            .metadata(metadata)
            .system("discard")
            .omit("system");
    tools.add(Tool.builder().type("custom").build());
    metadata.put("value", JsonValue.of("changed"));
    TemplateUpdateParams first = builder.build();
    builder.description("later");
    assertTrue(first.field("description").isNull());
    assertTrue(first.field("system").isMissing());
    assertEquals(0, first.field("tools").asArray().size());
    assertEquals(0, first.field("files").asObject().size());
    assertEquals("original", first.field("metadata").get("value").asString());
    assertTrue(
        TemplateUpdateParams.builder()
            .model(null)
            .environmentVariables(null)
            .build()
            .field("model")
            .isNull());
    assertEquals("auto", ModelConfig.of("auto").toJsonValue().asString());
    assertEquals("KEY=value", EnvironmentVariables.fromText("KEY=value").asString());
    assertEquals(
        "\"KEY=value\"",
        TemplateUpdateParams.builder()
            .environmentVariables(EnvironmentVariables.fromText("KEY=value"))
            .build()
            .field("environment_variables")
            .toJson());
    assertEquals(
        "future",
        ModelConfig.fromJson(JsonValue.parse("{\"id\":\"future\",\"new_setting\":1}")).id());
    assertEquals(
        1,
        ModelConfig.fromJson(JsonValue.parse("{\"id\":\"future\",\"new_setting\":1}"))
            .field("new_setting")
            .asLong());
    GitHubRepositoryConfig.Builder secret =
        GitHubRepositoryConfig.builder().authorizationToken("write-secret");
    assertFalse(secret.toString().contains("write-secret"));
    assertFalse(secret.build().toString().contains("write-secret"));
    assertThrows(
        ConfigurationException.class,
        () -> TemplateCreateParams.builder().name("name").modelId("auto").build());
    assertThrows(
        ConfigurationException.class,
        () -> TemplateCreateParams.builder().name("name").environmentId("env").model(null).build());
    assertThrows(ConfigurationException.class, () -> ModelConfig.builder().build());
    assertThrows(
        ConfigurationException.class,
        () ->
            TemplateUpdateParams.builder()
                .additionalField("missing", JsonValue.parse("{}").get("x"))
                .build());
  }

  @Test
  void forwardIterationPreservesOrderFiltersOverridesAndFreshCredentials() throws Exception {
    AtomicInteger tokens = new AtomicInteger();
    try (ForwardClient rotating =
        ForwardClient.builder()
            .baseUrl(server.url("/api/v1/forward").toString())
            .credential(
                () -> CompletableFuture.completedFuture("token-" + tokens.incrementAndGet()))
            .build()) {
      server.enqueue(json(page("z", "a", true, "z", "a")));
      server.enqueue(json(page("b", "b", false, "b")).setHeader("request-id", "req-next"));
      RequestOptions options =
          RequestOptions.builder()
              .query("after_id", "initial")
              .query("custom", "value +/")
              .header("X-Correlation", "page-test")
              .maxRetries(0)
              .build();
      TemplateListResponse first =
          rotating
              .templates()
              .list(
                  TemplateListParams.builder()
                      .status("active")
                      .limit(2)
                      .afterId("overridden")
                      .build(),
                  options);
      Iterator<Template> items = first.iterator();
      assertEquals(1, server.getRequestCount());
      assertEquals("z", items.next().id());
      assertEquals("a", items.next().id());
      assertEquals(1, server.getRequestCount());
      assertEquals("b", items.next().id());
      assertFalse(items.hasNext());
      assertThrows(NoSuchElementException.class, items::next);
      for (String cursor : Arrays.asList("initial", "a")) {
        RecordedRequest request = request();
        assertEquals(cursor, request.getRequestUrl().queryParameter("after_id"));
        assertEquals("active", request.getRequestUrl().queryParameter("status"));
        assertEquals("2", request.getRequestUrl().queryParameter("limit"));
        assertEquals("value +/", request.getRequestUrl().queryParameter("custom"));
        assertEquals("page-test", request.getHeader("X-Correlation"));
        assertEquals(
            "Bearer token-" + (cursor.equals("initial") ? 1 : 2),
            request.getHeader("Authorization"));
      }
      assertEquals("initial", options.query().get("after_id"));
    }
  }

  @Test
  void backwardAsyncPaginationUsesFirstIdAndPreservesPerPageMetadata() throws Exception {
    server.enqueue(json(page("head", "tail", true, "head", "tail")));
    server.enqueue(
        json(page("older", "older", false, "older")).setHeader("x-request-id", "req-older"));
    TemplateListResponse first =
        client
            .async()
            .templates()
            .list(TemplateListParams.builder().beforeId("start").build())
            .get();
    TemplateListResponse next = first.getNextPageAsync().get();
    assertEquals("req-older", next.requestId());
    assertEquals("req-older", next.data().get(0).requestId());
    assertFalse(next.hasNextPage());
    assertEquals("start", request().getRequestUrl().queryParameter("before_id"));
    RecordedRequest second = request();
    assertEquals("head", second.getRequestUrl().queryParameter("before_id"));
    assertNull(second.getRequestUrl().queryParameter("after_id"));
    assertThrows(PaginationException.class, next::getNextPage);
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void pageIterationIsLazyAndStopsAtEnd() {
    server.enqueue(json(page("a", "a", true, "a")));
    server.enqueue(json(page("b", "b", false, "b")));
    TemplateListResponse first = client.templates().list();
    Iterator<TemplateListResponse> pages = first.iterPages().iterator();
    assertEquals(1, server.getRequestCount());
    assertSame(first, pages.next());
    assertTrue(pages.hasNext());
    assertEquals(1, server.getRequestCount());
    assertEquals("b", pages.next().data().get(0).id());
    assertFalse(pages.hasNext());
    assertThrows(NoSuchElementException.class, pages::next);
  }

  @Test
  void emptyAndMissingCursorPagesStopWithoutRequests() {
    for (String value :
        Arrays.asList(
            page(null, null, true), page("a", null, true, "a"), page("a", "a", false, "a"))) {
      server.enqueue(json(value));
      TemplateListResponse page = client.templates().list();
      assertFalse(page.hasNextPage());
      int requests = server.getRequestCount();
      assertThrows(PaginationException.class, page::getNextPage);
      assertEquals(requests, server.getRequestCount());
    }
    TemplateListResponse detached =
        new TemplateListResponse(JsonValue.parse(page("a", "a", true, "a")), null);
    assertThrows(PaginationException.class, detached::getNextPage);
  }

  @Test
  void repeatedCursorsCannotLoopIncludingLongerCycles() {
    server.enqueue(json(page("a", "a", true, "a")));
    server.enqueue(json(page("b", "b", true, "b")));
    server.enqueue(json(page("a", "a", true, "a")));
    TemplateListResponse repeated = client.templates().list().getNextPage().getNextPage();
    assertThrows(PaginationException.class, repeated::getNextPage);
    assertEquals(3, server.getRequestCount());
    server.enqueue(json(page("seed", "seed", true, "seed")));
    TemplateListResponse immediate =
        client.templates().list(TemplateListParams.builder().afterId("seed").build());
    assertThrows(PaginationException.class, immediate::getNextPage);
    assertEquals(4, server.getRequestCount());
  }

  @Test
  void conflictingCursorOverrideFailsBeforeHttp() {
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .templates()
                .list(
                    TemplateListParams.builder().beforeId("b").build(),
                    RequestOptions.builder().query("after_id", "a").build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void laterPageRetainsRetryPolicyAndReportsErrors() {
    client.close();
    client =
        ForwardClient.builder()
            .baseUrl(server.url("/api/v1/forward").toString())
            .pat("test-token")
            .maxRetries(2)
            .build();
    server.enqueue(json(page("a", "a", true, "a")));
    server.enqueue(
        json("{\"error\":{\"message\":\"unavailable\"}}")
            .setResponseCode(503)
            .setHeader("x-request-id", "req-error"));
    TemplateListResponse page =
        client
            .templates()
            .list(
                TemplateListParams.builder().build(),
                RequestOptions.builder().maxRetries(0).build());
    ApiStatusException error = assertThrows(ApiStatusException.class, page::getNextPage);
    assertEquals(503, error.statusCode());
    assertEquals("req-error", error.requestId());
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void laterPageRetainsTimeoutAndAsyncCancellation() throws Exception {
    server.enqueue(json(page("a", "a", true, "a")));
    server.enqueue(json(page("b", "b", false, "b")).setHeadersDelay(1, TimeUnit.SECONDS));
    TemplateListResponse first =
        client
            .templates()
            .list(
                TemplateListParams.builder().build(),
                RequestOptions.builder()
                    .timeout(Duration.ofMillis(150))
                    .totalTimeout(Duration.ofMillis(500))
                    .maxRetries(0)
                    .build());
    assertThrows(ApiTimeoutException.class, first::getNextPage);
    request();
    request();
    server.enqueue(json(page("c", "c", true, "c")));
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    TemplateListResponse another = client.templates().list();
    CompletableFuture<TemplateListResponse> pending = another.getNextPageAsync();
    request();
    request();
    assertTrue(pending.cancel(true));
    assertThrows(CancellationException.class, pending::join);
    assertTrue(pending.isCancelled());
  }

  @Test
  void cancellingNextPageClosesItsHttpSocket() throws Exception {
    try (java.net.ServerSocket listener =
            new java.net.ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress());
        ForwardClient direct =
            ForwardClient.builder()
                .baseUrl("http://localhost:" + listener.getLocalPort())
                .pat("test-token")
                .maxRetries(0)
                .build()) {
      listener.setSoTimeout(3000);
      CompletableFuture<TemplateListResponse> first = direct.async().templates().list();
      try (java.net.Socket peer = listener.accept()) {
        readHeaders(peer);
        byte[] body = page("a", "a", true, "a").getBytes(StandardCharsets.UTF_8);
        peer.getOutputStream()
            .write(
                ("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: "
                        + body.length
                        + "\r\nConnection: close\r\n\r\n")
                    .getBytes(StandardCharsets.US_ASCII));
        peer.getOutputStream().write(body);
        peer.getOutputStream().flush();
      }
      CompletableFuture<TemplateListResponse> pending =
          first.get(3, TimeUnit.SECONDS).getNextPageAsync();
      try (java.net.Socket peer = listener.accept()) {
        readHeaders(peer);
        assertTrue(pending.cancel(true));
        assertEquals(
            -1, peer.getInputStream().read(), "Pagination cancellation must reach the HTTP call");
      }
    }
  }

  private void readHeaders(java.net.Socket peer) throws IOException {
    peer.setSoTimeout(3000);
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(peer.getInputStream(), StandardCharsets.US_ASCII));
    String line;
    do {
      line = reader.readLine();
      assertNotNull(line);
    } while (!line.isEmpty());
  }

  @Test
  void writesRespectIdempotencyReplayRules() throws Exception {
    server.enqueue(json("{}").setResponseCode(503).setHeader("Retry-After", "0"));
    RequestOptions retries = RequestOptions.builder().maxRetries(1).build();
    assertThrows(
        ApiStatusException.class, () -> client.templates().create(createParams(), retries));
    assertEquals(1, server.getRequestCount());
    request();
    server.enqueue(json("{}").setResponseCode(503).setHeader("Retry-After", "0.001"));
    server.enqueue(json(TEMPLATE));
    client
        .async()
        .templates()
        .create(createParams(), retries.toBuilder().idempotencyKey("safe-key").build())
        .get();
    RecordedRequest first = request(), second = request();
    assertEquals(first.getBody().readUtf8(), second.getBody().readUtf8());
    assertEquals("safe-key", second.getHeader("Idempotency-Key"));
    assertEquals("1", second.getHeader("X-Qoder-Retry-Count"));
  }
}
