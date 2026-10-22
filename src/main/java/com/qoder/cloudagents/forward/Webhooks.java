package com.qoder.cloudagents.forward;

public final class Webhooks {
  private final WebhookEndpoints endpoints;

  public Webhooks(AsyncWebhooks async) {
    endpoints = new WebhookEndpoints(async.endpoints());
  }

  public WebhookEndpoints endpoints() {
    return endpoints;
  }
}
