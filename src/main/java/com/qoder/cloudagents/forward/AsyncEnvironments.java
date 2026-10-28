package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public final class AsyncEnvironments {
  private final ClientEngine engine;

  public AsyncEnvironments(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<EnvironmentListResponse> list() {
    return list(EnvironmentListParams.builder().build());
  }

  public CompletableFuture<EnvironmentListResponse> list(EnvironmentListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<EnvironmentListResponse> list(
      EnvironmentListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<Environment> create(EnvironmentCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Environment> create(
      EnvironmentCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<Environment> retrieve(String environmentId) {
    return retrieve(environmentId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Environment> retrieve(String environmentId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(environmentId, options), ApiResponse::data);
  }

  public CompletableFuture<Environment> update(
      String environmentId, EnvironmentUpdateParams params) {
    return update(environmentId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Environment> update(
      String environmentId, EnvironmentUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(environmentId, params, options), ApiResponse::data);
  }

  public CompletableFuture<Environment> archive(String environmentId) {
    return archive(environmentId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Environment> archive(String environmentId, RequestOptions options) {
    return Futures.map(withResponse().archive(environmentId, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedEnvironment> delete(String environmentId) {
    return delete(environmentId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedEnvironment> delete(
      String environmentId, RequestOptions options) {
    return Futures.map(withResponse().delete(environmentId, options), ApiResponse::data);
  }

  public CompletableFuture<EnvironmentListResponse> search(EnvironmentSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<EnvironmentListResponse> search(
      EnvironmentSearchParams params, RequestOptions options) {
    return Futures.map(withResponse().search(params, options), ApiResponse::data);
  }

  public CompletableFuture<EnvironmentListResponse> search() {
    return search(EnvironmentSearchParams.builder().build());
  }

  public CompletableFuture<EnvironmentListResponse> search(RequestOptions options) {
    return search(EnvironmentSearchParams.builder().build(), options);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<EnvironmentListResponse>> search() {
      return search(EnvironmentSearchParams.builder().build());
    }

    public CompletableFuture<ApiResponse<EnvironmentListResponse>> search(RequestOptions options) {
      return search(EnvironmentSearchParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<EnvironmentListResponse>> search(
        EnvironmentSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<EnvironmentListResponse>> search(
        EnvironmentSearchParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return SearchRequests.search(
          engine,
          new String[] {"environments", "search"},
          params.toJsonValue(),
          params.query(),
          options,
          EnvironmentListResponse::new);
    }

    public CompletableFuture<ApiResponse<EnvironmentListResponse>> list() {
      return list(EnvironmentListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<EnvironmentListResponse>> list(
        EnvironmentListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<EnvironmentListResponse>> list(
        EnvironmentListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("List parameters are required");
      return CursorRequests.list(
          engine,
          new String[] {"environments"},
          params.query(),
          options,
          EnvironmentListResponse::new);
    }

    public CompletableFuture<ApiResponse<Environment>> create(EnvironmentCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Environment>> create(
        EnvironmentCreateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"environments"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new Environment(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Environment>> retrieve(String environmentId) {
      return retrieve(environmentId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Environment>> retrieve(
        String environmentId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"environments", environmentId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Environment(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Environment>> update(
        String environmentId, EnvironmentUpdateParams params) {
      return update(environmentId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Environment>> update(
        String environmentId, EnvironmentUpdateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"environments", environmentId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new Environment(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Environment>> archive(String environmentId) {
      return archive(environmentId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Environment>> archive(
        String environmentId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"environments", environmentId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Environment(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedEnvironment>> delete(String environmentId) {
      return delete(environmentId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedEnvironment>> delete(
        String environmentId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"environments", environmentId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedEnvironment(v, r.requestId())));
    }
  }
}
