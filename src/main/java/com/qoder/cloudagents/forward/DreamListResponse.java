package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** A cursor page with lazy iteration and cancellable asynchronous page requests. */
public final class DreamListResponse extends CursorPage<Dream, DreamListResponse> {
  public DreamListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  DreamListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<DreamListResponse>> fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new Dream(item, requestId));
  }

  @Override
  protected DreamListResponse self() {
    return this;
  }
}
