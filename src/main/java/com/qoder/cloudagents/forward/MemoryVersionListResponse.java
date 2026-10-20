package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page retaining filters and request options for subsequent pages. */
public final class MemoryVersionListResponse
    extends CursorPage<MemoryVersion, MemoryVersionListResponse> {
  public MemoryVersionListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  MemoryVersionListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<MemoryVersionListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new MemoryVersion(item, requestId));
  }

  @Override
  protected MemoryVersionListResponse self() {
    return this;
  }
}
