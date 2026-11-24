package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ApiStatusException;
import com.qoder.cloudagents.errors.ConfigurationException;
import com.qoder.cloudagents.errors.ResponseValidationException;
import com.qoder.cloudagents.forward.*;
import com.qoder.cloudagents.streaming.EventStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Timeout(15)
class CapabilityParityTest {
  private MockWebServer server;
  private ForwardClient client;

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.start();
    client =
        ForwardClient.builder()
            .baseUrl(server.url("/api/v1/forward").toString())
            .pat("test")
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
    RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(request);
    return request;
  }

  private void assertJson(String expected, String actual) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    assertEquals(mapper.readTree(expected), mapper.readTree(actual));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void automaticTitlesAndSessionRosterUpdates(boolean async) throws Exception {
    // Contract: official api/forward/sessions/{create,update}; offline synthetic data only.
    String[] flags = {"", ",\"auto_title\":null", ",\"auto_title\":false", ",\"auto_title\":true"};
    for (int state = 0; state < flags.length; state++) {
      SessionCreateParams.Builder builder =
          SessionCreateParams.builder().identityId("idn_test").templateId("tmpl_test");
      if (state > 0) builder.autoTitle(state == 1 ? null : state == 3);
      SessionCreateParams create = builder.build();
      String responseBody = "{\"id\":\"sess_test\",\"title\":\"\",\"config\":{}}";
      server.enqueue(json(responseBody));
      Session response =
          async ? client.async().sessions().create(create).get() : client.sessions().create(create);
      RecordedRequest wire = request();
      assertEquals("POST", wire.getMethod());
      assertEquals("/api/v1/forward/sessions", wire.getPath());
      String sent = wire.getBody().readUtf8();
      assertJson(
          "{\"identity_id\":\"idn_test\",\"template_id\":\"tmpl_test\""
              + flags[state]
              + ",\"metadata\":{\"__qca_sdk_created\":\"true\",\"__qca_sdk_language\":\"java\",\"__qca_sdk_version\":\""
              + wire.getHeader("X-Qoder-Package-Version")
              + "\"}}",
          sent);
      assertTrue(JsonValue.parse(sent).get("title").isMissing());
      if (state < 2) assertNull(create.autoTitle());
      else assertEquals(state == 3, create.autoTitle());
      assertJson(responseBody, response.rawJson());
      assertEquals("", response.title());
    }

    MultiagentConfig roster =
        MultiagentConfig.builder()
            .type("coordinator")
            .agents(
                Arrays.asList(
                    MultiagentEntry.of("tmpl_child"),
                    MultiagentEntry.builder().type("agent").id("agent_child").build(),
                    MultiagentEntry.builder().type("self").build(),
                    MultiagentEntry.builder().type("advisor").model("ultimate").build()))
            .build();
    String saved =
        "{\"type\":\"coordinator\",\"agents\":[\"tmpl_child\",{\"type\":\"agent\",\"id\":\"agent_child\"},{\"type\":\"self\"},{\"type\":\"advisor\",\"model\":\"ultimate\"}]}";
    for (int state = 0; state < 3; state++) {
      SessionUpdateConfig.Builder config = SessionUpdateConfig.builder();
      if (state == 1) config.multiagent(null);
      if (state == 2) config.multiagent(roster);
      SessionUpdateParams update = SessionUpdateParams.builder().config(config.build()).build();
      String payload = state == 0 ? "{}" : "{\"multiagent\":" + (state == 1 ? "null" : saved) + "}";
      String responseBody = "{\"id\":\"sess_test\",\"config\":" + payload + "}";
      server.enqueue(json(responseBody));
      Session response =
          async
              ? client.async().sessions().update("sess_test", update).get()
              : client.sessions().update("sess_test", update);
      RecordedRequest wire = request();
      assertEquals("POST", wire.getMethod());
      assertEquals("/api/v1/forward/sessions/sess_test", wire.getPath());
      assertJson("{\"config\":" + payload + "}", wire.getBody().readUtf8());
      assertJson(responseBody, response.rawJson());
      assertEquals(state == 0, response.config().field("multiagent").isMissing());
      assertEquals(state == 1, response.config().field("multiagent").isNull());
      if (state < 2) assertNull(response.config().multiagent());
      else {
        assertEquals("coordinator", response.config().multiagent().type());
        assertEquals(4, response.config().multiagent().agents().size());
        assertTrue(response.config().multiagent().agents().get(0).isString());
        assertEquals("tmpl_child", response.config().multiagent().agents().get(0).templateId());
        assertEquals("agent_child", response.config().multiagent().agents().get(1).id());
        assertEquals("self", response.config().multiagent().agents().get(2).type());
        assertEquals("advisor", response.config().multiagent().agents().get(3).type());
        assertEquals("ultimate", response.config().multiagent().agents().get(3).model());
      }
    }
    Template template = new Template(JsonValue.parse("{\"multiagent\":" + saved + "}"), null);
    assertEquals(saved, template.multiagent().rawJson());
    assertEquals("\"tmpl_child\"", template.multiagent().agents().get(0).rawJson());
    assertThrows(
        ResponseValidationException.class, () -> MultiagentEntry.fromJson(JsonValue.of(3)));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void automaticTitleConflictsPreserveServerErrors(boolean async) throws Exception {
    // Contract: forward-current/internal/handler/session/{handler.go,auto_title_test.go}.
    String errorBody =
        "{\"type\":\"error\",\"request_id\":\"req_title\",\"error\":{\"type\":\"invalid_request_error\",\"code\":\"invalid_request\",\"message\":\"title"
            + " must not be supplied when auto_title is enabled\"}}";
    String[] titles = {"Normal title", "", null};
    String[] titleJson = {"\"Normal title\"", "\"\"", "null"};
    for (int state = 0; state < titles.length; state++) {
      SessionCreateParams create =
          SessionCreateParams.builder()
              .identityId("idn_test")
              .templateId("tmpl_test")
              .autoTitle(true)
              .title(titles[state])
              .build();
      server.enqueue(json(errorBody).setResponseCode(400).setHeader("X-Request-ID", "req_title"));
      ApiStatusException error;
      if (async) {
        Throwable cause =
            assertThrows(
                    java.util.concurrent.ExecutionException.class,
                    () -> client.async().sessions().create(create).get())
                .getCause();
        assertInstanceOf(ApiStatusException.class, cause);
        error = (ApiStatusException) cause;
      } else {
        error = assertThrows(ApiStatusException.class, () -> client.sessions().create(create));
      }
      RecordedRequest wire = request();
      assertEquals("POST", wire.getMethod());
      assertEquals("/api/v1/forward/sessions", wire.getPath());
      assertJson(
          "{\"identity_id\":\"idn_test\",\"template_id\":\"tmpl_test\",\"auto_title\":true,\"title\":"
              + titleJson[state]
              + ",\"metadata\":{\"__qca_sdk_created\":\"true\",\"__qca_sdk_language\":\"java\",\"__qca_sdk_version\":\""
              + wire.getHeader("X-Qoder-Package-Version")
              + "\"}}",
          wire.getBody().readUtf8());
      assertEquals(400, error.statusCode());
      assertEquals("invalid_request_error", error.type());
      assertEquals("invalid_request", error.code());
      assertEquals("req_title", error.requestId());
      assertJson(errorBody, error.body().toJson());
    }
    assertEquals(3, server.getRequestCount());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void a2aConfigurationAndAuthenticatedAgentCards(boolean async) throws Exception {
    // Contract: official api/forward/templates/{create,update} and a2a-integration.
    TemplateA2ASkill skill =
        TemplateA2ASkill.builder()
            .id("summary")
            .name("Summarize")
            .description("Summarize text")
            .tags(Collections.singletonList("text"))
            .examples(Collections.singletonList("Summarize notes"))
            .build();
    TemplateA2AConfig config =
        TemplateA2AConfig.builder()
            .enabled(true)
            .description("Public summary")
            .skills(Collections.singletonList(skill))
            .build();
    TemplateCreateParams create =
        TemplateCreateParams.builder()
            .name("Summary")
            .modelId("ultimate")
            .environmentId("env_test")
            .a2a(config)
            .build();
    String a2aJson =
        "{\"enabled\":true,\"description\":\"Public"
            + " summary\",\"skills\":[{\"id\":\"summary\",\"name\":\"Summarize\",\"description\":\"Summarize"
            + " text\",\"tags\":[\"text\"],\"examples\":[\"Summarize notes\"]}]}";
    String templateJson = "{\"id\":\"tmpl_test\",\"a2a\":" + a2aJson + "}";
    server.enqueue(json(templateJson));
    Template template =
        async ? client.async().templates().create(create).get() : client.templates().create(create);
    RecordedRequest createWire = request();
    assertEquals("POST", createWire.getMethod());
    assertEquals("/api/v1/forward/templates", createWire.getPath());
    assertJson(
        "{\"name\":\"Summary\",\"model\":\"ultimate\",\"environment_id\":\"env_test\",\"a2a\":"
            + a2aJson
            + "}",
        createWire.getBody().readUtf8());
    assertJson(templateJson, template.rawJson());
    assertTrue(template.field("skills").isMissing());
    assertTrue(template.a2a().enabled());
    assertEquals("Public summary", template.a2a().description());
    TemplateA2ASkill responseSkill = template.a2a().skills().get(0);
    assertEquals("summary", responseSkill.id());
    assertEquals("Summarize", responseSkill.name());
    assertEquals("Summarize text", responseSkill.description());
    assertEquals(Collections.singletonList("text"), responseSkill.tags());
    assertEquals(Collections.singletonList("Summarize notes"), responseSkill.examples());
    List<TemplateUpdateParams> updates =
        Arrays.asList(
            TemplateUpdateParams.builder().build(),
            TemplateUpdateParams.builder().a2a(null).build(),
            TemplateUpdateParams.builder().a2a(TemplateA2AConfig.builder().build()).build(),
            TemplateUpdateParams.builder()
                .a2a(TemplateA2AConfig.builder().skills(Collections.emptyList()).build())
                .build());
    String[] expectedUpdates = {
      "{}", "{\"a2a\":null}", "{\"a2a\":{}}", "{\"a2a\":{\"skills\":[]}}"
    };
    String[] responses = {
      templateJson,
      "{\"id\":\"tmpl_test\",\"a2a\":null}",
      templateJson,
      "{\"id\":\"tmpl_test\",\"a2a\":{\"enabled\":true,\"description\":\"Public"
          + " summary\",\"skills\":[]}}"
    };
    for (int state = 0; state < updates.size(); state++) {
      TemplateUpdateParams update = updates.get(state);
      server.enqueue(json(responses[state]));
      Template response =
          async
              ? client.async().templates().update("tmpl_test", update).get()
              : client.templates().update("tmpl_test", update);
      RecordedRequest wire = request();
      assertEquals("POST", wire.getMethod());
      assertEquals("/api/v1/forward/templates/tmpl_test", wire.getPath());
      assertJson(expectedUpdates[state], wire.getBody().readUtf8());
      assertJson(responses[state], response.rawJson());
      assertEquals(state == 0, update.field("a2a").isMissing());
      assertEquals(state == 1, response.field("a2a").isNull());
      if (state == 1) assertNull(response.a2a());
      if (state == 2) assertTrue(update.field("a2a").get("skills").isMissing());
      if (state == 3) {
        assertEquals(Collections.emptyList(), update.field("a2a").get("skills").asArray());
        assertEquals(Collections.emptyList(), response.a2a().skills());
      }
    }
    for (String version : Arrays.asList("0.3", "1.0")) {
      String endpoint = "http://example.invalid/a2a?identity_id=idn_test";
      String card =
          "{\"name\":\"Summary\",\"description\":\"Public\",\"version\":\"1\",\"skills\":[],\"defaultInputModes\":[\"text/plain\"],\"defaultOutputModes\":[\"text/plain\"],\"future\":true,"
              + (version.equals("0.3")
                  ? "\"url\":\"" + endpoint + "\""
                  : "\"supportedInterfaces\":[{\"url\":\""
                      + endpoint
                      + "\",\"protocolBinding\":\"JSONRPC\",\"protocolVersion\":\"1.0\"}]")
              + "}";
      server.enqueue(json(card).setHeader("X-Request-ID", "req_card"));
      TemplateAgentCardParams params =
          TemplateAgentCardParams.builder().identityId("idn/a ?#%").a2aVersion(version).build();
      ApiResponse<TemplateAgentCard> response =
          async
              ? client
                  .async()
                  .templates()
                  .withResponse()
                  .retrieveAgentCard("tmpl/a ?#%", params)
                  .get()
              : client.templates().withResponse().retrieveAgentCard("tmpl/a ?#%", params);
      RecordedRequest wire = request();
      assertEquals("GET", wire.getMethod());
      assertEquals(
          "/api/v1/forward/templates/tmpl%2Fa%20%3F%23%25/a2a/agent-card.json",
          wire.getRequestUrl().encodedPath());
      assertEquals("idn/a ?#%", wire.getRequestUrl().queryParameter("identity_id"));
      assertEquals(version, wire.getHeader("A2A-Version"));
      assertEquals("Bearer test", wire.getHeader("Authorization"));
      assertEquals(0, wire.getBodySize());
      assertEquals("req_card", response.data().requestId());
      assertJson(card, response.data().rawJson());
      assertEquals("Summary", response.data().name());
      assertEquals("Public", response.data().description());
      assertEquals("1", response.data().version());
      assertTrue(response.data().field("future").asBoolean());
      assertEquals(Collections.emptyList(), response.data().skills());
      assertEquals(Collections.singletonList("text/plain"), response.data().defaultInputModes());
      assertEquals(Collections.singletonList("text/plain"), response.data().defaultOutputModes());
      if (version.equals("0.3")) {
        assertEquals(endpoint, response.data().url());
        assertNull(response.data().supportedInterfaces());
        assertTrue(response.data().field("supportedInterfaces").isMissing());
      } else {
        assertNull(response.data().url());
        assertTrue(response.data().field("url").isMissing());
        assertEquals(1, response.data().supportedInterfaces().size());
        TemplateAgentInterface iface = response.data().supportedInterfaces().get(0);
        assertEquals(endpoint, iface.url());
        assertEquals("JSONRPC", iface.protocolBinding());
        assertEquals("1.0", iface.protocolVersion());
      }
    }
    for (boolean withResponse : Arrays.asList(false, true)) {
      server.enqueue(json("{\"name\":\"Summary\",\"supportedInterfaces\":[]}"));
      TemplateAgentCardParams params = TemplateAgentCardParams.builder().a2aVersion("0.3").build();
      RequestOptions override = RequestOptions.builder().header("a2a-VERSION", "1.0").build();
      TemplateAgentCard response;
      if (async) {
        response =
            withResponse
                ? client
                    .async()
                    .templates()
                    .withResponse()
                    .retrieveAgentCard("tmpl_test", params, override)
                    .get()
                    .data()
                : client.async().templates().retrieveAgentCard("tmpl_test", params, override).get();
      } else {
        response =
            withResponse
                ? client
                    .templates()
                    .withResponse()
                    .retrieveAgentCard("tmpl_test", params, override)
                    .data()
                : client.templates().retrieveAgentCard("tmpl_test", params, override);
      }
      RecordedRequest wire = request();
      assertEquals("GET", wire.getMethod());
      assertEquals("/api/v1/forward/templates/tmpl_test/a2a/agent-card.json", wire.getPath());
      assertEquals("1.0", wire.getHeader("A2A-Version"));
      assertEquals(0, wire.getBodySize());
      assertJson("{\"name\":\"Summary\",\"supportedInterfaces\":[]}", response.rawJson());
      assertEquals(Collections.emptyList(), response.supportedInterfaces());
    }
    try (ForwardClient sat =
        ForwardClient.builder()
            .baseUrl(server.url("/api/v1/forward").toString())
            .authMode(com.qoder.cloudagents.auth.AuthMode.SA)
            .accessToken("test-sat")
            .build()) {
      server.enqueue(json("{\"name\":\"Summary\"}"));
      TemplateAgentCard response =
          async
              ? sat.async().templates().retrieveAgentCard("tmpl_test").get()
              : sat.templates().retrieveAgentCard("tmpl_test");
      RecordedRequest wire = request();
      assertEquals("GET", wire.getMethod());
      assertEquals("/api/v1/forward/templates/tmpl_test/a2a/agent-card.json", wire.getPath());
      assertNull(wire.getRequestUrl().query());
      assertNull(wire.getHeader("A2A-Version"));
      assertEquals("Bearer test-sat", wire.getHeader("Authorization"));
      assertEquals(0, wire.getBodySize());
      assertJson("{\"name\":\"Summary\"}", response.rawJson());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void ossRequestsKeepSecretsSeparateFromMaskedResponses(boolean async) throws Exception {
    // Contract: official api/environments/storage-buckets (write input versus masked read view).
    for (String credentialType : Arrays.asList("ram_role", "access_key")) {
      EnvironmentStorageCredentialParams.Builder credentials =
          EnvironmentStorageCredentialParams.builder().type(credentialType);
      if (credentialType.equals("ram_role")) credentials.roleArn("test-role-arn");
      else credentials.accessKeyId("test-key-id").accessKeySecret("test-write-only-secret");
      EnvironmentStorageBucketParams bucket =
          EnvironmentStorageBucketParams.builder()
              .name("data")
              .provider("aliyun-oss")
              .region("cn-hangzhou")
              .bucket("test-bucket")
              .prefix("team/")
              .access("read_only")
              .acceleratorEndpoint("test.oss-data-acc.aliyuncs.com")
              .credential(credentials.build())
              .build();
      EnvironmentConfig config =
          EnvironmentConfig.builder()
              .type("cloud")
              .storage(
                  EnvironmentStorageParams.builder()
                      .buckets(Collections.singletonList(bucket))
                      .build())
              .build();
      EnvironmentCreateParams create =
          EnvironmentCreateParams.builder().name("OSS test").config(config).build();
      String payload =
          "{\"id\":\"env_test\",\"config\":{\"type\":\"cloud\",\"storage\":{\"buckets\":[{\"name\":\"data\",\"provider\":\"aliyun-oss\",\"region\":\"cn-hangzhou\",\"bucket\":\"test-bucket\",\"credential\":{\"type\":\""
              + credentialType
              + "\""
              + (credentialType.equals("access_key") ? ",\"access_key_id\":\"test****id\"" : "")
              + "}}]}}}";
      server.enqueue(json(payload));
      RequestOptions options =
          RequestOptions.builder().header("X-Qoder-Beta", "oss-bucket-2026-09-23").build();
      Environment response =
          async
              ? client.async().environments().create(create, options).get()
              : client.environments().create(create, options);
      RecordedRequest wire = request();
      assertEquals("POST", wire.getMethod());
      assertEquals("/api/v1/forward/environments", wire.getPath());
      assertEquals("oss-bucket-2026-09-23", wire.getHeader("X-Qoder-Beta"));
      String credentialJson =
          credentialType.equals("ram_role")
              ? "{\"type\":\"ram_role\",\"role_arn\":\"test-role-arn\"}"
              : "{\"type\":\"access_key\",\"access_key_id\":\"test-key-id\",\"access_key_secret\":\"test-write-only-secret\"}";
      assertJson(
          "{\"name\":\"OSS"
              + " test\",\"config\":{\"type\":\"cloud\",\"storage\":{\"buckets\":[{\"name\":\"data\",\"provider\":\"aliyun-oss\",\"region\":\"cn-hangzhou\",\"bucket\":\"test-bucket\",\"prefix\":\"team/\",\"access\":\"read_only\",\"accelerator_endpoint\":\"test.oss-data-acc.aliyuncs.com\",\"credential\":"
              + credentialJson
              + "}]}}}",
          wire.getBody().readUtf8());
      assertEquals("aliyun-oss", bucket.provider());
      assertEquals("cn-hangzhou", bucket.region());
      assertEquals("test-bucket", bucket.bucket());
      assertEquals("team/", bucket.prefix());
      assertEquals("read_only", bucket.access());
      assertEquals("test.oss-data-acc.aliyuncs.com", bucket.acceleratorEndpoint());
      assertJson(payload, response.rawJson());
      assertEquals(1, response.config().storage().buckets().size());
      EnvironmentStorageBucket readBucket = response.config().storage().buckets().get(0);
      assertEquals("data", readBucket.name());
      assertEquals("aliyun-oss", readBucket.provider());
      assertEquals("cn-hangzhou", readBucket.region());
      assertEquals("test-bucket", readBucket.bucket());
      assertNull(readBucket.prefix());
      assertNull(readBucket.access());
      assertNull(readBucket.acceleratorEndpoint());
      EnvironmentStorageCredential redacted = readBucket.credential();
      assertEquals(credentialType, redacted.type());
      if (credentialType.equals("access_key")) assertEquals("test****id", redacted.accessKeyId());
      else {
        assertNull(redacted.accessKeyId());
        assertTrue(redacted.field("access_key_id").isMissing());
      }
      assertTrue(redacted.field("access_key_secret").isMissing());
      assertTrue(redacted.field("role_arn").isMissing());
      assertFalse(create.toString().contains("test-write-only-secret"));
      assertFalse(config.toString().contains("test-write-only-secret"));
    }
    EnvironmentStorageBucketParams keep =
        EnvironmentStorageBucketParams.builder()
            .name("data")
            .provider("aliyun-oss")
            .region("cn-hangzhou")
            .bucket("test-bucket")
            .access("read_write")
            .build();
    assertTrue(keep.field("credential").isMissing());
    for (List<EnvironmentStorageBucketParams> buckets :
        Arrays.asList(
            Collections.singletonList(keep),
            Collections.<EnvironmentStorageBucketParams>emptyList())) {
      EnvironmentUpdateParams update =
          EnvironmentUpdateParams.builder()
              .config(
                  EnvironmentConfig.builder()
                      .type("cloud")
                      .storage(EnvironmentStorageParams.builder().buckets(buckets).build())
                      .build())
              .build();
      String expectedBuckets =
          buckets.isEmpty()
              ? "[]"
              : "[{\"name\":\"data\",\"provider\":\"aliyun-oss\",\"region\":\"cn-hangzhou\",\"bucket\":\"test-bucket\",\"access\":\"read_write\"}]";
      String responseBuckets =
          buckets.isEmpty()
              ? "[]"
              : "[{\"name\":\"data\",\"provider\":\"aliyun-oss\",\"region\":\"cn-hangzhou\",\"bucket\":\"test-bucket\",\"access\":\"read_write\",\"credential\":{\"type\":\"ram_role\"}}]";
      String responseBody =
          "{\"id\":\"env_test\",\"config\":{\"type\":\"cloud\",\"storage\":{\"buckets\":"
              + responseBuckets
              + "}}}";
      server.enqueue(json(responseBody));
      RequestOptions options =
          buckets.isEmpty()
              ? RequestOptions.DEFAULT
              : RequestOptions.builder().header("X-Qoder-Beta", "oss-bucket-2026-09-23").build();
      Environment response =
          async
              ? client.async().environments().update("env_test", update, options).get()
              : client.environments().update("env_test", update, options);
      RecordedRequest wire = request();
      assertEquals("POST", wire.getMethod());
      assertEquals("/api/v1/forward/environments/env_test", wire.getPath());
      assertEquals(
          buckets.isEmpty() ? null : "oss-bucket-2026-09-23", wire.getHeader("X-Qoder-Beta"));
      String sent = wire.getBody().readUtf8();
      assertJson(
          "{\"config\":{\"type\":\"cloud\",\"storage\":{\"buckets\":" + expectedBuckets + "}}}",
          sent);
      assertJson(responseBody, response.rawJson());
      if (buckets.isEmpty()) {
        assertEquals(Collections.emptyList(), response.config().storage().buckets());
        assertEquals(Collections.emptyList(), update.config().storage().buckets());
      } else {
        assertTrue(
            JsonValue.parse(sent)
                .get("config")
                .get("storage")
                .get("buckets")
                .asArray()
                .get(0)
                .get("credential")
                .isMissing());
        assertEquals("ram_role", response.config().storage().buckets().get(0).credential().type());
      }
    }
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{}",
        "{\"provider\":null,\"region\":null,\"bucket\":null,\"prefix\":null,\"access\":null,\"accelerator_endpoint\":null}",
        "{\"provider\":\"\",\"region\":\"\",\"bucket\":\"\",\"prefix\":\"\",\"access\":\"\",\"accelerator_endpoint\":\"\"}"
      })
  void storageBucketGettersPreserveOptionalStringStates(String payload) throws Exception {
    EnvironmentStorageBucketParams bucket =
        EnvironmentStorageBucketParams.fromJson(JsonValue.parse(payload));
    String expected = payload.contains("\"\"") ? "" : null;
    assertEquals(expected, bucket.provider());
    assertEquals(expected, bucket.region());
    assertEquals(expected, bucket.bucket());
    assertEquals(expected, bucket.prefix());
    assertEquals(expected, bucket.access());
    assertEquals(expected, bucket.acceleratorEndpoint());
    for (String field :
        Arrays.asList("provider", "region", "bucket", "prefix", "access", "accelerator_endpoint")) {
      assertEquals(payload.equals("{}"), bucket.field(field).isMissing());
      assertEquals(expected == null && !payload.equals("{}"), bucket.field(field).isNull());
    }
    assertJson(payload, bucket.rawJson());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void managedThreadSelectorsSurviveReconnect(boolean async) throws Exception {
    // Contract: cas-current/internal/sse/{stream.go,stream_event_deltas_test.go}.
    try (ManagedClient managed =
        ManagedClient.builder()
            .baseUrl(server.url("/api/v1/cloud").toString())
            .pat("test")
            .maxRetries(0)
            .build()) {
      List<String> deltas =
          new ArrayList<>(Arrays.asList("agent.message", "agent.thinking", "agent.message"));
      com.qoder.cloudagents.managed.SessionThreadEventStreamParams params =
          com.qoder.cloudagents.managed.SessionThreadEventStreamParams.builder()
              .lastEventId("evt_old")
              .eventDeltas(deltas)
              .build();
      deltas.clear();
      assertEquals(3, params.eventDeltas().size());
      assertThrows(UnsupportedOperationException.class, () -> params.eventDeltas().clear());
      server.enqueue(
          new MockResponse()
              .setHeader("Content-Type", "text/event-stream")
              .setBody(
                  "id: evt_message\n"
                      + "data:"
                      + " {\"id\":\"evt_message\",\"type\":\"agent.message\",\"content\":\"hello\"}\n\n"));
      server.enqueue(
          new MockResponse()
              .setHeader("Content-Type", "text/event-stream")
              .setBody(
                  "id: evt_end\n"
                      + "data:"
                      + " {\"id\":\"evt_end\",\"type\":\"session.thread_status_terminated\"}\n\n"));
      RequestOptions options = RequestOptions.builder().totalTimeout(Duration.ofSeconds(5)).build();
      try (EventStream<com.qoder.cloudagents.managed.SessionEvent> stream =
          async
              ? managed
                  .async()
                  .sessions()
                  .threads()
                  .events()
                  .resumableStream("s/a", "t/b", params, options)
              : managed
                  .sessions()
                  .threads()
                  .events()
                  .resumableStream("s/a", "t/b", params, options)) {
        Iterator<com.qoder.cloudagents.managed.SessionEvent> events = stream.iterator();
        assertJson(
            "{\"id\":\"evt_message\",\"type\":\"agent.message\",\"content\":\"hello\"}",
            events.next().rawJson());
        assertJson(
            "{\"id\":\"evt_end\",\"type\":\"session.thread_status_terminated\"}",
            events.next().rawJson());
        assertFalse(events.hasNext());
      }
      for (String cursor : Arrays.asList("evt_old", "evt_message")) {
        RecordedRequest wire = request();
        assertEquals(
            "/api/v1/cloud/sessions/s%2Fa/threads/t%2Fb/stream",
            wire.getRequestUrl().encodedPath());
        assertEquals(
            Arrays.asList("agent.message", "agent.thinking", "agent.message"),
            wire.getRequestUrl().queryParameterValues("event_deltas[]"));
        assertEquals("GET", wire.getMethod());
        assertEquals(0, wire.getBodySize());
        assertEquals(3, wire.getRequestUrl().querySize());
        assertEquals(cursor, wire.getHeader("Last-Event-ID"));
        assertNull(wire.getRequestUrl().queryParameter("last_event_id"));
      }
      assertThrows(
          ConfigurationException.class,
          () ->
              managed
                  .sessions()
                  .threads()
                  .events()
                  .resumableStream(
                      "s",
                      "t",
                      params,
                      RequestOptions.builder().query("event_types[]", "agent.message").build()));
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void managedMcpDefaultDenyUsesTopLevelPolicy(boolean async) throws Exception {
    // Contract: cas-current/internal/turn/ingest/freeze_policy_agreement_test.go.
    try (ManagedClient managed =
        ManagedClient.builder()
            .baseUrl(server.url("/api/v1/cloud").toString())
            .pat("test")
            .maxRetries(0)
            .build()) {
      com.qoder.cloudagents.managed.Tool tool =
          com.qoder.cloudagents.managed.Tool.builder()
              .type("mcp_toolset")
              .mcpServerName("test")
              .permissionPolicy(
                  com.qoder.cloudagents.managed.PermissionPolicy.builder()
                      .type("always_deny")
                      .build())
              .build();
      com.qoder.cloudagents.managed.AgentCreateParams params =
          com.qoder.cloudagents.managed.AgentCreateParams.builder()
              .name("deny")
              .model("auto")
              .tools(Collections.singletonList(tool))
              .build();
      server.enqueue(
          json(
              "{\"id\":\"agent_test\",\"tools\":[{\"type\":\"mcp_toolset\",\"mcp_server_name\":\"test\",\"permission_policy\":{\"type\":\"always_deny\"}}]}"));
      com.qoder.cloudagents.managed.Agent response =
          async ? managed.async().agents().create(params).get() : managed.agents().create(params);
      RecordedRequest wire = request();
      assertEquals("POST", wire.getMethod());
      assertEquals("/api/v1/cloud/agents", wire.getPath());
      String requestBody = wire.getBody().readUtf8();
      assertJson(
          "{\"name\":\"deny\",\"model\":\"auto\",\"tools\":[{\"type\":\"mcp_toolset\",\"mcp_server_name\":\"test\",\"permission_policy\":{\"type\":\"always_deny\"}}]}",
          requestBody);
      assertJson(
          "{\"id\":\"agent_test\",\"tools\":[{\"type\":\"mcp_toolset\",\"mcp_server_name\":\"test\",\"permission_policy\":{\"type\":\"always_deny\"}}]}",
          response.rawJson());
      JsonValue sent = JsonValue.parse(requestBody).get("tools").asArray().get(0);
      assertEquals("always_deny", sent.get("permission_policy").get("type").asString());
      assertTrue(sent.get("default_config").isMissing());
      assertTrue(sent.get("configs").isMissing());
      assertEquals("always_deny", response.tools().get(0).permissionPolicy().type());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void managedOauthResourceAndRedactedRefreshFields(boolean async) throws Exception {
    // Contract: official api/vaults/{schemas,create-credential} (write and redacted read views).
    try (ManagedClient managed =
        ManagedClient.builder()
            .baseUrl(server.url("/api/v1/cloud").toString())
            .pat("test")
            .maxRetries(0)
            .build()) {
      com.qoder.cloudagents.managed.VaultCredentialOAuthRefresh refresh =
          com.qoder.cloudagents.managed.VaultCredentialOAuthRefresh.builder()
              .clientId("test-client")
              .refreshToken("test-refresh")
              .tokenEndpoint("https://example.invalid/token")
              .resource("https://example.invalid/mcp")
              .scope("read")
              .tokenEndpointAuth(
                  JsonValue.parse(
                      "{\"type\":\"client_secret_post\",\"client_secret\":\"test-secret\"}"))
              .build();
      com.qoder.cloudagents.managed.VaultCredentialCreateParams create =
          com.qoder.cloudagents.managed.VaultCredentialCreateParams.builder()
              .auth(
                  com.qoder.cloudagents.managed.VaultCredentialCreateAuth.builder()
                      .type("mcp_oauth")
                      .mcpServerUrl("https://example.invalid/mcp")
                      .accessToken("test-access")
                      .refresh(refresh)
                      .build())
              .build();
      server.enqueue(
          json(
              "{\"id\":\"cred_test\",\"auth\":{\"type\":\"mcp_oauth\",\"expires_at\":\"2026-11-21T12:00:00Z\",\"refresh\":{\"client_id\":\"test-client\",\"token_endpoint\":\"https://example.invalid/token\",\"resource\":\"https://example.invalid/mcp\",\"scope\":\"read\",\"token_endpoint_auth\":{\"type\":\"client_secret_post\"}}}}"));
      com.qoder.cloudagents.managed.VaultCredential response =
          async
              ? managed.async().vaults().credentials().create("vault_test", create).get()
              : managed.vaults().credentials().create("vault_test", create);
      RecordedRequest wire = request();
      assertEquals("POST", wire.getMethod());
      assertEquals("/api/v1/cloud/vaults/vault_test/credentials", wire.getPath());
      assertJson(
          "{\"auth\":{\"type\":\"mcp_oauth\",\"mcp_server_url\":\"https://example.invalid/mcp\",\"access_token\":\"test-access\",\"refresh\":{\"client_id\":\"test-client\",\"refresh_token\":\"test-refresh\",\"token_endpoint\":\"https://example.invalid/token\",\"resource\":\"https://example.invalid/mcp\",\"scope\":\"read\",\"token_endpoint_auth\":{\"type\":\"client_secret_post\",\"client_secret\":\"test-secret\"}}}}",
          wire.getBody().readUtf8());
      assertJson(
          "{\"id\":\"cred_test\",\"auth\":{\"type\":\"mcp_oauth\",\"expires_at\":\"2026-11-21T12:00:00Z\",\"refresh\":{\"client_id\":\"test-client\",\"token_endpoint\":\"https://example.invalid/token\",\"resource\":\"https://example.invalid/mcp\",\"scope\":\"read\",\"token_endpoint_auth\":{\"type\":\"client_secret_post\"}}}}",
          response.rawJson());
      assertEquals("2026-11-21T12:00:00Z", response.auth().expiresAt());
      assertEquals("test-client", response.auth().refresh().clientId());
      assertEquals("https://example.invalid/token", response.auth().refresh().tokenEndpoint());
      assertEquals("read", response.auth().refresh().scope());
      assertTrue(response.auth().field("access_token").isMissing());
      assertEquals("https://example.invalid/mcp", response.auth().refresh().resource());
      assertEquals("client_secret_post", response.auth().refresh().tokenEndpointAuth().type());
      assertTrue(response.auth().refresh().field("refresh_token").isMissing());
      assertTrue(response.auth().refresh().tokenEndpointAuth().field("client_secret").isMissing());
      assertFalse(create.toString().contains("test-secret"));
    }
  }
}
