package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class IdentityUsageListResponse
    extends CursorPage<IdentityUsage, IdentityUsageListResponse> {
  public IdentityUsageListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  IdentityUsageListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<IdentityUsageListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new IdentityUsage(item, requestId));
  }

  @Override
  protected IdentityUsageListResponse self() {
    return this;
  }

  public String startAt() {
    return optionalString("start_at");
  }

  public String endAt() {
    return optionalString("end_at");
  }
}
