package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** A Identity cursor page with lazy iteration and cancellable asynchronous page requests. */
public final class IdentityListResponse extends CursorPage<Identity, IdentityListResponse> {
  public IdentityListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  IdentityListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<IdentityListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new Identity(item, requestId));
  }

  @Override
  protected IdentityListResponse self() {
    return this;
  }
}
