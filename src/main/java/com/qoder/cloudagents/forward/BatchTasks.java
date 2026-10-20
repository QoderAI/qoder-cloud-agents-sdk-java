package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class BatchTasks {
  private final AsyncBatchTasks async;

  public BatchTasks(AsyncBatchTasks async) {
    this.async = async;
  }

  public BatchTaskListResponse list(String batchId) {
    return list(batchId, BatchTaskListParams.builder().build());
  }

  public BatchTaskListResponse list(String batchId, RequestOptions options) {
    return list(batchId, BatchTaskListParams.builder().build(), options);
  }

  public BatchTaskListResponse list(String batchId, BatchTaskListParams params) {
    return list(batchId, params, RequestOptions.DEFAULT);
  }

  public BatchTaskListResponse list(
      String batchId, BatchTaskListParams params, RequestOptions options) {
    return Futures.await(async.list(batchId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<BatchTaskListResponse> list(String batchId) {
      return list(batchId, BatchTaskListParams.builder().build());
    }

    public ApiResponse<BatchTaskListResponse> list(String batchId, RequestOptions options) {
      return list(batchId, BatchTaskListParams.builder().build(), options);
    }

    public ApiResponse<BatchTaskListResponse> list(String batchId, BatchTaskListParams params) {
      return list(batchId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<BatchTaskListResponse> list(
        String batchId, BatchTaskListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(batchId, params, options));
    }
  }
}
