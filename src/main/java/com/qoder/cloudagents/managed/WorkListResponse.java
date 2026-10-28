package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class WorkListResponse extends CursorPage<WorkItem, WorkListResponse> {
  public WorkListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  WorkListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<WorkListResponse>> fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new WorkItem(item, requestId), true);
  }

  @Override
  protected WorkListResponse self() {
    return this;
  }
}
