package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page retaining filters and request options for subsequent pages. */
public final class MemoryStoreListResponse
    extends CursorPage<MemoryStore, MemoryStoreListResponse> {
  public MemoryStoreListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  MemoryStoreListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<MemoryStoreListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new MemoryStore(item, requestId));
  }

  @Override
  protected MemoryStoreListResponse self() {
    return this;
  }
}
