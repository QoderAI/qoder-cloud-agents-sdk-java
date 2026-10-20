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
import java.util.stream.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class IdentitiesTest {
  private MockWebServer server;
  private ForwardClient client;
  private static final String ID = "idn/a ?#%", TEMPLATE = "tmpl/b ?#%";
  private static final ObjectMapper JSON = new ObjectMapper();

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

  private String fixture(String file) throws IOException {
    try (BufferedReader in =
        new BufferedReader(
            new InputStreamReader(
                getClass().getResourceAsStream("/" + file), StandardCharsets.UTF_8))) {
      return in.lines().collect(Collectors.joining("\n"));
    }
  }

  static Stream<Arguments> routes() {
    String[][] rows = {
      {"list", "GET", "/identities", "", "{\"data\":[{\"id\":\"idn_1\"}],\"has_more\":false}"},
      {
        "create",
        "POST",
        "/identities",
        "{\"external_id\":\"user_1\",\"enabled\":false}",
        "{\"id\":\"idn_1\"}"
      },
      {
        "ensureAdmin",
        "POST",
        "/identities/admin/ensure",
        "",
        "{\"id\":\"idn_admin\",\"identity_type\":\"admin\"}"
      },
      {"stats", "GET", "/identities/stats", "", "{\"total_agents\":2,\"total_sessions\":3}"},
      {"retrieve", "GET", "/identities/ID", "", "{\"id\":\"idn_1\"}"},
      {
        "update",
        "POST",
        "/identities/ID",
        "{\"name\":\"Updated\",\"metadata\":null}",
        "{\"id\":\"idn_1\"}"
      },
      {"delete", "DELETE", "/identities/ID", "", "{\"id\":\"idn_1\",\"deleted\":true}"},
      {
        "listTemplates",
        "GET",
        "/identities/ID/agents",
        "",
        "{\"data\":[{\"template_id\":\"tmpl_1\",\"session_count\":2}]}"
      },
      {
        "clear",
        "POST",
        "/identities/ID/clear",
        "{\"reason\":\"test cleanup\"}",
        "{\"identity_id\":\"idn_1\",\"status\":\"completed\"}"
      },
      {"disable", "POST", "/identities/ID/disable", "", "{\"id\":\"idn_1\",\"enabled\":false}"},
      {"enable", "POST", "/identities/ID/enable", "", "{\"id\":\"idn_1\",\"enabled\":true}"},
      {
        "configList",
        "GET",
        "/identities/ID/templates",
        "",
        "{\"data\":[{\"id\":\"config_1\"}],\"has_more\":false}"
      },
      {
        "configRetrieve",
        "GET",
        "/identities/ID/templates/TEMPLATE/config",
        "",
        "{\"id\":\"config_1\",\"identity_config\":{}}"
      },
      {
        "configUpsert",
        "POST",
        "/identities/ID/templates/TEMPLATE/config",
        "{\"identity_config\":{}}",
        "{\"id\":\"config_1\",\"identity_config\":{}}"
      },
      {
        "configEffective",
        "GET",
        "/identities/ID/templates/TEMPLATE/effective",
        "",
        "{\"type\":\"effective_spec\",\"effective_hash\":\"hash\"}"
      }
    };
    return Arrays.stream(rows)
        .flatMap(
            row ->
                Stream.of(false, true)
                    .map(async -> Arguments.of(row[0], row[1], row[2], row[3], row[4], async)));
  }

  @ParameterizedTest
  @MethodSource("routes")
  void syncAndAsyncContracts(
      String operation,
      String method,
      String suffix,
      String body,
      String responseBody,
      boolean async)
      throws Exception {
    server.enqueue(json(responseBody).setHeader("x-request-id", "req-" + operation));
    RequestOptions options =
        RequestOptions.builder()
            .idempotencyKey("request-key")
            .header("X-Correlation", "contract")
            .build();
    ApiResponse<? extends JsonModel> response = invoke(operation, async, options);
    assertEquals(200, response.statusCode());
    assertEquals("req-" + operation, response.requestId());
    assertEquals(response.requestId(), response.data().requestId());
    RecordedRequest sent = request();
    assertEquals(method, sent.getMethod());
    assertEquals(
        "/api/v1/forward"
            + suffix
                .replace("TEMPLATE", "tmpl%2Fb%20%3F%23%25")
                .replace("ID", "idn%2Fa%20%3F%23%25"),
        sent.getPath());
    assertEquals("Bearer test-pat", sent.getHeader("Authorization"));
    assertEquals("request-key", sent.getHeader("Idempotency-Key"));
    assertEquals("contract", sent.getHeader("X-Correlation"));
    String actual = sent.getBody().readUtf8();
    if (body.isEmpty()) assertEquals("", actual);
    else assertEquals(JSON.readTree(body), JSON.readTree(actual));
    assertEquals(1, server.getRequestCount());
  }

  private ApiResponse<? extends JsonModel> invoke(
      String operation, boolean async, RequestOptions options) throws Exception {
    switch (operation) {
      case "list":
        return async
            ? client
                .async()
                .identities()
                .withResponse()
                .list(IdentityListParams.builder().build(), options)
                .get()
            : client
                .identities()
                .withResponse()
                .list(IdentityListParams.builder().build(), options);
      case "create":
        return async
            ? client
                .async()
                .identities()
                .withResponse()
                .create(
                    IdentityCreateParams.builder().externalId("user_1").enabled(false).build(),
                    options)
                .get()
            : client
                .identities()
                .withResponse()
                .create(
                    IdentityCreateParams.builder().externalId("user_1").enabled(false).build(),
                    options);
      case "ensureAdmin":
        return async
            ? client.async().identities().withResponse().ensureAdmin(options).get()
            : client.identities().withResponse().ensureAdmin(options);
      case "stats":
        return async
            ? client.async().identities().withResponse().stats(options).get()
            : client.identities().withResponse().stats(options);
      case "retrieve":
        return async
            ? client.async().identities().withResponse().retrieve(ID, options).get()
            : client.identities().withResponse().retrieve(ID, options);
      case "update":
        return async
            ? client
                .async()
                .identities()
                .withResponse()
                .update(
                    ID,
                    IdentityUpdateParams.builder().name("Updated").metadata(null).build(),
                    options)
                .get()
            : client
                .identities()
                .withResponse()
                .update(
                    ID,
                    IdentityUpdateParams.builder().name("Updated").metadata(null).build(),
                    options);
      case "delete":
        return async
            ? client.async().identities().withResponse().delete(ID, options).get()
            : client.identities().withResponse().delete(ID, options);
      case "listTemplates":
        return async
            ? client.async().identities().withResponse().listTemplates(ID, options).get()
            : client.identities().withResponse().listTemplates(ID, options);
      case "clear":
        return async
            ? client
                .async()
                .identities()
                .withResponse()
                .clear(ID, IdentityClearParams.builder().reason("test cleanup").build(), options)
                .get()
            : client
                .identities()
                .withResponse()
                .clear(ID, IdentityClearParams.builder().reason("test cleanup").build(), options);
      case "disable":
        return async
            ? client.async().identities().withResponse().disable(ID, options).get()
            : client.identities().withResponse().disable(ID, options);
      case "enable":
        return async
            ? client.async().identities().withResponse().enable(ID, options).get()
            : client.identities().withResponse().enable(ID, options);
      case "configList":
        return async
            ? client
                .async()
                .identities()
                .configs()
                .withResponse()
                .list(ID, IdentityConfigListParams.builder().build(), options)
                .get()
            : client
                .identities()
                .configs()
                .withResponse()
                .list(ID, IdentityConfigListParams.builder().build(), options);
      case "configRetrieve":
        return async
            ? client
                .async()
                .identities()
                .configs()
                .withResponse()
                .retrieve(ID, TEMPLATE, options)
                .get()
            : client.identities().configs().withResponse().retrieve(ID, TEMPLATE, options);
      case "configUpsert":
        return async
            ? client
                .async()
                .identities()
                .configs()
                .withResponse()
                .upsert(
                    ID,
                    TEMPLATE,
                    IdentityConfigUpsertParams.builder()
                        .identityConfig(IdentityConfigSpec.builder().build())
                        .build(),
                    options)
                .get()
            : client
                .identities()
                .configs()
                .withResponse()
                .upsert(
                    ID,
                    TEMPLATE,
                    IdentityConfigUpsertParams.builder()
                        .identityConfig(IdentityConfigSpec.builder().build())
                        .build(),
                    options);
      case "configEffective":
        return async
            ? client
                .async()
                .identities()
                .configs()
                .withResponse()
                .getEffective(ID, TEMPLATE, options)
                .get()
            : client.identities().configs().withResponse().getEffective(ID, TEMPLATE, options);
      default:
        throw new AssertionError(operation);
    }
  }

  private IdentityConfigSpec fullSpec() {
    Map<String, ToolOverride> tools = new LinkedHashMap<>();
    tools.put(
        "Bash",
        ToolOverride.builder()
            .enabled(true)
            .permissionPolicy(PermissionPolicy.builder().type("always_ask").build())
            .build());
    tools.put("Read", null);
    Map<String, ManagedToolOverride> managed = new LinkedHashMap<>();
    managed.put("create_forward_schedule", ManagedToolOverride.builder().enabled(false).build());
    managed.put("list_forward_schedules", null);
    Map<String, MCPServerOverride> servers = new LinkedHashMap<>();
    servers.put(
        "crm",
        MCPServerOverride.builder()
            .enabled(true)
            .type("http")
            .url("https://example.com/mcp")
            .build());
    servers.put("removed", null);
    Map<String, JsonValue> agentMetadata = new LinkedHashMap<>();
    agentMetadata.put("team", JsonValue.of("support"));
    agentMetadata.put("future", JsonValue.of(null));
    Map<String, EnvironmentVariableOverride> env = new LinkedHashMap<>();
    env.put("MODE", EnvironmentVariableOverride.builder().op("set").value("user").build());
    env.put("REMOVE", EnvironmentVariableOverride.builder().op("unset").build());
    env.put("INHERIT", null);
    return IdentityConfigSpec.builder()
        .system(SystemOverride.builder().mode("append").content("Prefer CRM data.").build())
        .model(ModelOverride.builder().effort("high").speed("standard").build())
        .tools(IdentityTools.overrides(tools))
        .managedToolConfig(managed)
        .mcpServers(servers)
        .skills(
            Collections.singletonMap(
                "skill_1",
                SkillOverride.builder().enabled(true).type("custom").version("latest").build()))
        .toolsets(
            Collections.singletonMap(
                "crm",
                ToolsetOverride.builder()
                    .type("mcp_toolset")
                    .enabled(true)
                    .mcpServerName("crm")
                    .tools(
                        Collections.singletonMap(
                            "lookup", ToolOverride.builder().enabled(false).build()))
                    .configs(
                        Collections.singletonList(
                            ToolConfig.builder().name("lookup").enabled(false).build()))
                    .build()))
        .agentMetadata(agentMetadata)
        .vaults(
            Collections.singletonMap("vault_1", ResourceBinding.builder().enabled(true).build()))
        .files(Collections.singletonMap("file_1", null))
        .githubRepositories(
            Collections.singletonMap(
                "repo",
                GitHubRepositoryConfig.builder()
                    .authorizationToken("test_write_only")
                    .enabled(true)
                    .mountPath(null)
                    .build()))
        .environmentVariables(env)
        .additionalField("future_option", JsonValue.parse("{\"value\":9007199254740993}"))
        .build();
  }

  @Test
  void fullOverlayMatchesWireFixtureWithoutCompilingItLocally() throws Exception {
    IdentityConfigUpsertParams params =
        IdentityConfigUpsertParams.builder()
            .name("User overrides")
            .identityConfig(fullSpec())
            .metadata(Collections.singletonMap("owner", JsonValue.of("test")))
            .build();
    ObjectNode response = (ObjectNode) JSON.readTree(fixture("identity-config-upsert.json"));
    ((ObjectNode) response.get("identity_config").get("github_repositories").get("repo"))
        .remove("authorization_token");
    response.put("id", "config_1");
    response.put("identity_id", "idn_1");
    response.put("template_id", "tmpl_1");
    server.enqueue(json(response.toString()));
    IdentityConfig result = client.identities().configs().upsert("idn_1", "tmpl_1", params);
    assertEquals(
        JSON.readTree(fixture("identity-config-upsert.json")),
        JSON.readTree(request().getBody().readUtf8()));
    IdentityConfigSpec spec = result.identityConfig();
    assertEquals("append", spec.system().mode());
    assertEquals("high", spec.model().effort());
    assertNull(spec.model().id());
    assertTrue(spec.tools().asOverrides().containsKey("Read"));
    assertNull(spec.tools().asOverrides().get("Read"));
    assertFalse(spec.managedToolConfig().get("create_forward_schedule").enabled());
    assertNull(spec.managedToolConfig().get("list_forward_schedules"));
    assertEquals("http", spec.mcpServers().get("crm").type());
    assertNull(spec.mcpServers().get("removed"));
    assertEquals("latest", spec.skills().get("skill_1").version());
    assertTrue(spec.vaults().get("vault_1").enabled());
    assertNull(spec.files().get("file_1"));
    assertNull(spec.environmentVariables().get("INHERIT"));
    assertEquals("unset", spec.environmentVariables().get("REMOVE").op());
    assertFalse(spec.toolsets().get("crm").tools().get("lookup").enabled());
    assertTrue(spec.agentMetadata().get("future").isNull());
    assertTrue(spec.githubRepositories().get("repo").field("authorization_token").isMissing());
    assertEquals(9007199254740993L, spec.field("future_option").get("value").asLong());
    assertFalse(params.toString().contains("test_write_only"));
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void compiledConfigUsesRuntimeShapesInsteadOfOverlayShapes() throws Exception {
    server.enqueue(json(fixture("effective-config.json")).setHeader("request-id", "req-effective"));
    EffectiveConfig result =
        client.async().identities().configs().getEffective("idn_1", "tmpl_1").get();
    assertEquals("req-effective", result.requestId());
    assertEquals("full-hash", result.effectiveHash());
    assertEquals("agent-hash", result.agentEffectiveHash());
    assertEquals("session-hash", result.sessionEffectiveHash());
    assertEquals("ultimate", result.agent().model().id());
    assertEquals(400000L, result.agent().model().contextWindow());
    assertEquals("Baseline\nPrefer CRM data.", result.agent().system());
    assertEquals(
        "always_ask", result.agent().tools().get(0).configs().get(0).permissionPolicy().type());
    assertEquals("crm", result.agent().mcpServers().get(0).name());
    assertEquals("skill_1", result.agent().skills().get(0).skillId());
    assertEquals(12L, result.agent().maxToolRounds());
    assertEquals("advisor", result.agent().multiagent().agents().get(0).type());
    assertEquals("support", result.agent().metadata().get("team").asString());
    assertTrue(result.agent().field("future_agent_field").asBoolean());
    assertEquals("user", result.session().environmentVariables().asMap().get("MODE"));
    assertEquals("vault_1", result.session().vaultIds().get(0));
    assertEquals("file_1", result.session().resources().get(0).fileId());
    assertNull(result.session().resources().get(0).url());
    assertEquals("/repo", result.session().resources().get(1).mountPath());
    assertEquals("preserved", result.field("future_response").asString());
    assertThrows(UnsupportedOperationException.class, () -> result.agent().tools().clear());
    EffectiveConfigSession text =
        EffectiveConfigSession.fromJson(
            JsonValue.parse("{\"environment_variables\":\"KEY=value\"}"));
    assertEquals("KEY=value", text.environmentVariables().asString());
  }

  @Test
  void nullEmptyAndAbsentOverlayValuesRemainDifferent() throws Exception {
    IdentityConfigSpec empty = IdentityConfigSpec.builder().build();
    assertEquals("{}", empty.rawJson());
    IdentityConfigSpec cleared =
        IdentityConfigSpec.builder()
            .system(null)
            .model(null)
            .managedToolConfig(null)
            .tools(IdentityTools.replacement(Collections.emptyList()))
            .environmentVariables(Collections.emptyMap())
            .build();
    server.enqueue(json("{\"identity_config\":{}}"));
    client
        .identities()
        .configs()
        .upsert(
            "idn_1",
            "tmpl_1",
            IdentityConfigUpsertParams.builder().identityConfig(cleared).metadata(null).build());
    assertEquals(
        JSON.readTree(
            "{\"identity_config\":{\"system\":null,\"model\":null,\"managed_tool_config\":null,\"tools\":[],\"environment_variables\":{}},\"metadata\":null}"),
        JSON.readTree(request().getBody().readUtf8()));
    assertTrue(cleared.tools().isReplacement());
    assertTrue(cleared.tools().asReplacement().isEmpty());
    assertTrue(cleared.field("skills").isMissing());
    assertTrue(cleared.field("system").isNull());
    assertEquals("auto", ModelOverride.of("auto").id());
    assertEquals(
        "custom",
        IdentityTools.replacement(Collections.singletonList(Tool.builder().type("custom").build()))
            .asReplacement()
            .get(0)
            .type());
  }

  @Test
  void buildersSnapshotMapsAndRejectMissingRequiredInputs() {
    Map<String, ResourceBinding> files = new LinkedHashMap<>();
    files.put("inherit", null);
    IdentityConfigSpec.Builder builder = IdentityConfigSpec.builder().files(files);
    files.clear();
    IdentityConfigSpec first = builder.build();
    builder.files(null);
    assertTrue(first.files().containsKey("inherit"));
    assertThrows(UnsupportedOperationException.class, () -> first.files().clear());
    assertThrows(ConfigurationException.class, () -> IdentityConfigUpsertParams.builder().build());
    assertThrows(
        ConfigurationException.class,
        () -> IdentityConfigUpsertParams.builder().identityConfig(null).build());
    assertThrows(ConfigurationException.class, () -> IdentityCreateParams.builder().build());
    assertThrows(ConfigurationException.class, () -> ManagedToolOverride.builder().build());
    assertThrows(
        ConfigurationException.class,
        () -> IdentityListParams.builder().identityIds(Collections.emptyList()));
    assertThrows(
        ConfigurationException.class,
        () -> IdentityConfigListParams.builder().afterId("a").beforeId("b").build());
    assertThrows(
        ResponseValidationException.class,
        () -> IdentityTools.overrides(Collections.emptyMap()).asReplacement());
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void identityFiltersAndCursorOverridesPersistAcrossLazyPages() throws Exception {
    server.enqueue(
        json("{\"data\":[{\"id\":\"z\"},{\"id\":\"a\"}],\"has_more\":true,\"last_id\":\"a\"}"));
    server.enqueue(json("{\"data\":[{\"id\":\"b\"}],\"has_more\":false}"));
    List<String> ids = new ArrayList<>(Arrays.asList("idn_1", "idn_2"));
    IdentityListParams params =
        IdentityListParams.builder()
            .externalId("customer +/")
            .identityIds(ids)
            .search("name ?")
            .enabled(false)
            .limit(200)
            .afterId("old")
            .build();
    ids.clear();
    IdentityListResponse first =
        client
            .identities()
            .list(
                params,
                RequestOptions.builder()
                    .query("after_id", "start")
                    .header("X-Trace", "trace")
                    .build());
    Iterator<Identity> items = first.iterator();
    assertEquals("z", items.next().id());
    assertEquals("a", items.next().id());
    assertEquals(1, server.getRequestCount());
    assertEquals("b", items.next().id());
    assertFalse(items.hasNext());
    for (String cursor : Arrays.asList("start", "a")) {
      RecordedRequest sent = request();
      assertEquals("idn_1,idn_2", sent.getRequestUrl().queryParameter("identity_ids"));
      assertEquals("false", sent.getRequestUrl().queryParameter("enabled"));
      assertEquals("200", sent.getRequestUrl().queryParameter("limit"));
      assertEquals("customer +/", sent.getRequestUrl().queryParameter("external_id"));
      assertEquals("name ?", sent.getRequestUrl().queryParameter("search"));
      assertEquals(cursor, sent.getRequestUrl().queryParameter("after_id"));
      assertEquals("trace", sent.getHeader("X-Trace"));
    }
  }

  @Test
  void configBackwardPagingRetainsIdentityScopeAndFilters() throws Exception {
    server.enqueue(
        json(
            "{\"data\":[{\"id\":\"config_a\"}],\"has_more\":true,\"first_id\":\"config_a\",\"last_id\":\"different\"}"));
    server.enqueue(
        json("{\"data\":[{\"id\":\"config_b\"}],\"has_more\":false}")
            .setHeader("x-request-id", "req-next"));
    IdentityConfigListResponse first =
        client
            .async()
            .identities()
            .configs()
            .list(
                ID,
                IdentityConfigListParams.builder()
                    .templateId("tmpl +/")
                    .status("archived")
                    .limit(5)
                    .beforeId("start")
                    .build())
            .get();
    IdentityConfigListResponse next = first.getNextPageAsync().get();
    assertEquals("req-next", next.data().get(0).requestId());
    for (String cursor : Arrays.asList("start", "config_a")) {
      RecordedRequest sent = request();
      assertEquals(
          "/api/v1/forward/identities/idn%2Fa%20%3F%23%25/templates",
          sent.getRequestUrl().encodedPath());
      assertEquals("tmpl +/", sent.getRequestUrl().queryParameter("template_id"));
      assertEquals("archived", sent.getRequestUrl().queryParameter("status"));
      assertEquals(cursor, sent.getRequestUrl().queryParameter("before_id"));
      assertNull(sent.getRequestUrl().queryParameter("after_id"));
    }
    assertFalse(next.hasNextPage());
    assertThrows(PaginationException.class, next::getNextPage);
  }

  @Test
  void nestedPageTimeoutAndConflictUseSharedRequestBehavior() {
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .identities()
                .configs()
                .list(
                    "idn_1",
                    IdentityConfigListParams.builder().beforeId("start").build(),
                    RequestOptions.builder().query("after_id", "other").build()));
    assertEquals(0, server.getRequestCount());
    server.enqueue(
        json("{\"data\":[{\"id\":\"config_a\"}],\"has_more\":true,\"last_id\":\"config_a\"}"));
    server.enqueue(json("{\"data\":[],\"has_more\":false}").setHeadersDelay(1, TimeUnit.SECONDS));
    IdentityConfigListResponse first =
        client
            .identities()
            .configs()
            .list(
                "idn_1",
                IdentityConfigListParams.builder().build(),
                RequestOptions.builder().timeout(Duration.ofMillis(150)).maxRetries(0).build());
    assertThrows(ApiTimeoutException.class, first::getNextPage);
  }

  @Test
  void statsAndCleanupResponsesHaveLosslessCounters() throws Exception {
    server.enqueue(
        json(
            "{\"total_identities\":9007199254740993,\"active_identities\":2,\"total_agents\":3,\"total_sessions\":4}"));
    IdentityStats stats = client.identities().stats();
    assertEquals(9007199254740993L, stats.totalIdentities());
    assertEquals(3L, stats.totalTemplates());
    server.enqueue(
        json(
            "{\"identity_id\":\"idn_1\",\"status\":\"completed\",\"completed_at\":\"2026-10-16T00:00:00Z\",\"summary\":{\"identity_configs_archived\":1,\"resource_bindings_archived\":2,\"identity_owned_resources_archived\":3,\"schedules_archived\":4,\"schedule_runs_skipped\":5,\"sessions_archived\":6},\"future\":true}"));
    IdentityClearResponse cleared =
        client.async().identities().clear("idn_1", IdentityClearParams.builder().build()).get();
    assertEquals("completed", cleared.status());
    assertEquals(6L, cleared.summary().sessionsArchived());
    assertEquals(1L, cleared.summary().identityConfigsArchived());
    assertEquals(2L, cleared.summary().resourceBindingsArchived());
    assertEquals(3L, cleared.summary().identityOwnedResourcesArchived());
    assertEquals(4L, cleared.summary().schedulesArchived());
    assertEquals(5L, cleared.summary().scheduleRunsSkipped());
    assertTrue(cleared.field("future").asBoolean());
    server.enqueue(
        json(
            "{\"data\":[{\"template_id\":\"tmpl_1\",\"template_name\":\"Support\",\"session_count\":10,\"last_active_at\":null}]}"));
    IdentityTemplate template = client.identities().listTemplates("idn_1").data().get(0);
    assertEquals("Support", template.templateName());
    assertEquals(10L, template.sessionCount());
    assertNull(template.lastActiveAt());
  }

  @Test
  void configWriteDoesNotRetryUnsafe503AndRetainsConflictCode() throws Exception {
    RequestOptions retries = RequestOptions.builder().maxRetries(2).build();
    server.enqueue(json("{}").setResponseCode(503).setHeader("Retry-After", "0"));
    assertThrows(
        ApiStatusException.class,
        () ->
            client
                .identities()
                .configs()
                .upsert(
                    "idn_1",
                    "tmpl_1",
                    IdentityConfigUpsertParams.builder()
                        .identityConfig(IdentityConfigSpec.builder().build())
                        .build(),
                    retries));
    assertEquals(1, server.getRequestCount());
    server.enqueue(
        json("{\"error\":{\"type\":\"conflict_error\",\"code\":\"ADMIN_IDENTITY_READ_ONLY\",\"message\":\"Read"
                 + " only\"}}")
            .setResponseCode(409)
            .setHeader("x-request-id", "req-conflict"));
    ApiStatusException error =
        assertThrows(
            ApiStatusException.class, () -> client.identities().disable("idn_admin", retries));
    assertEquals("ADMIN_IDENTITY_READ_ONLY", error.code());
    assertEquals("req-conflict", error.requestId());
    assertEquals(2, server.getRequestCount());
  }
}
