package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class ScheduleRunListResponse
    extends CursorPage<ScheduleRun, ScheduleRunListResponse> {
  public ScheduleRunListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  ScheduleRunListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<ScheduleRunListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new ScheduleRun(item, requestId));
  }

  @Override
  protected ScheduleRunListResponse self() {
    return this;
  }
}
