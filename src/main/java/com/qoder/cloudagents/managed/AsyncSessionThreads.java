package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncSessionThreads {
  private final ClientEngine engine;

  public AsyncSessionThreads(ClientEngine engine) {
    this.engine = engine;
  }

  public AsyncSessionThreadEvents events() {
    return new AsyncSessionThreadEvents(engine);
  }

  public CompletableFuture<SessionThread> archive(String sessionId, String threadId) {
    return archive(sessionId, threadId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionThread> archive(
      String sessionId, String threadId, RequestOptions options) {
    return Futures.map(withResponse().archive(sessionId, threadId, options), ApiResponse::data);
  }

  public CompletableFuture<SessionThread> retrieve(String sessionId, String threadId) {
    return retrieve(sessionId, threadId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionThread> retrieve(
      String sessionId, String threadId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(sessionId, threadId, options), ApiResponse::data);
  }

  public CompletableFuture<SessionThreadListResponse> list(String sessionId) {
    return list(sessionId, SessionThreadListParams.builder().build());
  }

  public CompletableFuture<SessionThreadListResponse> list(
      String sessionId, RequestOptions options) {
    return list(sessionId, SessionThreadListParams.builder().build(), options);
  }

  public CompletableFuture<SessionThreadListResponse> list(
      String sessionId, SessionThreadListParams params) {
    return list(sessionId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionThreadListResponse> list(
      String sessionId, SessionThreadListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(sessionId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<SessionThread>> archive(
        String sessionId, String threadId) {
      return archive(sessionId, threadId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionThread>> archive(
        String sessionId, String threadId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId, "threads", threadId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new SessionThread(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<SessionThread>> retrieve(
        String sessionId, String threadId) {
      return retrieve(sessionId, threadId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionThread>> retrieve(
        String sessionId, String threadId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"sessions", sessionId, "threads", threadId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new SessionThread(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<SessionThreadListResponse>> list(String sessionId) {
      return list(sessionId, SessionThreadListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<SessionThreadListResponse>> list(
        String sessionId, RequestOptions options) {
      return list(sessionId, SessionThreadListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<SessionThreadListResponse>> list(
        String sessionId, SessionThreadListParams params) {
      return list(sessionId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionThreadListResponse>> list(
        String sessionId, SessionThreadListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine,
          new String[] {"sessions", sessionId, "threads"},
          params.query(),
          options,
          SessionThreadListResponse::new);
    }
  }
}
