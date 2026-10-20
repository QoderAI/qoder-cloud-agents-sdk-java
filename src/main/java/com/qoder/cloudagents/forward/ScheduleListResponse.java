package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class ScheduleListResponse extends CursorPage<Schedule, ScheduleListResponse> {
  public ScheduleListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  ScheduleListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<ScheduleListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new Schedule(item, requestId));
  }

  @Override
  protected ScheduleListResponse self() {
    return this;
  }
}
