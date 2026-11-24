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
  void capabilityModelsAreAvailableFromThePackagedJar() {
    com.qoder.cloudagents.forward.MultiagentEntry reference =
        com.qoder.cloudagents.forward.MultiagentEntry.of("tmpl_child");
    assertTrue(reference.isString());
    assertEquals("tmpl_child", reference.templateId());
    com.qoder.cloudagents.forward.SessionUpdateConfig update =
        com.qoder.cloudagents.forward.SessionUpdateConfig.builder()
            .multiagent(
                com.qoder.cloudagents.forward.MultiagentConfig.builder()
                    .type("coordinator")
                    .agents(java.util.Collections.singletonList(reference))
                    .build())
            .build();
    assertEquals("tmpl_child", update.multiagent().agents().get(0).templateId());
    assertTrue(
        com.qoder.cloudagents.forward.SessionCreateParams.builder()
            .identityId("idn_test")
            .templateId("tmpl_test")
            .autoTitle(true)
            .build()
            .autoTitle());
    assertTrue(
        com.qoder.cloudagents.forward.TemplateA2AConfig.builder().enabled(true).build().enabled());
    com.qoder.cloudagents.forward.TemplateAgentCard card =
        new com.qoder.cloudagents.forward.TemplateAgentCard(
            com.qoder.cloudagents.core.JsonValue.parse(
                "{\"name\":\"Summary\",\"defaultInputModes\":[\"text/plain\"]}"),
            null);
    assertEquals("Summary", card.name());
    assertEquals("text/plain", card.defaultInputModes().get(0));
    com.qoder.cloudagents.forward.EnvironmentStorageBucketParams bucket =
        com.qoder.cloudagents.forward.EnvironmentStorageBucketParams.builder()
            .name("data")
            .provider("aliyun-oss")
            .region("cn-hangzhou")
            .bucket("test-bucket")
            .prefix("team/")
            .access("read_only")
            .acceleratorEndpoint("test.oss-data-acc.aliyuncs.com")
            .credential(
                com.qoder.cloudagents.forward.EnvironmentStorageCredentialParams.builder()
                    .type("ram_role")
                    .roleArn("test-role")
                    .build())
            .build();
    assertEquals("aliyun-oss", bucket.provider());
    assertEquals("cn-hangzhou", bucket.region());
    assertEquals("test-bucket", bucket.bucket());
    assertEquals("team/", bucket.prefix());
    assertEquals("read_only", bucket.access());
    assertEquals("test.oss-data-acc.aliyuncs.com", bucket.acceleratorEndpoint());
    assertEquals(
        "data",
        com.qoder.cloudagents.forward.EnvironmentStorageParams.builder()
            .buckets(java.util.Collections.singletonList(bucket))
            .build()
            .buckets()
            .get(0)
            .name());
    assertEquals(
        1,
        com.qoder.cloudagents.managed.SessionThreadEventStreamParams.builder()
            .eventDeltas(java.util.Collections.singletonList("agent.message"))
            .build()
            .eventDeltas()
            .size());
    assertEquals(
        "resource",
        com.qoder.cloudagents.managed.VaultCredentialOAuthRefresh.builder()
            .resource("resource")
            .build()
            .resource());
    assertEquals(
        "always_deny",
        com.qoder.cloudagents.managed.Tool.builder()
            .type("mcp_toolset")
            .mcpServerName("test")
            .permissionPolicy(
                com.qoder.cloudagents.managed.PermissionPolicy.builder()
                    .type("always_deny")
                    .build())
            .build()
            .permissionPolicy()
            .type());
  }

  @Test
  void newSessionContractsAreAvailableFromThePackagedJar() {
    com.qoder.cloudagents.forward.SessionBudget budget =
        com.qoder.cloudagents.forward.SessionBudget.builder()
            .type("limit")
            .maxCreditCost("100.00")
            .build();
    com.qoder.cloudagents.forward.SessionUpdateConfig config =
        com.qoder.cloudagents.forward.SessionUpdateConfig.builder()
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
