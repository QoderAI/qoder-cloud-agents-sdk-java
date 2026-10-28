package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class DeploymentScopedRunListResponse
    extends CursorPage<DeploymentRun, DeploymentScopedRunListResponse> {
  public DeploymentScopedRunListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  DeploymentScopedRunListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<
              Map<String, String>, Set<String>, CompletableFuture<DeploymentScopedRunListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new DeploymentRun(item, requestId), true);
  }

  @Override
  protected DeploymentScopedRunListResponse self() {
    return this;
  }
}
