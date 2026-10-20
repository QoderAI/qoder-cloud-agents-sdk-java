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
}
