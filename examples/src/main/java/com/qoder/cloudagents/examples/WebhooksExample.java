package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.forward.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Lists endpoints. parseDelivery is the framework-independent receiver integration point. */
public final class WebhooksExample {
  private WebhooksExample() {}

  public static JsonValue parseDelivery(
      byte[] rawBody, Map<String, String> headers, String secret) {
    WebhookSignatures.verify(rawBody, headers, secret);
    return JsonValue.parse(new String(rawBody, StandardCharsets.UTF_8));
  }

  public static void main(String[] args) {
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      for (WebhookEndpoint endpoint : setup.client().webhooks().endpoints().list())
        System.out.println("endpoint=" + endpoint.id() + " active=" + endpoint.active());
    }
  }
}
