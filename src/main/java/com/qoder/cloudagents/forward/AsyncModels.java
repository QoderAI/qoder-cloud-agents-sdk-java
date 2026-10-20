package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.ApiResponse;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.core.internal.ClientEngine;
import com.qoder.cloudagents.core.internal.Futures;
import java.util.Collections;
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
      return Futures.map(
          engine.get(new String[] {"models"}, Collections.emptyMap(), options),
          response -> response.map(value -> new ModelListResponse(value, response.requestId())));
    }
  }
}
