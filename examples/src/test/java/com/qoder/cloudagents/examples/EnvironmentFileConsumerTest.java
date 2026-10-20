package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.errors.ApiStatusException;
import java.io.*;
import java.util.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class EnvironmentFileConsumerTest {
  final List<String> calls = new ArrayList<>();
  final Set<String> keys = new HashSet<>();

  Dispatcher dispatch(String url, boolean uploadFails, boolean cleanupFails) {
    return new Dispatcher() {
      public MockResponse dispatch(RecordedRequest r) {
        String path = r.getRequestUrl().encodedPath().substring("/api/v1/forward".length()),
            method = r.getMethod();
        calls.add(method + " " + path);
        if (!"Bearer example-token".equals(r.getHeader("Authorization"))) return error(401);
        if (!method.equals("GET")
            && (r.getHeader("Idempotency-Key") == null
                || !keys.add(r.getHeader("Idempotency-Key")))) return error(400);
        if (path.equals("/environments"))
          return method.equals("POST")
              ? ok("{\"id\":\"env_new\"}")
              : ok("{\"data\":[],\"has_more\":false,\"next_page\":null}");
        if (path.equals("/environments/env_new"))
          return method.equals("DELETE")
              ? ok("{\"id\":\"env_new\",\"deleted\":true}")
              : ok("{\"id\":\"env_new\"}");
        if (path.equals("/files") && method.equals("POST")) {
          if (uploadFails) return error(403);
          String body = r.getBody().readUtf8();
          if (!body.contains("QCA Java SDK\n文件 round trip\n") || !body.contains("session_resource"))
            return error(400);
          return ok("{\"id\":\"file_new\",\"filename\":\"example.txt\"}");
        }
        if (path.equals("/files"))
          return ok("{\"data\":[{\"id\":\"file_new\"}],\"has_more\":false,\"next_page\":null}");
        if (path.equals("/files/file_new"))
          return method.equals("DELETE")
              ? (cleanupFails ? error(409) : ok("{\"id\":\"file_new\",\"deleted\":true}"))
              : ok("{\"id\":\"file_new\",\"filename\":\"example.txt\"}");
        if (path.equals("/files/file_new/content"))
          return ok("{\"url\":" + JsonValue.of(url).toJson() + "}")
              .setHeader("x-request-id", "api-download");
        return error(404);
      }
    };
  }

  MockResponse ok(String s) {
    return new MockResponse().setBody(s);
  }

  MockResponse error(int s) {
    return ok("{}").setResponseCode(s);
  }

  ForwardClient client(MockWebServer api) {
    return ForwardClient.builder()
        .baseUrl(api.url("/api/v1/forward").toString())
        .pat("example-token")
        .maxRetries(0)
        .build();
  }

  @Test
  void installedJarRunsTextRoundTripAndCleansOnlyItsOwnResources() throws Exception {
    assertTrue(
        ForwardClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()
            .endsWith(".jar"));
    try (MockWebServer api = new MockWebServer();
        MockWebServer storage = new MockWebServer()) {
      storage.start();
      api.setDispatcher(dispatch(storage.url("/signed?token=secret").toString(), false, false));
      api.start();
      storage.enqueue(ok("QCA Java SDK\n文件 round trip\n"));
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient c = client(api);
          PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
        EnvironmentFileExample.run(c, out);
      }
      assertEquals(
          Arrays.asList(
              "POST /environments",
              "GET /environments/env_new",
              "POST /environments/env_new",
              "GET /environments",
              "POST /files",
              "GET /files/file_new",
              "GET /files",
              "GET /files/file_new/content",
              "DELETE /files/file_new",
              "DELETE /environments/env_new"),
          calls);
      assertEquals(5, keys.size());
      assertNull(storage.takeRequest().getHeader("Authorization"));
      String output = bytes.toString("UTF-8");
      assertTrue(output.contains("request_id=api-download"));
      assertTrue(output.contains("deleted_environment=env_new"));
      assertFalse(output.contains("secret"));
      assertFalse(output.contains("example-token"));
    }
  }

  @Test
  void mismatchRetainsCleanupFailureAndStillDeletesEnvironment() throws Exception {
    try (MockWebServer api = new MockWebServer();
        MockWebServer storage = new MockWebServer()) {
      storage.start();
      api.setDispatcher(dispatch(storage.url("/signed").toString(), false, true));
      api.start();
      storage.enqueue(ok("wrong"));
      try (ForwardClient c = client(api);
          PrintStream out = new PrintStream(new ByteArrayOutputStream())) {
        IllegalStateException e =
            assertThrows(IllegalStateException.class, () -> EnvironmentFileExample.run(c, out));
        assertEquals(1, e.getSuppressed().length);
        assertEquals(409, ((ApiStatusException) e.getSuppressed()[0]).statusCode());
      }
      assertEquals("DELETE /environments/env_new", calls.get(calls.size() - 1));
    }
  }

  @Test
  void failedUploadStillCleansEnvironmentAndDoesNotGuessFileId() throws Exception {
    try (MockWebServer api = new MockWebServer()) {
      api.setDispatcher(dispatch("https://storage.example/unused", true, false));
      api.start();
      try (ForwardClient c = client(api);
          PrintStream out = new PrintStream(new ByteArrayOutputStream())) {
        assertEquals(
            403,
            assertThrows(ApiStatusException.class, () -> EnvironmentFileExample.run(c, out))
                .statusCode());
      }
      assertEquals("DELETE /environments/env_new", calls.get(calls.size() - 1));
      assertFalse(calls.contains("DELETE /files/file_new"));
    }
  }
}
