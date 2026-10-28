package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class DeploymentRuns {
  private final AsyncDeploymentRuns async;

  public DeploymentRuns(AsyncDeploymentRuns async) {
    this.async = async;
  }

  public DeploymentRun retrieve(String runId) {
    return retrieve(runId, RequestOptions.DEFAULT);
  }

  public DeploymentRun retrieve(String runId, RequestOptions options) {
    return Futures.await(async.retrieve(runId, options));
  }

  public DeploymentRunListResponse list() {
    return list(DeploymentRunListParams.builder().build());
  }

  public DeploymentRunListResponse list(RequestOptions options) {
    return list(DeploymentRunListParams.builder().build(), options);
  }

  public DeploymentRunListResponse list(DeploymentRunListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public DeploymentRunListResponse list(DeploymentRunListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<DeploymentRun> retrieve(String runId) {
      return retrieve(runId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeploymentRun> retrieve(String runId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(runId, options));
    }

    public ApiResponse<DeploymentRunListResponse> list() {
      return list(DeploymentRunListParams.builder().build());
    }

    public ApiResponse<DeploymentRunListResponse> list(RequestOptions options) {
      return list(DeploymentRunListParams.builder().build(), options);
    }

    public ApiResponse<DeploymentRunListResponse> list(DeploymentRunListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeploymentRunListResponse> list(
        DeploymentRunListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }
  }
}
