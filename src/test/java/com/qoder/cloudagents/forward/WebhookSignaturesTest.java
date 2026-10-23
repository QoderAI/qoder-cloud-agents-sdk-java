package com.qoder.cloudagents.forward;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.JsonValue;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;

class WebhookSignaturesTest {
  @Test
  void sharedVectorAndFailures() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (InputStream in = getClass().getResourceAsStream("/webhook-signature.json")) {
      byte[] b = new byte[1024];
      int n;
      while ((n = in.read(b)) != -1) out.write(b, 0, n);
    }
    JsonValue v = JsonValue.parse(new String(out.toByteArray(), StandardCharsets.UTF_8));
    String secret = v.get("secret").asString();
    byte[] body = v.get("payload").asString().getBytes(StandardCharsets.UTF_8);
    Map<String, String> h = new LinkedHashMap<>();
    h.put("webhook-id", v.get("id").asString());
    h.put("Webhook-Timestamp", v.get("timestamp").asString());
    h.put("Webhook-Signature", "v2,unknown v1,invalid " + v.get("signature").asString());
    for (long delta : new long[] {-300, 0, 300})
      WebhookSignatures.verifyAt(body, h, secret, 1767225600L + delta);
    for (long delta : new long[] {-301, 301})
      assertThrows(
          WebhookVerificationException.class,
          () -> WebhookSignatures.verifyAt(body, h, secret, 1767225600L + delta));
    for (String[] update :
        new String[][] {
          {"Webhook-Timestamp", "9223372036854775807"},
          {"Webhook-Timestamp", "-1"},
          {"Webhook-Signature", "v2,abc"},
          {"webhook-id", "other"}
        }) {
      Map<String, String> changed = new LinkedHashMap<>(h);
      changed.put(update[0], update[1]);
      assertThrows(
          WebhookVerificationException.class,
          () -> WebhookSignatures.verifyAt(body, changed, secret, 1767225600L));
    }
    assertThrows(
        WebhookVerificationException.class,
        () ->
            WebhookSignatures.verifyAt(
                Arrays.copyOf(body, body.length + 1), h, secret, 1767225600L));
    for (String bad : new String[] {"", "whsec_", "whsec_bad!", "whsec_dG9vIHNob3J0"})
      assertThrows(
          WebhookVerificationException.class,
          () -> WebhookSignatures.verifyAt(body, h, bad, 1767225600L));
  }
}
