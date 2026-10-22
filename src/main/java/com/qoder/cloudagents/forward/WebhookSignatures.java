package com.qoder.cloudagents.forward;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Verifies Forward delivery signatures using only Java 8 platform APIs. */
public final class WebhookSignatures {
  private WebhookSignatures() {}

  /**
   * Verify original body bytes before parsing. Accept any matching v1 signature within 300 seconds.
   * Deduplicate accepted events using Webhook-ID.
   */
  public static void verify(byte[] payload, Map<String, String> headers, String secret) {
    verifyAt(payload, headers, secret, Instant.now().getEpochSecond());
  }

  static void verifyAt(byte[] payload, Map<String, String> headers, String secret, long now) {
    if (payload == null || headers == null || secret == null)
      throw new WebhookVerificationException();
    String id = header(headers, "Webhook-ID"), timestamp = header(headers, "Webhook-Timestamp");
    if (id.isEmpty()
        || id.contains(".")
        || id.contains("\r")
        || id.contains("\n")
        || !timestamp.matches("[0-9]{1,19}")
        || !secret.startsWith("whsec_")) throw new WebhookVerificationException();
    long ts;
    try {
      ts = Long.parseLong(timestamp);
    } catch (NumberFormatException e) {
      throw new WebhookVerificationException();
    }
    if (ts < now - 300 || ts > now + 300) throw new WebhookVerificationException();
    byte[] key;
    try {
      key = Base64.getDecoder().decode(secret.substring(6));
    } catch (IllegalArgumentException e) {
      throw new WebhookVerificationException();
    }
    if (key.length < 24
        || key.length > 64
        || !Base64.getEncoder().encodeToString(key).equals(secret.substring(6)))
      throw new WebhookVerificationException();
    byte[] expected;
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      mac.update((id + "." + timestamp + ".").getBytes(StandardCharsets.UTF_8));
      expected = mac.doFinal(payload);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("HMAC-SHA256 unavailable", e);
    }
    for (String signature : header(headers, "Webhook-Signature").split("\\s+")) {
      if (!signature.startsWith("v1,")) continue;
      try {
        if (MessageDigest.isEqual(expected, Base64.getDecoder().decode(signature.substring(3))))
          return;
      } catch (IllegalArgumentException ignored) {
        /* Try the remaining signatures. */
      }
    }
    throw new WebhookVerificationException();
  }

  private static String header(Map<String, String> headers, String name) {
    String value = null;
    for (Map.Entry<String, String> e : headers.entrySet()) {
      if (name.equalsIgnoreCase(e.getKey())) {
        if (value != null || e.getValue() == null) throw new WebhookVerificationException();
        value = e.getValue();
      }
    }
    return value == null ? "" : value;
  }
}
