package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Batches {
  private final AsyncBatches async;

  public Batches(AsyncBatches async) {
    this.async = async;
  }

  public BatchTasks tasks() {
    return new BatchTasks(async.tasks());
  }

  public BatchListResponse list() {
    return list(BatchListParams.builder().build());
  }

  public BatchListResponse list(RequestOptions options) {
    return list(BatchListParams.builder().build(), options);
  }

  public BatchListResponse list(BatchListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public BatchListResponse list(BatchListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public Batch create(BatchCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Batch create(BatchCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public Batch retrieve(String batchId) {
    return retrieve(batchId, RequestOptions.DEFAULT);
  }

  public Batch retrieve(String batchId, RequestOptions options) {
    return Futures.await(async.retrieve(batchId, options));
  }

  public Batch cancel(String batchId) {
    return cancel(batchId, RequestOptions.DEFAULT);
  }

  public Batch cancel(String batchId, RequestOptions options) {
    return Futures.await(async.cancel(batchId, options));
  }

  public BatchFile getOutput(String batchId) {
    return getOutput(batchId, RequestOptions.DEFAULT);
  }

  public BatchFile getOutput(String batchId, RequestOptions options) {
    return Futures.await(async.getOutput(batchId, options));
  }

  public BatchFile getError(String batchId) {
    return getError(batchId, RequestOptions.DEFAULT);
  }

  public BatchFile getError(String batchId, RequestOptions options) {
    return Futures.await(async.getError(batchId, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<BatchListResponse> list() {
      return list(BatchListParams.builder().build());
    }

    public ApiResponse<BatchListResponse> list(RequestOptions options) {
      return list(BatchListParams.builder().build(), options);
    }

    public ApiResponse<BatchListResponse> list(BatchListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<BatchListResponse> list(BatchListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<Batch> create(BatchCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Batch> create(BatchCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<Batch> retrieve(String batchId) {
      return retrieve(batchId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Batch> retrieve(String batchId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(batchId, options));
    }

    public ApiResponse<Batch> cancel(String batchId) {
      return cancel(batchId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Batch> cancel(String batchId, RequestOptions options) {
      return Futures.await(async.withResponse().cancel(batchId, options));
    }

    public ApiResponse<BatchFile> getOutput(String batchId) {
      return getOutput(batchId, RequestOptions.DEFAULT);
    }

    public ApiResponse<BatchFile> getOutput(String batchId, RequestOptions options) {
      return Futures.await(async.withResponse().getOutput(batchId, options));
    }

    public ApiResponse<BatchFile> getError(String batchId) {
      return getError(batchId, RequestOptions.DEFAULT);
    }

    public ApiResponse<BatchFile> getError(String batchId, RequestOptions options) {
      return Futures.await(async.withResponse().getError(batchId, options));
    }
  }
}
