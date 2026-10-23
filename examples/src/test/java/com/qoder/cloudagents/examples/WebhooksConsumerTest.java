package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.forward.WebhookVerificationException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class WebhooksConsumerTest {
  @Test
  void receiverUsesPackagedSdkAndPreservesPayload() throws Exception {
    assertTrue(
        ForwardClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()
            .endsWith(".jar"));
    byte[] key = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
    String secret = "whsec_" + Base64.getEncoder().encodeToString(key);
    for (String json :
        Arrays.asList(
            "{\"type\":\"event\",\"data\":{\"type\":\"future.event\",\"extra\":42}}",
            "{\"type\":\"event\",\"endpoint_id\":\"id\",\"note\":\"synthetic\"}")) {
      byte[] body = json.getBytes(StandardCharsets.UTF_8);
      String timestamp = Long.toString(Instant.now().getEpochSecond());
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      mac.update(("whe_test." + timestamp + ".").getBytes(StandardCharsets.UTF_8));
      Map<String, String> headers = new HashMap<>();
      headers.put("Webhook-ID", "whe_test");
      headers.put("Webhook-Timestamp", timestamp);
      headers.put(
          "Webhook-Signature", "v1," + Base64.getEncoder().encodeToString(mac.doFinal(body)));
      JsonValue result = WebhooksExample.parseDelivery(body, headers, secret);
      assertEquals(json, result.toJson());
      assertThrows(
          WebhookVerificationException.class,
          () ->
              WebhooksExample.parseDelivery(Arrays.copyOf(body, body.length + 1), headers, secret));
    }
  }
}
