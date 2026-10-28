package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncDreams {
  private final ClientEngine engine;

  public AsyncDreams(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<Dream> create(DreamCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Dream> create(DreamCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<DreamListResponse> list(DreamListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DreamListResponse> list(DreamListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<Dream> retrieve(String dreamId) {
    return retrieve(dreamId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Dream> retrieve(String dreamId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(dreamId, options), ApiResponse::data);
  }

  public CompletableFuture<Dream> cancel(String dreamId) {
    return cancel(dreamId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Dream> cancel(String dreamId, RequestOptions options) {
    return Futures.map(withResponse().cancel(dreamId, options), ApiResponse::data);
  }

  public CompletableFuture<Dream> archive(String dreamId) {
    return archive(dreamId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Dream> archive(String dreamId, RequestOptions options) {
    return Futures.map(withResponse().archive(dreamId, options), ApiResponse::data);
  }

  public CompletableFuture<DreamListResponse> list() {
    return list(DreamListParams.builder().build());
  }

  public CompletableFuture<DreamListResponse> list(RequestOptions options) {
    return list(DreamListParams.builder().build(), options);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<DreamListResponse>> list() {
      return list(DreamListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<DreamListResponse>> list(RequestOptions options) {
      return list(DreamListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<Dream>> create(DreamCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Dream>> create(
        DreamCreateParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"dreams"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new Dream(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DreamListResponse>> list(DreamListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DreamListResponse>> list(
        DreamListParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return CursorRequests.list(
          engine, new String[] {"dreams"}, params.query(), options, DreamListResponse::new);
    }

    public CompletableFuture<ApiResponse<Dream>> retrieve(String dreamId) {
      return retrieve(dreamId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Dream>> retrieve(String dreamId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "GET", new String[] {"dreams", dreamId}, Collections.emptyMap(), null, true, options),
          r -> r.map(v -> new Dream(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Dream>> cancel(String dreamId) {
      return cancel(dreamId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Dream>> cancel(String dreamId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"dreams", dreamId, "cancel"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Dream(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Dream>> archive(String dreamId) {
      return archive(dreamId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Dream>> archive(String dreamId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"dreams", dreamId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Dream(v, r.requestId())));
    }
  }
}
