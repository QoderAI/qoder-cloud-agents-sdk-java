package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public final class VaultListResponse extends CursorPage<Vault, VaultListResponse> {
  public VaultListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  VaultListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<VaultListResponse>> fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new Vault(item, requestId), true);
  }

  public String nextPage() {
    return optionalString("next_page");
  }

  @Override
  protected VaultListResponse self() {
    return this;
  }
}
