package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncBatches {
  private final ClientEngine engine;

  public AsyncBatches(ClientEngine engine) {
    this.engine = engine;
  }

  public AsyncBatchTasks tasks() {
    return new AsyncBatchTasks(engine);
  }

  public CompletableFuture<BatchListResponse> list() {
    return list(BatchListParams.builder().build());
  }

  public CompletableFuture<BatchListResponse> list(RequestOptions options) {
    return list(BatchListParams.builder().build(), options);
  }

  public CompletableFuture<BatchListResponse> list(BatchListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<BatchListResponse> list(BatchListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<Batch> create(BatchCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Batch> create(BatchCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<Batch> retrieve(String batchId) {
    return retrieve(batchId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Batch> retrieve(String batchId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(batchId, options), ApiResponse::data);
  }

  public CompletableFuture<Batch> cancel(String batchId) {
    return cancel(batchId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Batch> cancel(String batchId, RequestOptions options) {
    return Futures.map(withResponse().cancel(batchId, options), ApiResponse::data);
  }

  public CompletableFuture<BatchFile> getOutput(String batchId) {
    return getOutput(batchId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<BatchFile> getOutput(String batchId, RequestOptions options) {
    return Futures.map(withResponse().getOutput(batchId, options), ApiResponse::data);
  }

  public CompletableFuture<BatchFile> getError(String batchId) {
    return getError(batchId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<BatchFile> getError(String batchId, RequestOptions options) {
    return Futures.map(withResponse().getError(batchId, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<BatchListResponse>> list() {
      return list(BatchListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<BatchListResponse>> list(RequestOptions options) {
      return list(BatchListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<BatchListResponse>> list(BatchListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<BatchListResponse>> list(
        BatchListParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      if (options.query().containsKey("page"))
        throw new ConfigurationException("Unsupported pagination parameter for Batches");
      return CursorRequests.list(
          engine, new String[] {"batches"}, params.query(), options, BatchListResponse::new);
    }

    public CompletableFuture<ApiResponse<Batch>> create(BatchCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Batch>> create(
        BatchCreateParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"batches"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new Batch(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Batch>> retrieve(String batchId) {
      return retrieve(batchId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Batch>> retrieve(String batchId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"batches", batchId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Batch(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Batch>> cancel(String batchId) {
      return cancel(batchId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Batch>> cancel(String batchId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"batches", batchId, "cancel"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Batch(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<BatchFile>> getOutput(String batchId) {
      return getOutput(batchId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<BatchFile>> getOutput(
        String batchId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"batches", batchId, "output"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new BatchFile(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<BatchFile>> getError(String batchId) {
      return getError(batchId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<BatchFile>> getError(
        String batchId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"batches", batchId, "error"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new BatchFile(v, r.requestId())));
    }
  }
}
