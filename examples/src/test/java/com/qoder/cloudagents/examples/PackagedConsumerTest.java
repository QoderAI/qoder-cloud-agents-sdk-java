package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(15)
class PackagedConsumerTest {
  @Test
  void examplesRunAgainstInstalledJarAndPreserveMetadata() throws Exception {
    String sdkLocation =
        ForwardClient.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    assertTrue(
        sdkLocation.endsWith(".jar"),
        "Consumer must load SDK JAR, not target/classes: " + sdkLocation);
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(
          new MockResponse()
              .setBody(
                  "{\"data\":[{\"id\":\"auto\",\"display_name\":\"Auto\"}],\"has_more\":false}"));
      server.enqueue(
          new MockResponse()
              .setHeader("x-request-id", "req-list")
              .setBody(
                  "{\"data\":[{\"id\":\"tmpl_test\",\"name\":\"Example\"}],\"has_more\":false}"));
      server.enqueue(
          new MockResponse()
              .setHeader("x-request-id", "req-selected")
              .setBody("{\"id\":\"tmpl_test\",\"name\":\"Example\"}"));
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient client =
              ForwardClient.builder()
                  .baseUrl(server.url("/api/v1/forward/").toString())
                  .accessToken("test-token")
                  .build();
          PrintStream output = new PrintStream(bytes, true, "UTF-8")) {
        ForwardReadOnlyExample.run(client, "tmpl_test", output);
      }
      String text = bytes.toString("UTF-8");
      assertTrue(text.contains("model=auto"));
      assertTrue(text.contains("template=tmpl_test"));
      assertTrue(text.contains("request_id=req-selected"));
      assertFalse(text.contains("test-token"));
      assertEquals(3, server.getRequestCount());
      assertEquals("/api/v1/forward/models", server.takeRequest().getPath());
    }
  }

  @Test
  void newSessionContractsAreAvailableFromThePackagedJar() {
    com.qoder.cloudagents.forward.SessionBudget budget =
        com.qoder.cloudagents.forward.SessionBudget.builder()
            .type("limit")
            .maxCreditCost("100.00")
            .build();
    com.qoder.cloudagents.forward.SessionConfig config =
        com.qoder.cloudagents.forward.SessionConfig.builder()
            .model(com.qoder.cloudagents.forward.ModelConfig.of("ultimate"))
            .build();
    com.qoder.cloudagents.forward.SessionUpdateParams params =
        com.qoder.cloudagents.forward.SessionUpdateParams.builder()
            .budget(budget)
            .config(config)
            .build();
    assertEquals("100.00", params.budget().maxCreditCost());
    assertEquals("ultimate", params.config().model().id());
    com.qoder.cloudagents.core.JsonValue payload =
        com.qoder.cloudagents.core.JsonValue.parse(
            "{\"evaluated_permission\":\"deny\",\"evaluation\":{\"type\":\"auto\",\"evaluated_permission\":{\"type\":\"deny\",\"reason_code\":\"high_risk\"}}}");
    com.qoder.cloudagents.forward.SessionEvent event =
        new com.qoder.cloudagents.forward.SessionEvent(payload, null);
    assertEquals("deny", event.evaluatedPermission());
    assertEquals("high_risk", event.evaluation().evaluatedPermission().reasonCode());
    assertEquals(
        "deny",
        new com.qoder.cloudagents.managed.SessionEvent(payload, null).evaluatedPermission());
    assertEquals(
        "echo ready",
        com.qoder.cloudagents.forward.EnvironmentConfig.builder()
            .type("cloud")
            .startupScript("echo ready")
            .build()
            .startupScript());
    assertEquals(
        "echo ready",
        com.qoder.cloudagents.managed.EnvironmentConfig.builder()
            .type("cloud")
            .startupScript("echo ready")
            .build()
            .startupScript());
  }
}
