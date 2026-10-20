package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class BatchListResponse extends CursorPage<Batch, BatchListResponse> {
  public BatchListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  BatchListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<BatchListResponse>> fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new Batch(item, requestId));
  }

  @Override
  protected BatchListResponse self() {
    return this;
  }
}
