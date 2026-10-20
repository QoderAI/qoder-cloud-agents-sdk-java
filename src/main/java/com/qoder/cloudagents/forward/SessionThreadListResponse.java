package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class SessionThreadListResponse
    extends CursorPage<SessionThread, SessionThreadListResponse> {
  public SessionThreadListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  SessionThreadListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<SessionThreadListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new SessionThread(item, requestId));
  }

  @Override
  protected SessionThreadListResponse self() {
    return this;
  }
}
