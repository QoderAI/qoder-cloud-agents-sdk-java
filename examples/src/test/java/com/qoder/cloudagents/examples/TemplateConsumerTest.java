package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.errors.ApiStatusException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(15)
class TemplateConsumerTest {
  private String template(String id) {
    return "{\"id\":\"" + id + "\",\"name\":\"Test\",\"status\":\"active\"}";
  }

  @Test
  void lifecycleUsesPackagedSdkAndArchivesBothTemplates() throws Exception {
    assertTrue(
        ForwardClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()
            .endsWith(".jar"));
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      for (String id : Arrays.asList("tmpl_a", "tmpl_a", "tmpl_b", "tmpl_b", "tmpl_b", "tmpl_a"))
        server.enqueue(new MockResponse().setBody(template(id)));
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient client =
              ForwardClient.builder()
                  .baseUrl(server.url("/api/v1/forward").toString())
                  .pat("test-token")
                  .build();
          PrintStream output = new PrintStream(bytes, true, "UTF-8")) {
        TemplateLifecycleExample.run(client, "env_test", "auto", output);
      }
      List<String> paths = new ArrayList<>();
      Set<String> keys = new HashSet<>();
      for (int i = 0; i < 6; i++) {
        RecordedRequest request = server.takeRequest();
        paths.add(request.getPath());
        if (!"GET".equals(request.getMethod()))
          assertTrue(keys.add(request.getHeader("Idempotency-Key")));
      }
      assertEquals(
          Arrays.asList(
              "/api/v1/forward/templates",
              "/api/v1/forward/templates/tmpl_a",
              "/api/v1/forward/templates/tmpl_a/clone",
              "/api/v1/forward/templates/tmpl_b",
              "/api/v1/forward/templates/tmpl_b/archive",
              "/api/v1/forward/templates/tmpl_a/archive"),
          paths);
      String text = bytes.toString("UTF-8");
      assertTrue(text.contains("archived=tmpl_a"));
      assertTrue(text.contains("archived=tmpl_b"));
      assertFalse(text.contains("test-token"));
    }
  }

  @Test
  void cleanupFailureIsRetainedAlongsideOriginalFailure() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(new MockResponse().setBody(template("tmpl_a")));
      server.enqueue(
          new MockResponse()
              .setResponseCode(400)
              .setBody("{\"error\":{\"message\":\"update failed\"}}"));
      server.enqueue(
          new MockResponse()
              .setResponseCode(409)
              .setBody("{\"error\":{\"message\":\"archive failed\"}}"));
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient client =
              ForwardClient.builder()
                  .baseUrl(server.url("/api/v1/forward").toString())
                  .pat("test-token")
                  .maxRetries(0)
                  .build();
          PrintStream output = new PrintStream(bytes, true, "UTF-8")) {
        ApiStatusException failure =
            assertThrows(
                ApiStatusException.class,
                () -> TemplateLifecycleExample.run(client, "env_test", "auto", output));
        assertEquals(400, failure.statusCode());
        assertEquals(1, failure.getSuppressed().length);
        assertEquals(409, ((ApiStatusException) failure.getSuppressed()[0]).statusCode());
      }
      assertEquals(3, server.getRequestCount());
      assertTrue(bytes.toString("UTF-8").contains("archive_failed=tmpl_a"));
    }
  }

  @Test
  void archiveFailureDoesNotPreventOtherCleanup() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      for (String id : Arrays.asList("tmpl_a", "tmpl_a", "tmpl_b", "tmpl_b"))
        server.enqueue(new MockResponse().setBody(template(id)));
      server.enqueue(new MockResponse().setResponseCode(409).setBody("{}"));
      server.enqueue(new MockResponse().setBody(template("tmpl_a")));
      try (ForwardClient client =
              ForwardClient.builder()
                  .baseUrl(server.url("/api/v1/forward").toString())
                  .pat("test-token")
                  .build();
          PrintStream output = new PrintStream(new ByteArrayOutputStream())) {
        assertThrows(
            ApiStatusException.class,
            () -> TemplateLifecycleExample.run(client, "env_test", "auto", output));
      }
      assertEquals(6, server.getRequestCount());
      for (int i = 0; i < 5; i++) server.takeRequest();
      assertEquals("/api/v1/forward/templates/tmpl_a/archive", server.takeRequest().getPath());
    }
  }
}
