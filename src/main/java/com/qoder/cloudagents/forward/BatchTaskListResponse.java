package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class BatchTaskListResponse extends CursorPage<BatchTask, BatchTaskListResponse> {
  public BatchTaskListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  BatchTaskListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<BatchTaskListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new BatchTask(item, requestId));
  }

  @Override
  protected BatchTaskListResponse self() {
    return this;
  }
}
