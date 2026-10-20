package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
public final class ChannelListResponse extends CursorPage<Channel, ChannelListResponse> {
  public ChannelListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  ChannelListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<ChannelListResponse>>
          fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new Channel(item, requestId));
  }

  @Override
  protected ChannelListResponse self() {
    return this;
  }
}
