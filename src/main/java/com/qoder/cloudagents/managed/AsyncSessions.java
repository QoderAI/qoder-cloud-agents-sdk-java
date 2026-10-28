package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncSessions {
  private final ClientEngine engine;

  public AsyncSessions(ClientEngine engine) {
    this.engine = engine;
  }

  public AsyncSessionEvents events() {
    return new AsyncSessionEvents(engine);
  }

  public AsyncSessionThreads threads() {
    return new AsyncSessionThreads(engine);
  }

  public AsyncSessionResources resources() {
    return new AsyncSessionResources(engine);
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

  public CompletableFuture<Session> create(SessionCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Session> create(SessionCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedSession> delete(String sessionId) {
    return delete(sessionId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedSession> delete(String sessionId, RequestOptions options) {
    return Futures.map(withResponse().delete(sessionId, options), ApiResponse::data);
  }

  public CompletableFuture<Session> retrieve(String sessionId) {
    return retrieve(sessionId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Session> retrieve(String sessionId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(sessionId, options), ApiResponse::data);
  }

  public CompletableFuture<SessionListResponse> list() {
    return list(SessionListParams.builder().build());
  }

  public CompletableFuture<SessionListResponse> list(RequestOptions options) {
    return list(SessionListParams.builder().build(), options);
  }

  public CompletableFuture<SessionListResponse> list(SessionListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionListResponse> list(
      SessionListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<SessionListResponse> search() {
    return search(SessionSearchParams.builder().build());
  }

  public CompletableFuture<SessionListResponse> search(RequestOptions options) {
    return search(SessionSearchParams.builder().build(), options);
  }

  public CompletableFuture<SessionListResponse> search(SessionSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionListResponse> search(
      SessionSearchParams params, RequestOptions options) {
    return Futures.map(withResponse().search(params, options), ApiResponse::data);
  }

  public CompletableFuture<Session> update(String sessionId, SessionUpdateParams params) {
    return update(sessionId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Session> update(
      String sessionId, SessionUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(sessionId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<Session>> archive(String sessionId) {
      return archive(sessionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Session>> archive(
        String sessionId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Session(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Session>> cancel(String sessionId) {
      return cancel(sessionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Session>> cancel(
        String sessionId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId, "cancel"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Session(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Session>> create(SessionCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Session>> create(
        SessionCreateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions"},
              Collections.emptyMap(),
              SessionMetadata.created(params.toJsonValue()),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Session(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedSession>> delete(String sessionId) {
      return delete(sessionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedSession>> delete(
        String sessionId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"sessions", sessionId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedSession(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Session>> retrieve(String sessionId) {
      return retrieve(sessionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Session>> retrieve(
        String sessionId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"sessions", sessionId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Session(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<SessionListResponse>> list() {
      return list(SessionListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<SessionListResponse>> list(RequestOptions options) {
      return list(SessionListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<SessionListResponse>> list(SessionListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionListResponse>> list(
        SessionListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine, new String[] {"sessions"}, params.query(), options, SessionListResponse::new);
    }

    public CompletableFuture<ApiResponse<SessionListResponse>> search() {
      return search(SessionSearchParams.builder().build());
    }

    public CompletableFuture<ApiResponse<SessionListResponse>> search(RequestOptions options) {
      return search(SessionSearchParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<SessionListResponse>> search(SessionSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionListResponse>> search(
        SessionSearchParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return SearchRequests.search(
          engine,
          new String[] {"sessions", "search"},
          params.toJsonValue(),
          Collections.emptyMap(),
          options,
          SessionListResponse::new);
    }

    public CompletableFuture<ApiResponse<Session>> update(
        String sessionId, SessionUpdateParams params) {
      return update(sessionId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Session>> update(
        String sessionId, SessionUpdateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Session(v, r.requestId())));
    }
  }
}
