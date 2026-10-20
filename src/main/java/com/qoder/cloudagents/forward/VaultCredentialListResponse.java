package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public final class VaultCredentialListResponse
    extends CursorPage<VaultCredential, VaultCredentialListResponse> {
  public VaultCredentialListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  VaultCredentialListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<VaultCredentialListResponse>>
          fetcher) {
    super(
        value, requestId, query, seen, fetcher, item -> new VaultCredential(item, requestId), true);
  }

  public String nextPage() {
    return optionalString("next_page");
  }

  @Override
  protected VaultCredentialListResponse self() {
    return this;
  }
}
