package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.errors.ApiStatusException;
import java.io.*;
import java.util.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class MemoryStoreConsumerTest {
  private final List<String> calls = new ArrayList<>();
  private final Set<String> keys = new HashSet<>();

  private Dispatcher dispatcher(boolean updateFails, boolean cleanupFails, boolean createFails) {
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest r) {
        String path = r.getRequestUrl().encodedPath().substring("/api/v1/forward".length());
        String method = r.getMethod();
        calls.add(method + " " + path);
        if (!"Bearer test-token".equals(r.getHeader("Authorization"))) return error(401);
        if (!"GET".equals(method)) {
          String key = r.getHeader("Idempotency-Key");
          if (key == null || !keys.add(key)) return error(400);
        }
        if (path.equals("/memory_stores") && method.equals("POST")) {
          String body = r.getBody().readUtf8();
          if (!body.contains("sdk-java-memory-") || body.contains("\"mount\"")) return error(400);
          return createFails ? error(403) : ok("{\"id\":\"memstore_new\",\"status\":\"active\"}");
        }
        if (path.equals("/memory_stores/memstore_new")) {
          if (method.equals("DELETE"))
            return cleanupFails ? error(409) : ok("{\"id\":\"memstore_new\",\"deleted\":true}");
          return ok("{\"id\":\"memstore_new\",\"status\":\"active\"}")
              .setHeader("x-request-id", "req-store");
        }
        if (path.equals("/memory_stores/memstore_new/memories")) {
          if (method.equals("GET")) {
            if (!"sdk-example/".equals(r.getRequestUrl().queryParameter("path_prefix")))
              return error(400);
            return ok("{\"data\":[{\"id\":\"mem_new\"}],\"has_more\":false}");
          }
          if (!r.getBody().readUtf8().contains("\"path\":\"sdk-example/note.md\""))
            return error(400);
          return ok("{\"id\":\"mem_new\",\"content_sha256\":\"initial-hash\"}");
        }
        if (path.equals("/memory_stores/memstore_new/memories/mem_new")) {
          if (method.equals("DELETE")) return ok("{\"id\":\"mem_new\",\"deleted\":true}");
          if (method.equals("POST")) {
            if (!r.getBody().readUtf8().contains("\"content_sha256\":\"current-hash\""))
              return error(400);
            if (updateFails) return error(409);
          }
          return ok("{\"id\":\"mem_new\",\"content_sha256\":\"current-hash\"}");
        }
        if (path.equals("/memory_stores/memstore_new/memory_versions")) {
          if (!"mem_new".equals(r.getRequestUrl().queryParameter("memory_id"))) return error(400);
          return ok("{\"data\":[{\"id\":\"memver_new\"}],\"has_more\":false}");
        }
        if (path.equals("/memory_stores/memstore_new/memory_versions/memver_new"))
          return ok(
              "{\"id\":\"memver_new\",\"operation\":\"updated\",\"redacted\":false,\"content\":\"private"
                  + " example\"}");
        return error(404);
      }
    };
  }

  private MockResponse ok(String body) {
    return new MockResponse().setBody(body);
  }

  private MockResponse error(int status) {
    return ok("{}").setResponseCode(status);
  }

  private ForwardClient client(MockWebServer server) {
    return ForwardClient.builder()
        .baseUrl(server.url("/api/v1/forward").toString())
        .pat("test-token")
        .maxRetries(0)
        .build();
  }

  @Test
  void installedJarConsumerCleansOnlyItsOwnStoreAndPreservesConcurrencyHash() throws Exception {
    assertTrue(
        ForwardClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()
            .endsWith(".jar"));
    try (MockWebServer server = new MockWebServer()) {
      server.setDispatcher(dispatcher(false, false, false));
      server.start();
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient c = client(server);
          PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
        MemoryStoreLifecycleExample.run(c, out);
      }
      assertEquals(
          Arrays.asList(
              "POST /memory_stores",
              "GET /memory_stores/memstore_new",
              "POST /memory_stores/memstore_new",
              "POST /memory_stores/memstore_new/memories",
              "GET /memory_stores/memstore_new/memories/mem_new",
              "POST /memory_stores/memstore_new/memories/mem_new",
              "GET /memory_stores/memstore_new/memories",
              "GET /memory_stores/memstore_new/memory_versions",
              "GET /memory_stores/memstore_new/memory_versions/memver_new",
              "DELETE /memory_stores/memstore_new/memories/mem_new",
              "DELETE /memory_stores/memstore_new"),
          calls);
      assertEquals(6, keys.size());
      String output = bytes.toString("UTF-8");
      assertTrue(output.contains("deleted_memory_store=memstore_new"));
      assertTrue(output.contains("request_id=req-store"));
      assertFalse(output.contains("test-token"));
      assertFalse(output.contains("private example"));
    }
  }

  @Test
  void conflictStillCleansStoreAndRetainsCleanupFailure() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.setDispatcher(dispatcher(true, true, false));
      server.start();
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient c = client(server);
          PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
        ApiStatusException e =
            assertThrows(ApiStatusException.class, () -> MemoryStoreLifecycleExample.run(c, out));
        assertEquals(409, e.statusCode());
        assertEquals(1, e.getSuppressed().length);
        assertEquals(409, ((ApiStatusException) e.getSuppressed()[0]).statusCode());
      }
      assertEquals("DELETE /memory_stores/memstore_new", calls.get(calls.size() - 1));
      assertFalse(calls.stream().anyMatch(s -> s.endsWith("/archive") || s.endsWith("/redact")));
      assertTrue(bytes.toString("UTF-8").contains("cleanup_failed_memory_store=memstore_new"));
    }
  }

  @Test
  void failedCreateDoesNotDeleteAnythingAndKeepsRecoveryNameAndKey() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.setDispatcher(dispatcher(false, false, true));
      server.start();
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient c = client(server);
          PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
        ApiStatusException e =
            assertThrows(ApiStatusException.class, () -> MemoryStoreLifecycleExample.run(c, out));
        assertEquals(403, e.statusCode());
        assertEquals(0, e.getSuppressed().length);
      }
      assertEquals(Collections.singletonList("POST /memory_stores"), calls);
      assertTrue(bytes.toString("UTF-8").contains("test_store_name=sdk-java-memory-"));
      assertTrue(bytes.toString("UTF-8").contains("create_idempotency_key="));
    }
  }
}
