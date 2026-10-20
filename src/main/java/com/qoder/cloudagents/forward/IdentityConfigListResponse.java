package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** A IdentityConfig cursor page with lazy iteration and cancellable asynchronous page requests. */
public final class IdentityConfigListResponse
    extends CursorPage<IdentityConfig, IdentityConfigListResponse> {
  public IdentityConfigListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  IdentityConfigListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<IdentityConfigListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new IdentityConfig(item, requestId));
  }

  @Override
  protected IdentityConfigListResponse self() {
    return this;
  }
}
