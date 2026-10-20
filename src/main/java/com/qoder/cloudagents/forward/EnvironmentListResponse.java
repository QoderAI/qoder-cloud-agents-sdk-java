package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public final class EnvironmentListResponse
    extends CursorPage<Environment, EnvironmentListResponse> {
  public EnvironmentListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  EnvironmentListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<EnvironmentListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new Environment(item, requestId), true);
  }

  public String nextPage() {
    return optionalString("next_page");
  }

  @Override
  protected EnvironmentListResponse self() {
    return this;
  }
}
