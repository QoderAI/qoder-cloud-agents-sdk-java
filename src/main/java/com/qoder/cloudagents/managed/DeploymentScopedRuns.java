package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class DeploymentScopedRuns {
  private final AsyncDeploymentScopedRuns async;

  public DeploymentScopedRuns(AsyncDeploymentScopedRuns async) {
    this.async = async;
  }

  public DeploymentRun retrieve(String deploymentId, String runId) {
    return retrieve(deploymentId, runId, RequestOptions.DEFAULT);
  }

  public DeploymentRun retrieve(String deploymentId, String runId, RequestOptions options) {
    return Futures.await(async.retrieve(deploymentId, runId, options));
  }

  public DeploymentScopedRunListResponse list(String deploymentId) {
    return list(deploymentId, DeploymentScopedRunListParams.builder().build());
  }

  public DeploymentScopedRunListResponse list(String deploymentId, RequestOptions options) {
    return list(deploymentId, DeploymentScopedRunListParams.builder().build(), options);
  }

  public DeploymentScopedRunListResponse list(
      String deploymentId, DeploymentScopedRunListParams params) {
    return list(deploymentId, params, RequestOptions.DEFAULT);
  }

  public DeploymentScopedRunListResponse list(
      String deploymentId, DeploymentScopedRunListParams params, RequestOptions options) {
    return Futures.await(async.list(deploymentId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<DeploymentRun> retrieve(String deploymentId, String runId) {
      return retrieve(deploymentId, runId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeploymentRun> retrieve(
        String deploymentId, String runId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(deploymentId, runId, options));
    }

    public ApiResponse<DeploymentScopedRunListResponse> list(String deploymentId) {
      return list(deploymentId, DeploymentScopedRunListParams.builder().build());
    }

    public ApiResponse<DeploymentScopedRunListResponse> list(
        String deploymentId, RequestOptions options) {
      return list(deploymentId, DeploymentScopedRunListParams.builder().build(), options);
    }

    public ApiResponse<DeploymentScopedRunListResponse> list(
        String deploymentId, DeploymentScopedRunListParams params) {
      return list(deploymentId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeploymentScopedRunListResponse> list(
        String deploymentId, DeploymentScopedRunListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(deploymentId, params, options));
    }
  }
}
