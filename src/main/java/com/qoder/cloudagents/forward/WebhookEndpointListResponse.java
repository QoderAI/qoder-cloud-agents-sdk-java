package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public final class WebhookEndpointListResponse
    extends CursorPage<WebhookEndpoint, WebhookEndpointListResponse> {
  public WebhookEndpointListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  WebhookEndpointListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<WebhookEndpointListResponse>>
          fetcher) {
    super(
        value,
        requestId,
        query,
        seen,
        fetcher,
        item -> new WebhookEndpoint(item, requestId),
        false);
  }

  @Override
  protected WebhookEndpointListResponse self() {
    return this;
  }
}
