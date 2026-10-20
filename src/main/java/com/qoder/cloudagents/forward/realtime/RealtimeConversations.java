package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.*;
import java.util.*;

public final class RealtimeConversations {
  private final AsyncRealtimeConversations async;

  public RealtimeConversations(AsyncRealtimeConversations async) {
    this.async = async;
  }

  public RealtimeConversation create(RealtimeConversationCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public RealtimeConversation create(
      RealtimeConversationCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public RealtimeHistoryPage history(String conversationId, RealtimeHistoryParams params) {
    return history(conversationId, params, RequestOptions.DEFAULT);
  }

  public RealtimeHistoryPage history(
      String conversationId, RealtimeHistoryParams params, RequestOptions options) {
    return Futures.await(async.history(conversationId, params, options));
  }

  public RealtimeHistoryPage history(String conversationId) {
    return history(conversationId, RealtimeHistoryParams.builder().build());
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<RealtimeConversation> create(RealtimeConversationCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<RealtimeConversation> create(
        RealtimeConversationCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<RealtimeHistoryPage> history(
        String conversationId, RealtimeHistoryParams params) {
      return history(conversationId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<RealtimeHistoryPage> history(
        String conversationId, RealtimeHistoryParams params, RequestOptions options) {
      return Futures.await(async.withResponse().history(conversationId, params, options));
    }
  }
}
