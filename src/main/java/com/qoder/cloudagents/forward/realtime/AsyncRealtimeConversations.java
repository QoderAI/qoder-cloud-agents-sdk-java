package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncRealtimeConversations {
  private final ClientEngine engine;

  public AsyncRealtimeConversations(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<RealtimeConversation> create(RealtimeConversationCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<RealtimeConversation> create(
      RealtimeConversationCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<RealtimeHistoryPage> history(
      String conversationId, RealtimeHistoryParams params) {
    return history(conversationId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<RealtimeHistoryPage> history(
      String conversationId, RealtimeHistoryParams params, RequestOptions options) {
    return Futures.map(withResponse().history(conversationId, params, options), ApiResponse::data);
  }

  public CompletableFuture<RealtimeHistoryPage> history(String conversationId) {
    return history(conversationId, RealtimeHistoryParams.builder().build());
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<RealtimeConversation>> create(
        RealtimeConversationCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<RealtimeConversation>> create(
        RealtimeConversationCreateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Options must not be null");
      RealtimeValidation.create(params);
      String key = engine.defaultHeader("Idempotency-Key");
      for (Map.Entry<String, String> e : options.headers().entrySet())
        if (e.getKey().equalsIgnoreCase("Idempotency-Key")) key = e.getValue();
      RealtimeValidation.text(key, "Idempotency-Key", 256);
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"realtime", "conversations"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new RealtimeConversation(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<RealtimeHistoryPage>> history(
        String conversationId, RealtimeHistoryParams params) {
      return history(conversationId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<RealtimeHistoryPage>> history(
        String conversationId, RealtimeHistoryParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Parameters and options must not be null");
      RealtimeValidation.text(conversationId, "conversation_id", 128);
      Map<String, String> query = new LinkedHashMap<>(params.query());
      query.putAll(options.query());
      RealtimeValidation.history(query);
      Set<String> seen = new HashSet<>();
      if (query.containsKey("before")) seen.add(query.get("before"));
      return historyPage(conversationId, query, seen, options.toBuilder().clearQuery().build());
    }
  }

  private CompletableFuture<ApiResponse<RealtimeHistoryPage>> historyPage(
      String id, Map<String, String> query, Set<String> seen, RequestOptions options) {
    return Futures.map(
        engine.get(new String[] {"realtime", "conversations", id, "history"}, query, options),
        r ->
            r.map(
                v ->
                    new RealtimeHistoryPage(
                        v,
                        r.requestId(),
                        seen,
                        (before, nextSeen) -> {
                          Map<String, String> next = new LinkedHashMap<>(query);
                          next.put("before", before);
                          return Futures.map(
                              historyPage(id, next, nextSeen, options), ApiResponse::data);
                        })));
  }
}
