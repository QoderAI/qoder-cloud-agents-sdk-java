package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** A Template cursor page with lazy iteration and cancellable asynchronous page requests. */
public final class TemplateListResponse extends CursorPage<Template, TemplateListResponse> {
  public TemplateListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  TemplateListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<TemplateListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new Template(item, requestId));
  }

  @Override
  protected TemplateListResponse self() {
    return this;
  }
}
