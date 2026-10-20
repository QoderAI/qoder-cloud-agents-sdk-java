package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;

public final class Realtime {
  private final ClientEngine engine;
  private final RealtimeConversations conversations;

  public Realtime(ClientEngine engine) {
    this.engine = engine;
    conversations = new RealtimeConversations(new AsyncRealtimeConversations(engine));
  }

  public RealtimeConversations conversations() {
    return conversations;
  }

  public RealtimeConnection connect(RealtimeConnectParams params) {
    return connect(params, RequestOptions.DEFAULT);
  }

  public RealtimeConnection connect(RealtimeConnectParams params, RequestOptions options) {
    return Futures.await(RealtimeConnection.open(engine, params, options));
  }
}
