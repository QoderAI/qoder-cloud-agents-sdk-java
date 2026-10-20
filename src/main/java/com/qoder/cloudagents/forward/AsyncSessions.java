package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncSessions {
  private final ClientEngine engine;
  private final AsyncSessionEvents events;
  private final AsyncSessionResources resources;
  private final AsyncSessionThreads threads;

  public AsyncSessions(ClientEngine engine) {
    this.engine = engine;
    events = new AsyncSessionEvents(engine);
    resources = new AsyncSessionResources(engine);
    threads = new AsyncSessionThreads(engine);
  }

  public AsyncSessionEvents events() {
    return events;
  }

  public AsyncSessionResources resources() {
    return resources;
  }

  public AsyncSessionThreads threads() {
    return threads;
  }

  public CompletableFuture<SessionListResponse> list() {
    return list(SessionListParams.builder().build());
  }

  public CompletableFuture<SessionListResponse> list(SessionListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionListResponse> list(
      SessionListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<Session> create(SessionCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Session> create(SessionCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<Session> retrieve(String sessionId) {
    return retrieve(sessionId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Session> retrieve(String sessionId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(sessionId, options), ApiResponse::data);
  }

  public CompletableFuture<Session> update(String sessionId, SessionUpdateParams params) {
    return update(sessionId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Session> update(
      String sessionId, SessionUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(sessionId, params, options), ApiResponse::data);
  }

  public CompletableFuture<Session> archive(String sessionId) {
    return archive(sessionId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Session> archive(String sessionId, RequestOptions options) {
    return Futures.map(withResponse().archive(sessionId, options), ApiResponse::data);
  }

  public CompletableFuture<Session> cancel(String sessionId) {
    return cancel(sessionId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Session> cancel(String sessionId, RequestOptions options) {
    return Futures.map(withResponse().cancel(sessionId, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<SessionListResponse>> list() {
      return list(SessionListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<SessionListResponse>> list(SessionListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionListResponse>> list(
        SessionListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return CursorRequests.list(
          engine, new String[] {"sessions"}, params.query(), options, SessionListResponse::new);
    }

    public CompletableFuture<ApiResponse<Session>> create(SessionCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Session>> create(
        SessionCreateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new Session(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Session>> retrieve(String sessionId) {
      return retrieve(sessionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Session>> retrieve(
        String sessionId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"sessions", sessionId},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new Session(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Session>> update(
        String sessionId, SessionUpdateParams params) {
      return update(sessionId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Session>> update(
        String sessionId, SessionUpdateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new Session(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Session>> archive(String sessionId) {
      return archive(sessionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Session>> archive(
        String sessionId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new Session(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Session>> cancel(String sessionId) {
      return cancel(sessionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Session>> cancel(
        String sessionId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId, "cancel"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new Session(value, response.requestId())));
    }
  }
}
