package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.internal.ClientEngine;

public final class AsyncWebhooks {
  private final AsyncWebhookEndpoints endpoints;

  public AsyncWebhooks(ClientEngine engine) {
    endpoints = new AsyncWebhookEndpoints(engine);
  }

  public AsyncWebhookEndpoints endpoints() {
    return endpoints;
  }
}
