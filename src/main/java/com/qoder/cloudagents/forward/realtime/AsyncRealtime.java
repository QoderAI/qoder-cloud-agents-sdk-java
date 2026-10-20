package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncRealtime {
  private final ClientEngine engine;
  private final AsyncRealtimeConversations conversations;

  public AsyncRealtime(ClientEngine engine) {
    this.engine = engine;
    conversations = new AsyncRealtimeConversations(engine);
  }

  public AsyncRealtimeConversations conversations() {
    return conversations;
  }

  public CompletableFuture<RealtimeConnection> connect(RealtimeConnectParams params) {
    return connect(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<RealtimeConnection> connect(
      RealtimeConnectParams params, RequestOptions options) {
    return RealtimeConnection.open(engine, params, options);
  }
}
