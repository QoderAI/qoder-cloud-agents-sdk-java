package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.errors.ApiStatusException;
import java.io.*;
import java.util.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class SessionConsumerTest {
  private final List<String> calls = new ArrayList<>();
  private final Set<String> writeKeys = new HashSet<>();
  private final List<String> auth = new ArrayList<>();

  private Dispatcher dispatcher(boolean updateFails, boolean archiveFails, boolean createFails) {
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest r) {
        String path = r.getRequestUrl().encodedPath().substring("/api/v1/forward".length());
        calls.add(r.getMethod() + " " + path);
        auth.add(r.getHeader("Authorization"));
        if (!"GET".equals(r.getMethod())) {
          String key = r.getHeader("Idempotency-Key");
          if (key == null || !writeKeys.add(key))
            return new MockResponse().setResponseCode(400).setBody("{}");
        }
        if (path.equals("/identities")) {
          if (!r.getBody().readUtf8().contains("sdk-java-session-"))
            return new MockResponse().setResponseCode(400).setBody("{}");
          return ok("{\"id\":\"idn_new\"}");
        }
        if (path.equals("/identities/idn_new/templates/tmpl_existing/memory_stores"))
          return ok(
              r.getMethod().equals("GET")
                  ? "{\"data\":[{\"memory_store_id\":\"memstore_existing\"}],\"has_more\":false}"
                  : "{\"memory_store_id\":\"memstore_existing\"}");
        if (path.endsWith("/memory_stores/memstore_existing"))
          return ok(
              "{\"id\":\"memstore_existing\",\"deleted\":true,\"type\":\"memory_store_binding_deleted\"}");
        if (path.equals("/sessions")) {
          if (createFails) return new MockResponse().setResponseCode(403).setBody("{}");
          return ok("{\"id\":\"sess_new\",\"status\":\"idle\"}");
        }
        if (path.equals("/sessions/sess_new")) {
          if (updateFails && r.getMethod().equals("POST"))
            return new MockResponse().setResponseCode(400).setBody("{}");
          return ok("{\"id\":\"sess_new\",\"status\":\"idle\"}")
              .setHeader("x-request-id", "req-session");
        }
        if (path.equals("/sessions/sess_new/events") || path.equals("/sessions/sess_new/threads"))
          return ok("{\"data\":[],\"has_more\":false}");
        if (path.equals("/sessions/sess_new/archive"))
          return archiveFails
              ? new MockResponse().setResponseCode(409).setBody("{}")
              : ok("{\"id\":\"sess_new\",\"archived_at\":\"2026-10-17T00:00:00Z\"}");
        if (path.equals("/identities/idn_new/clear")) return ok("{\"status\":\"completed\"}");
        if (path.equals("/identities/idn_new") && r.getMethod().equals("DELETE"))
          return ok("{\"id\":\"idn_new\",\"deleted\":true}");
        return new MockResponse().setResponseCode(404).setBody("{}");
      }
    };
  }

  private MockResponse ok(String body) {
    return new MockResponse().setBody(body);
  }

  private ForwardClient client(MockWebServer server) {
    return ForwardClient.builder()
        .baseUrl(server.url("/api/v1/forward").toString())
        .pat("test-token")
        .maxRetries(0)
        .build();
  }

  @Test
  void jarConsumerCreatesAndCleansOwnSessionAndMountOnly() throws Exception {
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
        SessionLifecycleExample.run(c, "tmpl_existing", "memstore_existing", out);
      }
      assertEquals(
          Arrays.asList(
              "POST /identities",
              "POST /identities/idn_new/templates/tmpl_existing/memory_stores",
              "GET /identities/idn_new/templates/tmpl_existing/memory_stores",
              "POST /sessions",
              "GET /sessions/sess_new",
              "POST /sessions/sess_new",
              "GET /sessions/sess_new/events",
              "GET /sessions/sess_new/threads",
              "POST /sessions/sess_new/archive",
              "DELETE /identities/idn_new/templates/tmpl_existing/memory_stores/memstore_existing",
              "POST /identities/idn_new/clear",
              "DELETE /identities/idn_new"),
          calls);
      assertEquals(8, writeKeys.size());
      assertTrue(auth.stream().allMatch("Bearer test-token"::equals));
      String output = bytes.toString("UTF-8");
      assertTrue(output.contains("archived_session=sess_new"));
      assertTrue(output.contains("deleted_identity=idn_new"));
      assertTrue(output.contains("request_id=req-session"));
      assertFalse(output.contains("test-token"));
    }
  }

  @Test
  void businessFailureKeepsCleanupFailureAndStillDetachesMount() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.setDispatcher(dispatcher(true, true, false));
      server.start();
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient c = client(server);
          PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
        ApiStatusException error =
            assertThrows(
                ApiStatusException.class,
                () -> SessionLifecycleExample.run(c, "tmpl_existing", "memstore_existing", out));
        assertEquals(400, error.statusCode());
        assertEquals(1, error.getSuppressed().length);
        assertEquals(409, ((ApiStatusException) error.getSuppressed()[0]).statusCode());
      }
      assertTrue(
          calls.contains(
              "DELETE"
                  + " /identities/idn_new/templates/tmpl_existing/memory_stores/memstore_existing"));
      assertFalse(calls.contains("DELETE /identities/idn_new"));
      assertFalse(calls.contains("POST /identities/idn_new/clear"));
      assertTrue(bytes.toString("UTF-8").contains("cleanup_failed_identity=idn_new"));
    }
  }

  @Test
  void sessionCreationFailureStillClearsIsolatedIdentity() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.setDispatcher(dispatcher(false, false, true));
      server.start();
      try (ForwardClient c = client(server);
          PrintStream out = new PrintStream(new ByteArrayOutputStream())) {
        ApiStatusException error =
            assertThrows(
                ApiStatusException.class,
                () -> SessionLifecycleExample.run(c, "tmpl_existing", null, out));
        assertEquals(403, error.statusCode());
        assertEquals(0, error.getSuppressed().length);
      }
      assertEquals(
          Arrays.asList(
              "POST /identities",
              "POST /sessions",
              "POST /identities/idn_new/clear",
              "DELETE /identities/idn_new"),
          calls);
    }
  }
}
