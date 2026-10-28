package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.forward.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class ForwardCapabilitiesConsumerTest {
  private MockWebServer server;
  private ForwardClient client;

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

  private void respond(String value) {
    server.enqueue(new MockResponse().setBody(value));
  }

  private RecordedRequest request() throws Exception {
    RecordedRequest r = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  @Test
  void searchAndDecisionExamplesUseTheInstalledJar() throws Exception {
    respond("{\"data\":[{\"id\":\"file_test\"}],\"has_more\":false}");
    assertEquals(
        "file_test",
        ForwardCapabilitiesExample.searchFiles(client, "report", "idn_test").data().get(0).id());
    RecordedRequest search = request();
    assertEquals("/api/v1/forward/files/search?identity_id=idn_test", search.getPath());
    assertEquals("{\"name\":\"report\",\"limit\":5}", search.getBody().readUtf8());
    assertEquals("search-2026-08-31", search.getHeader("x-qoder-beta"));
    respond(
        "{\"id\":\"dec_test\",\"status\":\"completed\",\"answers\":[{\"type\":\"choice\",\"value\":true}]}");
    Decision decision = ForwardCapabilitiesExample.decide(client, "one-decision");
    assertTrue(decision.answers().get(0).value().asBoolean());
    RecordedRequest create = request();
    assertEquals("/api/v1/forward/decisions", create.getPath());
    assertEquals("one-decision", create.getHeader("Idempotency-Key"));
    assertTrue(
        JsonValue.parse(create.getBody().readUtf8())
            .get("questions")
            .asArray()
            .get(0)
            .get("choices")
            .asArray()
            .get(0)
            .get("value")
            .isBoolean());
  }

  @Test
  void dreamExampleAndAsyncLifecycleRemainSeparateFromTheInputStore() throws Exception {
    respond("{\"id\":\"drm_test\",\"status\":\"pending\",\"model\":{\"id\":\"auto\"}}");
    Dream dream = ForwardCapabilitiesExample.createDream(client, "memstore_input", "dream-key");
    assertEquals("auto", dream.model().id());
    assertEquals(
        "{\"inputs\":[{\"type\":\"memory_store\",\"memory_store_id\":\"memstore_input\"}]}",
        request().getBody().readUtf8());
    respond("{\"id\":\"drm_test\",\"status\":\"canceled\"}");
    assertEquals(
        "canceled", client.async().dreams().cancel(dream.id()).get(3, TimeUnit.SECONDS).status());
    assertEquals("/api/v1/forward/dreams/drm_test/cancel", request().getPath());
    respond(
        "{\"id\":\"drm_test\",\"status\":\"canceled\",\"archived_at\":\"2026-10-28T00:00:00Z\"}");
    assertNotNull(client.dreams().archive(dream.id()).archivedAt());
    assertEquals("/api/v1/forward/dreams/drm_test/archive", request().getPath());
    assertEquals(3, server.getRequestCount());
  }

  @Test
  void sessionProvenanceAndContractFieldsReachTheWireFromTheJar() throws Exception {
    Map<String, JsonValue> metadata = new LinkedHashMap<>();
    metadata.put("ticket", JsonValue.of("consumer"));
    SessionCreateParams params =
        SessionCreateParams.builder().identityId("i").templateId("t").metadata(metadata).build();
    respond("{\"id\":\"sess_test\"}");
    client.async().sessions().withResponse().create(params).get(3, TimeUnit.SECONDS);
    RecordedRequest created = request();
    JsonValue markers = JsonValue.parse(created.getBody().readUtf8()).get("metadata");
    assertEquals("java", markers.get("__qca_sdk_language").asString());
    assertEquals("true", markers.get("__qca_sdk_created").asString());
    assertEquals(
        created.getHeader("X-Qoder-Package-Version"), markers.get("__qca_sdk_version").asString());
    assertEquals(1, metadata.size());
    assertEquals(1, params.metadata().size());
    respond("{\"data\":[]}");
    client
        .sessions()
        .events()
        .send(
            "sess_test",
            SessionEventSendParams.builder()
                .events(
                    Collections.singletonList(
                        SessionEventParam.builder()
                            .type("user.define_outcome")
                            .description("Return done")
                            .rubric(
                                OutcomeRubric.builder()
                                    .type("text")
                                    .content("Reply is done")
                                    .build())
                            .build()))
                .build());
    JsonValue rubric =
        JsonValue.parse(request().getBody().readUtf8())
            .get("events")
            .asArray()
            .get(0)
            .get("rubric");
    assertEquals("Reply is done", rubric.get("content").asString());
    assertTrue(rubric.get("text").isMissing());
    respond("{\"auto_memory_store\":false}");
    assertFalse(
        client
            .identities()
            .update("i", IdentityUpdateParams.builder().autoMemoryStore(false).build())
            .autoMemoryStore());
    assertEquals("{\"auto_memory_store\":false}", request().getBody().readUtf8());
    respond("{\"resources\":[]}");
    assertTrue(
        client
            .schedules()
            .update(
                "schedule_test",
                ScheduleUpdateParams.builder().resources(Collections.emptyList()).build())
            .resources()
            .isEmpty());
    assertEquals("{\"resources\":[]}", request().getBody().readUtf8());
  }
}
