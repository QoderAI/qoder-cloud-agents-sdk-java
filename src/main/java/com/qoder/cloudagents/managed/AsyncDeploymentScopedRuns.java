package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncDeploymentScopedRuns {
  private final ClientEngine engine;

  public AsyncDeploymentScopedRuns(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<DeploymentRun> retrieve(String deploymentId, String runId) {
    return retrieve(deploymentId, runId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeploymentRun> retrieve(
      String deploymentId, String runId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(deploymentId, runId, options), ApiResponse::data);
  }

  public CompletableFuture<DeploymentScopedRunListResponse> list(String deploymentId) {
    return list(deploymentId, DeploymentScopedRunListParams.builder().build());
  }

  public CompletableFuture<DeploymentScopedRunListResponse> list(
      String deploymentId, RequestOptions options) {
    return list(deploymentId, DeploymentScopedRunListParams.builder().build(), options);
  }

  public CompletableFuture<DeploymentScopedRunListResponse> list(
      String deploymentId, DeploymentScopedRunListParams params) {
    return list(deploymentId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeploymentScopedRunListResponse> list(
      String deploymentId, DeploymentScopedRunListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(deploymentId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<DeploymentRun>> retrieve(
        String deploymentId, String runId) {
      return retrieve(deploymentId, runId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeploymentRun>> retrieve(
        String deploymentId, String runId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"deployments", deploymentId, "runs", runId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeploymentRun(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeploymentScopedRunListResponse>> list(
        String deploymentId) {
      return list(deploymentId, DeploymentScopedRunListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<DeploymentScopedRunListResponse>> list(
        String deploymentId, RequestOptions options) {
      return list(deploymentId, DeploymentScopedRunListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<DeploymentScopedRunListResponse>> list(
        String deploymentId, DeploymentScopedRunListParams params) {
      return list(deploymentId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeploymentScopedRunListResponse>> list(
        String deploymentId, DeploymentScopedRunListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine,
          new String[] {"deployments", deploymentId, "runs"},
          params.query(),
          options,
          DeploymentScopedRunListResponse::new);
    }
  }
}
