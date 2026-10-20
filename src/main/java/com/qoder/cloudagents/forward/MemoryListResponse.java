package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page retaining filters and request options for subsequent pages. */
public final class MemoryListResponse extends CursorPage<Memory, MemoryListResponse> {
  public MemoryListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  MemoryListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<MemoryListResponse>> fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new Memory(item, requestId));
  }

  @Override
  protected MemoryListResponse self() {
    return this;
  }
}
