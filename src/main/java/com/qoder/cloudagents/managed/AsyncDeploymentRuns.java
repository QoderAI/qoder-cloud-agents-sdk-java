package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncDeploymentRuns {
  private final ClientEngine engine;

  public AsyncDeploymentRuns(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<DeploymentRun> retrieve(String runId) {
    return retrieve(runId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeploymentRun> retrieve(String runId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(runId, options), ApiResponse::data);
  }

  public CompletableFuture<DeploymentRunListResponse> list() {
    return list(DeploymentRunListParams.builder().build());
  }

  public CompletableFuture<DeploymentRunListResponse> list(RequestOptions options) {
    return list(DeploymentRunListParams.builder().build(), options);
  }

  public CompletableFuture<DeploymentRunListResponse> list(DeploymentRunListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeploymentRunListResponse> list(
      DeploymentRunListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<DeploymentRun>> retrieve(String runId) {
      return retrieve(runId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeploymentRun>> retrieve(
        String runId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"deployment_runs", runId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeploymentRun(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeploymentRunListResponse>> list() {
      return list(DeploymentRunListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<DeploymentRunListResponse>> list(RequestOptions options) {
      return list(DeploymentRunListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<DeploymentRunListResponse>> list(
        DeploymentRunListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeploymentRunListResponse>> list(
        DeploymentRunListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine,
          new String[] {"deployment_runs"},
          params.query(),
          options,
          DeploymentRunListResponse::new);
    }
  }
}
