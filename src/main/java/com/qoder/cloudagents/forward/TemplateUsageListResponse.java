package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class TemplateUsageListResponse
    extends CursorPage<TemplateUsage, TemplateUsageListResponse> {
  public TemplateUsageListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  TemplateUsageListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<TemplateUsageListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new TemplateUsage(item, requestId));
  }

  @Override
  protected TemplateUsageListResponse self() {
    return this;
  }

  public String startAt() {
    return optionalString("start_at");
  }

  public String endAt() {
    return optionalString("end_at");
  }
}
