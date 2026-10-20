package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class MemoryStoresTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private MockWebServer server;
  private ForwardClient client;
  private static final String STORE = "memstore/a ?#%",
      MEMORY = "mem/a ?#%",
      VERSION = "memver/a ?#%";

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
    RecordedRequest r = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  static Stream<Arguments> routes() {
    String[][] rows = {
      {"storesList", "GET", "/memory_stores", "", "{\"data\":[],\"has_more\":false}"},
      {
        "storeCreate",
        "POST",
        "/memory_stores",
        "{\"name\":\"Knowledge\",\"metadata\":{\"team\":\"sdk\"},\"mount\":{\"identity_id\":\"idn_1\",\"template_id\":\"tmpl_1\"}}",
        "{\"id\":\"memstore_1\",\"system_managed\":false}"
      },
      {"storeGet", "GET", "/memory_stores/STORE", "", "{\"id\":\"memstore_1\"}"},
      {
        "storeUpdate",
        "POST",
        "/memory_stores/STORE",
        "{\"description\":\"Updated\",\"metadata\":{}}",
        "{\"id\":\"memstore_1\"}"
      },
      {
        "storeDelete",
        "DELETE",
        "/memory_stores/STORE",
        "",
        "{\"id\":\"memstore_1\",\"type\":\"memory_store_deleted\",\"deleted\":true}"
      },
      {
        "storeArchive",
        "POST",
        "/memory_stores/STORE/archive",
        "",
        "{\"id\":\"memstore_1\",\"status\":\"archived\"}"
      },
      {
        "memoriesList",
        "GET",
        "/memory_stores/STORE/memories",
        "",
        "{\"data\":[],\"has_more\":false}"
      },
      {
        "memoryCreate",
        "POST",
        "/memory_stores/STORE/memories",
        "{\"path\":\"notes/说明.md\",\"content\":\"说明\\n正文\\t✓\"}",
        "{\"id\":\"mem_1\"}"
      },
      {
        "memoryGet",
        "GET",
        "/memory_stores/STORE/memories/MEMORY",
        "",
        "{\"id\":\"mem_1\",\"content\":\"text\"}"
      },
      {
        "memoryUpdate",
        "POST",
        "/memory_stores/STORE/memories/MEMORY",
        "{\"content\":\"\",\"content_sha256\":\"expected-hash\",\"metadata\":{}}",
        "{\"id\":\"mem_1\",\"content\":\"\"}"
      },
      {
        "memoryDelete",
        "DELETE",
        "/memory_stores/STORE/memories/MEMORY",
        "",
        "{\"id\":\"mem_1\",\"type\":\"memory_deleted\",\"deleted\":true}"
      },
      {
        "versionsList",
        "GET",
        "/memory_stores/STORE/memory_versions",
        "",
        "{\"data\":[],\"has_more\":false}"
      },
      {
        "versionGet",
        "GET",
        "/memory_stores/STORE/memory_versions/VERSION",
        "",
        "{\"id\":\"memver_1\",\"content\":\"old\"}"
      },
      {
        "versionRedact",
        "POST",
        "/memory_stores/STORE/memory_versions/VERSION/redact",
        "",
        "{\"id\":\"memver_1\",\"redacted\":true,\"content\":null}"
      }
    };
    return Arrays.stream(rows)
        .flatMap(
            r ->
                Stream.of(false, true)
                    .map(async -> Arguments.of(r[0], r[1], r[2], r[3], r[4], async)));
  }

  @ParameterizedTest
  @MethodSource("routes")
  void syncAndAsyncContracts(
      String operation, String method, String path, String body, String responseBody, boolean async)
      throws Exception {
    server.enqueue(json(responseBody).setHeader("x-request-id", "req-" + operation));
    ApiResponse<? extends JsonModel> response =
        invoke(
            operation,
            async,
            RequestOptions.builder()
                .idempotencyKey("logical-write")
                .header("X-Test", "contract")
                .build());
    assertEquals(200, response.statusCode());
    assertEquals("req-" + operation, response.requestId());
    assertEquals(response.requestId(), response.data().requestId());
    assertEquals(JSON.readTree(responseBody), JSON.readTree(response.data().rawJson()));
    RecordedRequest r = request();
    assertEquals(method, r.getMethod());
    assertEquals(
        "/api/v1/forward"
            + path.replace("STORE", "memstore%2Fa%20%3F%23%25")
                .replace("MEMORY", "mem%2Fa%20%3F%23%25")
                .replace("VERSION", "memver%2Fa%20%3F%23%25"),
        r.getPath());
    assertEquals("Bearer test-pat", r.getHeader("Authorization"));
    assertEquals("logical-write", r.getHeader("Idempotency-Key"));
    assertEquals("contract", r.getHeader("X-Test"));
    String actual = r.getBody().readUtf8();
    if (body.isEmpty()) assertEquals("", actual);
    else assertEquals(JSON.readTree(body), JSON.readTree(actual));
    assertEquals(1, server.getRequestCount());
  }

  private ApiResponse<? extends JsonModel> invoke(String op, boolean async, RequestOptions o)
      throws Exception {
    MemoryStores.WithResponse s = client.memoryStores().withResponse();
    AsyncMemoryStores.WithResponse a = client.async().memoryStores().withResponse();
    MemoryStoreMemories.WithResponse m = client.memoryStores().memories().withResponse();
    AsyncMemoryStoreMemories.WithResponse am =
        client.async().memoryStores().memories().withResponse();
    MemoryStoreMemoryVersions.WithResponse v =
        client.memoryStores().memoryVersions().withResponse();
    AsyncMemoryStoreMemoryVersions.WithResponse av =
        client.async().memoryStores().memoryVersions().withResponse();
    switch (op) {
      case "storesList":
        MemoryStoreListParams sl = MemoryStoreListParams.builder().build();
        return async ? a.list(sl, o).get() : s.list(sl, o);
      case "storeCreate":
        MemoryStoreCreateParams sc =
            MemoryStoreCreateParams.builder()
                .name("Knowledge")
                .metadata(Collections.singletonMap("team", "sdk"))
                .mount(
                    MemoryStoreCreateMount.builder()
                        .identityId("idn_1")
                        .templateId("tmpl_1")
                        .build())
                .build();
        return async ? a.create(sc, o).get() : s.create(sc, o);
      case "storeGet":
        return async ? a.retrieve(STORE, o).get() : s.retrieve(STORE, o);
      case "storeUpdate":
        MemoryStoreUpdateParams su =
            MemoryStoreUpdateParams.builder()
                .description("Updated")
                .metadata(Collections.emptyMap())
                .build();
        return async ? a.update(STORE, su, o).get() : s.update(STORE, su, o);
      case "storeDelete":
        return async ? a.delete(STORE, o).get() : s.delete(STORE, o);
      case "storeArchive":
        return async ? a.archive(STORE, o).get() : s.archive(STORE, o);
      case "memoriesList":
        MemoryListParams ml = MemoryListParams.builder().build();
        return async ? am.list(STORE, ml, o).get() : m.list(STORE, ml, o);
      case "memoryCreate":
        MemoryCreateParams mc =
            MemoryCreateParams.builder().path("notes/说明.md").content("说明\n正文\t✓").build();
        return async ? am.create(STORE, mc, o).get() : m.create(STORE, mc, o);
      case "memoryGet":
        return async ? am.retrieve(STORE, MEMORY, o).get() : m.retrieve(STORE, MEMORY, o);
      case "memoryUpdate":
        MemoryUpdateParams mu =
            MemoryUpdateParams.builder()
                .content("")
                .contentSha256("expected-hash")
                .metadata(Collections.emptyMap())
                .build();
        return async ? am.update(STORE, MEMORY, mu, o).get() : m.update(STORE, MEMORY, mu, o);
      case "memoryDelete":
        return async ? am.delete(STORE, MEMORY, o).get() : m.delete(STORE, MEMORY, o);
      case "versionsList":
        MemoryVersionListParams vl = MemoryVersionListParams.builder().build();
        return async ? av.list(STORE, vl, o).get() : v.list(STORE, vl, o);
      case "versionGet":
        return async ? av.retrieve(STORE, VERSION, o).get() : v.retrieve(STORE, VERSION, o);
      case "versionRedact":
        return async ? av.redact(STORE, VERSION, o).get() : v.redact(STORE, VERSION, o);
      default:
        throw new AssertionError(op);
    }
  }

  @Test
  void createRetryKeepsIdempotencyHeaderAndBody() throws Exception {
    server.enqueue(json("{}").setResponseCode(503).setHeader("retry-after-ms", "1"));
    server.enqueue(json("{\"id\":\"memstore_1\"}"));
    assertEquals(
        "memstore_1",
        client
            .memoryStores()
            .create(
                MemoryStoreCreateParams.builder().name("my-store").build(),
                RequestOptions.builder().idempotencyKey("create-once").maxRetries(1).build())
            .id());
    RecordedRequest first = request(), second = request();
    assertEquals("create-once", first.getHeader("Idempotency-Key"));
    assertEquals(first.getHeader("Idempotency-Key"), second.getHeader("Idempotency-Key"));
    assertEquals(first.getBody().readUtf8(), second.getBody().readUtf8());
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void missingKeyPreservesServerErrorAndDefaultHeaderIsSupported() throws Exception {
    server.enqueue(
        json("{\"error\":{\"message\":\"Idempotency-Key"
                + " required\",\"type\":\"invalid_request_error\"}}")
            .setResponseCode(400)
            .setHeader("x-request-id", "req-no-key"));
    ApiStatusException e =
        assertThrows(
            ApiStatusException.class,
            () ->
                client
                    .memoryStores()
                    .create(MemoryStoreCreateParams.builder().name("new-store").build()));
    assertEquals(400, e.statusCode());
    assertEquals("req-no-key", e.requestId());
    assertNull(request().getHeader("Idempotency-Key"));
    server.enqueue(json("{\"id\":\"memstore_1\"}"));
    try (ForwardClient defaults =
        ForwardClient.builder()
            .baseUrl(server.url("/api/v1/forward").toString())
            .pat("test-pat")
            .defaultHeader("Idempotency-Key", "default-key")
            .build()) {
      assertEquals(
          "memstore_1",
          defaults
              .memoryStores()
              .create(MemoryStoreCreateParams.builder().name("new-store").build())
              .id());
    }
    RecordedRequest r = request();
    assertEquals("default-key", r.getHeader("Idempotency-Key"));
    assertFalse(r.getBody().readUtf8().contains("idempotency"));
  }

  @Test
  void staleHashConflictNeverRetriesOrOverwrites() throws Exception {
    server.enqueue(
        json("{\"error\":{\"type\":\"conflict_error\",\"message\":\"content hash mismatch\"}}")
            .setResponseCode(409)
            .setHeader("x-should-retry", "true"));
    ApiStatusException e =
        assertThrows(
            ApiStatusException.class,
            () ->
                client
                    .memoryStores()
                    .memories()
                    .update(
                        "memstore_1",
                        "mem_1",
                        MemoryUpdateParams.builder()
                            .content("new")
                            .contentSha256("old-hash")
                            .build(),
                        RequestOptions.builder()
                            .maxRetries(3)
                            .idempotencyKey("update-once")
                            .build()));
    assertEquals(409, e.statusCode());
    assertEquals(
        "{\"content\":\"new\",\"content_sha256\":\"old-hash\"}", request().getBody().readUtf8());
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void metadataIsAnImmutableReplacementWithExplicitOmissionNullAndEmpty() throws Exception {
    Map<String, String> metadata = new LinkedHashMap<>();
    metadata.put("team", "first");
    MemoryStoreUpdateParams snapshot = MemoryStoreUpdateParams.builder().metadata(metadata).build();
    metadata.put("team", "later");
    assertEquals("first", snapshot.metadata().get("team"));
    assertThrows(
        UnsupportedOperationException.class, () -> snapshot.metadata().put("other", "value"));
    assertTrue(MemoryStoreUpdateParams.builder().build().field("metadata").isMissing());
    assertTrue(MemoryStoreUpdateParams.builder().metadata(null).build().field("metadata").isNull());
    assertTrue(
        MemoryStoreUpdateParams.builder()
            .metadata(metadata)
            .omit("metadata")
            .build()
            .field("metadata")
            .isMissing());
    server.enqueue(json("{\"id\":\"mem_1\",\"content\":\"\",\"metadata\":{}}"));
    Memory value =
        client
            .async()
            .memoryStores()
            .memories()
            .update(
                "memstore_1",
                "mem_1",
                MemoryUpdateParams.builder().content("").metadata(Collections.emptyMap()).build())
            .get();
    assertEquals("", value.content());
    assertEquals("{\"content\":\"\",\"metadata\":{}}", request().getBody().readUtf8());
  }

  @Test
  void responsesKeepCountsUnknownFieldsAndContentPresence() throws Exception {
    server.enqueue(
        json("{\"id\":\"memstore_1\",\"type\":\"memory_store\",\"name\":\"store\",\"description\":\"desc\","
                 + "\"status\":\"active\",\"entry_count\":12,\"total_size\":9007199254740993,"
                 + "\"metadata\":{\"created_by\":\"forward\",\"team\":\"sdk\"},\"system_managed\":false,"
                 + "\"identity_id\":null,\"created_at\":\"created\",\"updated_at\":\"updated\",\"archived_at\":null,"
                 + "\"binding_info\":{\"identity_template_count\":2,\"future_count\":3},\"future\":{\"value\":1}}")
            .setHeader("x-request-id", "req-store"));
    MemoryStore store = client.memoryStores().retrieve("memstore_1");
    assertEquals(9007199254740993L, store.totalSize());
    assertEquals(12L, store.entryCount());
    assertEquals(2L, store.bindingInfo().identityTemplateCount());
    assertEquals(3, store.bindingInfo().field("future_count").asLong());
    assertEquals("forward", store.metadata().get("created_by"));
    assertFalse(store.systemManaged());
    assertNull(store.identityId());
    assertTrue(store.field("archived_at").isNull());
    assertEquals("req-store", store.requestId());
    assertEquals(1, store.field("future").get("value").asLong());
    Memory absent = new Memory(JsonValue.parse("{\"id\":\"mem_1\"}"), null);
    Memory empty = new Memory(JsonValue.parse("{\"id\":\"mem_1\",\"content\":\"\"}"), null);
    MemoryVersion redacted =
        new MemoryVersion(
            JsonValue.parse(
                "{\"id\":\"memver_1\",\"content\":null,\"redacted\":true,\"operation\":\"future_operation\",\"content_size_bytes\":9007199254740993}"),
            null);
    assertNull(absent.content());
    assertTrue(absent.field("content").isMissing());
    assertEquals("", empty.content());
    assertFalse(empty.field("content").isMissing());
    assertNull(redacted.content());
    assertTrue(redacted.field("content").isNull());
    assertTrue(redacted.redacted());
    assertEquals("future_operation", redacted.operation());
    assertEquals(9007199254740993L, redacted.contentSizeBytes());
  }

  @Test
  void storesPageRetainsFalseAndDoesNotSort() throws Exception {
    server.enqueue(
        json(
            "{\"data\":[{\"id\":\"z\"},{\"id\":\"a\"}],\"has_more\":true,\"first_id\":\"z\",\"last_id\":\"a\"}"));
    server.enqueue(
        json(
            "{\"data\":[{\"id\":\"b\"}],\"has_more\":false,\"first_id\":\"b\",\"last_id\":\"b\"}"));
    MemoryStoreListResponse page =
        client
            .memoryStores()
            .list(
                MemoryStoreListParams.builder().systemManaged(false).limit(2).build(),
                RequestOptions.builder().header("X-Test", "pages").build());
    List<String> ids = new ArrayList<>();
    for (MemoryStore s : page) ids.add(s.id());
    assertEquals(Arrays.asList("z", "a", "b"), ids);
    RecordedRequest first = request(), second = request();
    assertEquals("false", first.getRequestUrl().queryParameter("system_managed"));
    assertEquals("false", second.getRequestUrl().queryParameter("system_managed"));
    assertEquals("2", second.getRequestUrl().queryParameter("limit"));
    assertEquals("a", second.getRequestUrl().queryParameter("after_id"));
    assertEquals("pages", second.getHeader("X-Test"));
  }

  @Test
  void memoryPrefixRemainsRawAcrossAsyncPages() throws Exception {
    server.enqueue(
        json(
            "{\"data\":[{\"id\":\"mem_1\",\"path\":\"a/bc.md\"}],\"has_more\":true,\"last_id\":\"mem_1\"}"));
    server.enqueue(json("{\"data\":[{\"id\":\"mem_2\"}],\"has_more\":false}"));
    MemoryListResponse first =
        client
            .async()
            .memoryStores()
            .memories()
            .list("memstore_1", MemoryListParams.builder().pathPrefix("a/b").build())
            .get();
    assertTrue(first.data().get(0).field("content").isMissing());
    MemoryListResponse last = first.getNextPageAsync().get();
    assertEquals("mem_2", last.data().get(0).id());
    assertThrows(PaginationException.class, last::getNextPage);
    assertEquals("a/b", request().getRequestUrl().queryParameter("path_prefix"));
    RecordedRequest next = request();
    assertEquals(
        "/api/v1/forward/memory_stores/memstore_1/memories", next.getRequestUrl().encodedPath());
    assertEquals("a/b", next.getRequestUrl().queryParameter("path_prefix"));
    assertEquals("mem_1", next.getRequestUrl().queryParameter("after_id"));
  }

  @Test
  void versionsAreStoreScopedAndBackwardPagesRetainMemoryFilter() throws Exception {
    server.enqueue(
        json(
            "{\"data\":[{\"id\":\"memver_2\"}],\"has_more\":true,\"first_id\":\"memver_2\",\"last_id\":\"memver_9\"}"));
    server.enqueue(json("{\"data\":[],\"has_more\":false}"));
    MemoryVersionListResponse p =
        client
            .memoryStores()
            .memoryVersions()
            .list(
                "memstore_1",
                MemoryVersionListParams.builder()
                    .memoryId("mem_1")
                    .beforeId("memver_3")
                    .limit(1)
                    .build());
    p.getNextPage();
    assertEquals("memver_3", request().getRequestUrl().queryParameter("before_id"));
    RecordedRequest next = request();
    assertEquals(
        "/api/v1/forward/memory_stores/memstore_1/memory_versions",
        next.getRequestUrl().encodedPath());
    assertEquals("memver_2", next.getRequestUrl().queryParameter("before_id"));
    assertEquals("mem_1", next.getRequestUrl().queryParameter("memory_id"));
    assertNull(next.getRequestUrl().queryParameter("after_id"));
  }

  @Test
  void redactIsExplicitAndConflictIsNotSuccess() throws Exception {
    server.enqueue(
        json("{\"id\":\"memver_1\",\"redacted\":true,\"redacted_at\":\"now\",\"content\":null}"));
    MemoryVersion v =
        client.async().memoryStores().memoryVersions().redact("memstore_1", "memver_1").get();
    assertTrue(v.redacted());
    assertEquals("now", v.redactedAt());
    assertNull(v.content());
    server.enqueue(
        json("{\"error\":{\"type\":\"conflict_error\",\"message\":\"Already redacted\"}}")
            .setResponseCode(409));
    ApiStatusException e =
        assertThrows(
            ApiStatusException.class,
            () -> client.memoryStores().memoryVersions().redact("memstore_1", "memver_1"));
    assertEquals(409, e.statusCode());
    assertEquals(2, server.getRequestCount());
    assertEquals("POST", request().getMethod());
    assertEquals("POST", request().getMethod());
  }

  @Test
  void memoryRequestsSupportTimeoutAndRealCancellation() throws Exception {
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    CompletableFuture<Memory> pending =
        client.async().memoryStores().memories().retrieve("memstore_1", "mem_1");
    request();
    assertTrue(pending.cancel(true));
    assertTrue(pending.isCancelled());
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    assertThrows(
        ApiTimeoutException.class,
        () ->
            client
                .memoryStores()
                .memoryVersions()
                .retrieve(
                    "memstore_1",
                    "memver_1",
                    RequestOptions.builder().totalTimeout(Duration.ofMillis(250)).build()));
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void buildersRequireContentButAllowEmptyText() {
    assertEquals("", MemoryCreateParams.builder().path("empty.md").content("").build().content());
    assertEquals("", MemoryUpdateParams.builder().content("").build().content());
    assertThrows(
        ConfigurationException.class, () -> MemoryCreateParams.builder().path("x").build());
    assertThrows(
        ConfigurationException.class, () -> MemoryUpdateParams.builder().content(null).build());
    assertThrows(
        ConfigurationException.class, () -> MemoryCreateParams.builder().content("x").build());
    assertThrows(ConfigurationException.class, () -> MemoryStoreCreateParams.builder().build());
    assertThrows(
        ConfigurationException.class,
        () -> MemoryStoreCreateMount.builder().identityId("idn_1").build());
    assertThrows(
        ConfigurationException.class,
        () -> MemoryListParams.builder().afterId("a").beforeId("b").build());
    assertThrows(ConfigurationException.class, () -> MemoryVersionListParams.builder().limit(101));
    assertThrows(ConfigurationException.class, () -> MemoryStoreListParams.builder().limit(0));
    assertThrows(ConfigurationException.class, () -> client.memoryStores().create(null));
    assertEquals(0, server.getRequestCount());
  }
}
