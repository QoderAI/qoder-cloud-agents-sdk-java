package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncDeployments {
  private final ClientEngine engine;

  public AsyncDeployments(ClientEngine engine) {
    this.engine = engine;
  }

  public AsyncDeploymentScopedRuns runs() {
    return new AsyncDeploymentScopedRuns(engine);
  }

  public CompletableFuture<Deployment> archive(String deploymentId) {
    return archive(deploymentId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Deployment> archive(String deploymentId, RequestOptions options) {
    return Futures.map(withResponse().archive(deploymentId, options), ApiResponse::data);
  }

  public CompletableFuture<Deployment> create(DeploymentCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Deployment> create(
      DeploymentCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<Deployment> retrieve(String deploymentId) {
    return retrieve(deploymentId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Deployment> retrieve(String deploymentId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(deploymentId, options), ApiResponse::data);
  }

  public CompletableFuture<DeploymentListResponse> list() {
    return list(DeploymentListParams.builder().build());
  }

  public CompletableFuture<DeploymentListResponse> list(RequestOptions options) {
    return list(DeploymentListParams.builder().build(), options);
  }

  public CompletableFuture<DeploymentListResponse> list(DeploymentListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeploymentListResponse> list(
      DeploymentListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<Deployment> pause(String deploymentId) {
    return pause(deploymentId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Deployment> pause(String deploymentId, RequestOptions options) {
    return Futures.map(withResponse().pause(deploymentId, options), ApiResponse::data);
  }

  public CompletableFuture<DeploymentRun> run(String deploymentId) {
    return run(deploymentId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeploymentRun> run(String deploymentId, RequestOptions options) {
    return Futures.map(withResponse().run(deploymentId, options), ApiResponse::data);
  }

  public CompletableFuture<DeploymentListResponse> search() {
    return search(DeploymentSearchParams.builder().build());
  }

  public CompletableFuture<DeploymentListResponse> search(RequestOptions options) {
    return search(DeploymentSearchParams.builder().build(), options);
  }

  public CompletableFuture<DeploymentListResponse> search(DeploymentSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeploymentListResponse> search(
      DeploymentSearchParams params, RequestOptions options) {
    return Futures.map(withResponse().search(params, options), ApiResponse::data);
  }

  public CompletableFuture<Deployment> unpause(String deploymentId) {
    return unpause(deploymentId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Deployment> unpause(String deploymentId, RequestOptions options) {
    return Futures.map(withResponse().unpause(deploymentId, options), ApiResponse::data);
  }

  public CompletableFuture<Deployment> update(String deploymentId, DeploymentUpdateParams params) {
    return update(deploymentId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Deployment> update(
      String deploymentId, DeploymentUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(deploymentId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<Deployment>> archive(String deploymentId) {
      return archive(deploymentId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Deployment>> archive(
        String deploymentId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"deployments", deploymentId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Deployment(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Deployment>> create(DeploymentCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Deployment>> create(
        DeploymentCreateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"deployments"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Deployment(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Deployment>> retrieve(String deploymentId) {
      return retrieve(deploymentId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Deployment>> retrieve(
        String deploymentId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"deployments", deploymentId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Deployment(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeploymentListResponse>> list() {
      return list(DeploymentListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<DeploymentListResponse>> list(RequestOptions options) {
      return list(DeploymentListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<DeploymentListResponse>> list(
        DeploymentListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeploymentListResponse>> list(
        DeploymentListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine,
          new String[] {"deployments"},
          params.query(),
          options,
          DeploymentListResponse::new);
    }

    public CompletableFuture<ApiResponse<Deployment>> pause(String deploymentId) {
      return pause(deploymentId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Deployment>> pause(
        String deploymentId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"deployments", deploymentId, "pause"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Deployment(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeploymentRun>> run(String deploymentId) {
      return run(deploymentId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeploymentRun>> run(
        String deploymentId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"deployments", deploymentId, "run"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeploymentRun(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeploymentListResponse>> search() {
      return search(DeploymentSearchParams.builder().build());
    }

    public CompletableFuture<ApiResponse<DeploymentListResponse>> search(RequestOptions options) {
      return search(DeploymentSearchParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<DeploymentListResponse>> search(
        DeploymentSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeploymentListResponse>> search(
        DeploymentSearchParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return SearchRequests.search(
          engine,
          new String[] {"deployments", "search"},
          params.toJsonValue(),
          Collections.emptyMap(),
          options,
          DeploymentListResponse::new);
    }

    public CompletableFuture<ApiResponse<Deployment>> unpause(String deploymentId) {
      return unpause(deploymentId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Deployment>> unpause(
        String deploymentId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"deployments", deploymentId, "unpause"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Deployment(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Deployment>> update(
        String deploymentId, DeploymentUpdateParams params) {
      return update(deploymentId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Deployment>> update(
        String deploymentId, DeploymentUpdateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"deployments", deploymentId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Deployment(v, r.requestId())));
    }
  }
}
