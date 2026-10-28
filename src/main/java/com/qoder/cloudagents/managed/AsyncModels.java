package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncModels {
  private final ClientEngine engine;

  public AsyncModels(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<ModelListResponse> list() {
    return list(RequestOptions.DEFAULT);
  }

  public CompletableFuture<ModelListResponse> list(RequestOptions options) {
    return Futures.map(withResponse().list(options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<ModelListResponse>> list() {
      return list(RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ModelListResponse>> list(RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET", new String[] {"models"}, Collections.emptyMap(), null, true, options),
          r -> r.map(v -> v.isNull() ? null : new ModelListResponse(v, r.requestId())));
    }
  }
}
