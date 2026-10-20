package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public final class SkillVersionListResponse
    extends CursorPage<SkillVersion, SkillVersionListResponse> {
  public SkillVersionListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  SkillVersionListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<SkillVersionListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new SkillVersion(item, requestId), true);
  }

  public String nextPage() {
    return optionalString("next_page");
  }

  @Override
  protected SkillVersionListResponse self() {
    return this;
  }
}
