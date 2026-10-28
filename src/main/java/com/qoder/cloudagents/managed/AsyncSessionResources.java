package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncSessionResources {
  private final ClientEngine engine;

  public AsyncSessionResources(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<SessionResource> create(
      String sessionId, SessionResourceCreateParams params) {
    return create(sessionId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionResource> create(
      String sessionId, SessionResourceCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(sessionId, params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedSessionResource> delete(String sessionId, String resourceId) {
    return delete(sessionId, resourceId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedSessionResource> delete(
      String sessionId, String resourceId, RequestOptions options) {
    return Futures.map(withResponse().delete(sessionId, resourceId, options), ApiResponse::data);
  }

  public CompletableFuture<SessionResource> retrieve(String sessionId, String resourceId) {
    return retrieve(sessionId, resourceId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionResource> retrieve(
      String sessionId, String resourceId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(sessionId, resourceId, options), ApiResponse::data);
  }

  public CompletableFuture<SessionResourceListResponse> list(String sessionId) {
    return list(sessionId, SessionResourceListParams.builder().build());
  }

  public CompletableFuture<SessionResourceListResponse> list(
      String sessionId, RequestOptions options) {
    return list(sessionId, SessionResourceListParams.builder().build(), options);
  }

  public CompletableFuture<SessionResourceListResponse> list(
      String sessionId, SessionResourceListParams params) {
    return list(sessionId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionResourceListResponse> list(
      String sessionId, SessionResourceListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(sessionId, params, options), ApiResponse::data);
  }

  public CompletableFuture<SessionResource> update(
      String sessionId, String resourceId, SessionResourceUpdateParams params) {
    return update(sessionId, resourceId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionResource> update(
      String sessionId,
      String resourceId,
      SessionResourceUpdateParams params,
      RequestOptions options) {
    return Futures.map(
        withResponse().update(sessionId, resourceId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<SessionResource>> create(
        String sessionId, SessionResourceCreateParams params) {
      return create(sessionId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionResource>> create(
        String sessionId, SessionResourceCreateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId, "resources"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new SessionResource(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedSessionResource>> delete(
        String sessionId, String resourceId) {
      return delete(sessionId, resourceId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedSessionResource>> delete(
        String sessionId, String resourceId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"sessions", sessionId, "resources", resourceId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedSessionResource(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<SessionResource>> retrieve(
        String sessionId, String resourceId) {
      return retrieve(sessionId, resourceId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionResource>> retrieve(
        String sessionId, String resourceId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"sessions", sessionId, "resources", resourceId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new SessionResource(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<SessionResourceListResponse>> list(String sessionId) {
      return list(sessionId, SessionResourceListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<SessionResourceListResponse>> list(
        String sessionId, RequestOptions options) {
      return list(sessionId, SessionResourceListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<SessionResourceListResponse>> list(
        String sessionId, SessionResourceListParams params) {
      return list(sessionId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionResourceListResponse>> list(
        String sessionId, SessionResourceListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine,
          new String[] {"sessions", sessionId, "resources"},
          params.query(),
          options,
          SessionResourceListResponse::new);
    }

    public CompletableFuture<ApiResponse<SessionResource>> update(
        String sessionId, String resourceId, SessionResourceUpdateParams params) {
      return update(sessionId, resourceId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionResource>> update(
        String sessionId,
        String resourceId,
        SessionResourceUpdateParams params,
        RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId, "resources", resourceId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new SessionResource(v, r.requestId())));
    }
  }
}
