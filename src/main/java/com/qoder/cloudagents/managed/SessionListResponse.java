package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class SessionListResponse extends CursorPage<Session, SessionListResponse> {
  public SessionListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  SessionListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<SessionListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new Session(item, requestId), true);
  }

  @Override
  protected SessionListResponse self() {
    return this;
  }
}
