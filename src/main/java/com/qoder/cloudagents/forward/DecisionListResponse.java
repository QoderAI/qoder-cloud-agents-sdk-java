package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** A cursor page with lazy iteration and cancellable asynchronous page requests. */
public final class DecisionListResponse extends CursorPage<Decision, DecisionListResponse> {
  public DecisionListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  DecisionListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<DecisionListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new Decision(item, requestId));
  }

  @Override
  protected DecisionListResponse self() {
    return this;
  }
}
