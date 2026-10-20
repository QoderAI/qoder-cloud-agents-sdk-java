package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncSessionResources {
  private final ClientEngine engine;

  public AsyncSessionResources(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<SessionResource> add(String sessionId, SessionResourceAddParams params) {
    return add(sessionId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionResource> add(
      String sessionId, SessionResourceAddParams params, RequestOptions options) {
    return Futures.map(withResponse().add(sessionId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<SessionResource>> add(
        String sessionId, SessionResourceAddParams params) {
      return add(sessionId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionResource>> add(
        String sessionId, SessionResourceAddParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId, "resources"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new SessionResource(value, response.requestId())));
    }
  }
}
