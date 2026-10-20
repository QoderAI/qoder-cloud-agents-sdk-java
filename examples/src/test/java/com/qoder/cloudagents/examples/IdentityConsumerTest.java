package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.errors.ApiStatusException;
import java.io.*;
import java.util.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class IdentityConsumerTest {
  @Test
  void exampleUsesJarAndCleansOnlyItsOwnIdentity() throws Exception {
    assertTrue(
        ForwardClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()
            .endsWith(".jar"));
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(new MockResponse().setBody("{\"id\":\"idn_new\"}"));
      server.enqueue(new MockResponse().setBody("{\"id\":\"config_new\"}"));
      server.enqueue(
          new MockResponse()
              .setBody("{\"effective_hash\":\"hash\"}")
              .setHeader("x-request-id", "req-effective"));
      server.enqueue(
          new MockResponse().setBody("{\"identity_id\":\"idn_new\",\"status\":\"completed\"}"));
      server.enqueue(new MockResponse().setBody("{\"id\":\"idn_new\",\"deleted\":true}"));
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient client =
              ForwardClient.builder()
                  .baseUrl(server.url("/api/v1/forward").toString())
                  .pat("test-token")
                  .build();
          PrintStream output = new PrintStream(bytes, true, "UTF-8")) {
        IdentityConfigExample.run(client, "tmpl_existing", output);
      }
      List<String> paths = new ArrayList<>();
      Set<String> keys = new HashSet<>();
      for (int i = 0; i < 5; i++) {
        RecordedRequest sent = server.takeRequest();
        paths.add(sent.getMethod() + " " + sent.getPath());
        if (!"GET".equals(sent.getMethod()))
          assertTrue(keys.add(sent.getHeader("Idempotency-Key")));
      }
      assertEquals(
          Arrays.asList(
              "POST /api/v1/forward/identities",
              "POST /api/v1/forward/identities/idn_new/templates/tmpl_existing/config",
              "GET /api/v1/forward/identities/idn_new/templates/tmpl_existing/effective",
              "POST /api/v1/forward/identities/idn_new/clear",
              "DELETE /api/v1/forward/identities/idn_new"),
          paths);
      String text = bytes.toString("UTF-8");
      assertTrue(text.contains("deleted_identity=idn_new"));
      assertTrue(text.contains("request_id=req-effective"));
      assertFalse(text.contains("test-token"));
    }
  }

  @Test
  void clearFailureKeepsOriginalErrorAndLeavesIdentityForRecovery() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(new MockResponse().setBody("{\"id\":\"idn_new\"}"));
      server.enqueue(new MockResponse().setResponseCode(400).setBody("{}"));
      server.enqueue(new MockResponse().setResponseCode(409).setBody("{}"));
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
                () -> IdentityConfigExample.run(client, "tmpl_existing", output));
        assertEquals(400, failure.statusCode());
        assertEquals(1, failure.getSuppressed().length);
        assertEquals(409, ((ApiStatusException) failure.getSuppressed()[0]).statusCode());
      }
      assertEquals(3, server.getRequestCount());
      assertTrue(bytes.toString("UTF-8").contains("cleanup_failed_identity=idn_new"));
      assertFalse(bytes.toString("UTF-8").contains("deleted_identity="));
    }
  }

  @Test
  void incompleteClearDoesNotDeleteTheIdentity() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(new MockResponse().setBody("{\"id\":\"idn_new\"}"));
      server.enqueue(new MockResponse().setBody("{\"id\":\"config_new\"}"));
      server.enqueue(new MockResponse().setBody("{\"effective_hash\":\"hash\"}"));
      server.enqueue(new MockResponse().setBody("{\"status\":\"pending\"}"));
      try (ForwardClient client =
              ForwardClient.builder()
                  .baseUrl(server.url("/api/v1/forward").toString())
                  .pat("test-token")
                  .build();
          PrintStream output = new PrintStream(new ByteArrayOutputStream())) {
        assertThrows(
            IllegalStateException.class,
            () -> IdentityConfigExample.run(client, "tmpl_existing", output));
      }
      assertEquals(4, server.getRequestCount());
    }
  }
}
