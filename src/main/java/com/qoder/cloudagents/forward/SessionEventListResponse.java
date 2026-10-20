package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class SessionEventListResponse
    extends CursorPage<SessionEvent, SessionEventListResponse> {
  public SessionEventListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  SessionEventListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<SessionEventListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new SessionEvent(item, requestId));
  }

  @Override
  protected SessionEventListResponse self() {
    return this;
  }
}
