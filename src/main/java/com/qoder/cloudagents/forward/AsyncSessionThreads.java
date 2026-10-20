package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncSessionThreads {
  private final ClientEngine engine;
  private final AsyncSessionThreadEvents events;

  public AsyncSessionThreads(ClientEngine engine) {
    this.engine = engine;
    events = new AsyncSessionThreadEvents(engine);
  }

  public AsyncSessionThreadEvents events() {
    return events;
  }

  public CompletableFuture<SessionThreadListResponse> list(String sessionId) {
    return list(sessionId, SessionThreadListParams.builder().build());
  }

  public CompletableFuture<SessionThreadListResponse> list(
      String sessionId, SessionThreadListParams params) {
    return list(sessionId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionThreadListResponse> list(
      String sessionId, SessionThreadListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(sessionId, params, options), ApiResponse::data);
  }

  public CompletableFuture<SessionThread> retrieve(String sessionId, String threadId) {
    return retrieve(sessionId, threadId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionThread> retrieve(
      String sessionId, String threadId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(sessionId, threadId, options), ApiResponse::data);
  }

  public CompletableFuture<SessionThread> archive(String sessionId, String threadId) {
    return archive(sessionId, threadId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionThread> archive(
      String sessionId, String threadId, RequestOptions options) {
    return Futures.map(withResponse().archive(sessionId, threadId, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<SessionThreadListResponse>> list(String sessionId) {
      return list(sessionId, SessionThreadListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<SessionThreadListResponse>> list(
        String sessionId, SessionThreadListParams params) {
      return list(sessionId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionThreadListResponse>> list(
        String sessionId, SessionThreadListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return CursorRequests.list(
          engine,
          new String[] {"sessions", sessionId, "threads"},
          params.query(),
          options,
          SessionThreadListResponse::new);
    }

    public CompletableFuture<ApiResponse<SessionThread>> retrieve(
        String sessionId, String threadId) {
      return retrieve(sessionId, threadId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionThread>> retrieve(
        String sessionId, String threadId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"sessions", sessionId, "threads", threadId},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new SessionThread(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<SessionThread>> archive(
        String sessionId, String threadId) {
      return archive(sessionId, threadId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionThread>> archive(
        String sessionId, String threadId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId, "threads", threadId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new SessionThread(value, response.requestId())));
    }
  }
}
