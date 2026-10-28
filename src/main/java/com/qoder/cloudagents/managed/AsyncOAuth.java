package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncOAuth {
  private final ClientEngine engine;

  public AsyncOAuth(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<OAuthStartResponse> start(OAuthStartParams params) {
    return start(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<OAuthStartResponse> start(
      OAuthStartParams params, RequestOptions options) {
    return Futures.map(withResponse().start(params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<OAuthStartResponse>> start(OAuthStartParams params) {
      return start(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<OAuthStartResponse>> start(
        OAuthStartParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"oauth", "start"},
              Collections.emptyMap(),
              params.toJsonValue(),
              false,
              options.toBuilder().maxRetries(0).build()),
          r -> r.map(v -> v.isNull() ? null : new OAuthStartResponse(v, r.requestId())));
    }
  }
}
