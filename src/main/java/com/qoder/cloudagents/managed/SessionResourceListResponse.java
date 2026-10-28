package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class SessionResourceListResponse
    extends CursorPage<SessionResource, SessionResourceListResponse> {
  public SessionResourceListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  SessionResourceListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<SessionResourceListResponse>>
          fetcher) {
    super(
        value, requestId, query, seen, fetcher, item -> new SessionResource(item, requestId), true);
  }

  @Override
  protected SessionResourceListResponse self() {
    return this;
  }
}
