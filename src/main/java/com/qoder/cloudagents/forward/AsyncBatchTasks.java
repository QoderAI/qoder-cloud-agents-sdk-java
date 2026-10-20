package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncBatchTasks {
  private final ClientEngine engine;

  public AsyncBatchTasks(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<BatchTaskListResponse> list(String batchId) {
    return list(batchId, BatchTaskListParams.builder().build());
  }

  public CompletableFuture<BatchTaskListResponse> list(String batchId, RequestOptions options) {
    return list(batchId, BatchTaskListParams.builder().build(), options);
  }

  public CompletableFuture<BatchTaskListResponse> list(String batchId, BatchTaskListParams params) {
    return list(batchId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<BatchTaskListResponse> list(
      String batchId, BatchTaskListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(batchId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<BatchTaskListResponse>> list(String batchId) {
      return list(batchId, BatchTaskListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<BatchTaskListResponse>> list(
        String batchId, RequestOptions options) {
      return list(batchId, BatchTaskListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<BatchTaskListResponse>> list(
        String batchId, BatchTaskListParams params) {
      return list(batchId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<BatchTaskListResponse>> list(
        String batchId, BatchTaskListParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      if (options.query().containsKey("page") || options.query().containsKey("before_id"))
        throw new ConfigurationException("Unsupported pagination parameter for BatchTasks");
      return CursorRequests.list(
          engine,
          new String[] {"batches", batchId, "tasks"},
          params.query(),
          options,
          BatchTaskListResponse::new);
    }
  }
}
