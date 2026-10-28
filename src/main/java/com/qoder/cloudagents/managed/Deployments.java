package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Deployments {
  private final AsyncDeployments async;

  public Deployments(AsyncDeployments async) {
    this.async = async;
  }

  public DeploymentScopedRuns runs() {
    return new DeploymentScopedRuns(async.runs());
  }

  public Deployment archive(String deploymentId) {
    return archive(deploymentId, RequestOptions.DEFAULT);
  }

  public Deployment archive(String deploymentId, RequestOptions options) {
    return Futures.await(async.archive(deploymentId, options));
  }

  public Deployment create(DeploymentCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Deployment create(DeploymentCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public Deployment retrieve(String deploymentId) {
    return retrieve(deploymentId, RequestOptions.DEFAULT);
  }

  public Deployment retrieve(String deploymentId, RequestOptions options) {
    return Futures.await(async.retrieve(deploymentId, options));
  }

  public DeploymentListResponse list() {
    return list(DeploymentListParams.builder().build());
  }

  public DeploymentListResponse list(RequestOptions options) {
    return list(DeploymentListParams.builder().build(), options);
  }

  public DeploymentListResponse list(DeploymentListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public DeploymentListResponse list(DeploymentListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public Deployment pause(String deploymentId) {
    return pause(deploymentId, RequestOptions.DEFAULT);
  }

  public Deployment pause(String deploymentId, RequestOptions options) {
    return Futures.await(async.pause(deploymentId, options));
  }

  public DeploymentRun run(String deploymentId) {
    return run(deploymentId, RequestOptions.DEFAULT);
  }

  public DeploymentRun run(String deploymentId, RequestOptions options) {
    return Futures.await(async.run(deploymentId, options));
  }

  public DeploymentListResponse search() {
    return search(DeploymentSearchParams.builder().build());
  }

  public DeploymentListResponse search(RequestOptions options) {
    return search(DeploymentSearchParams.builder().build(), options);
  }

  public DeploymentListResponse search(DeploymentSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public DeploymentListResponse search(DeploymentSearchParams params, RequestOptions options) {
    return Futures.await(async.search(params, options));
  }

  public Deployment unpause(String deploymentId) {
    return unpause(deploymentId, RequestOptions.DEFAULT);
  }

  public Deployment unpause(String deploymentId, RequestOptions options) {
    return Futures.await(async.unpause(deploymentId, options));
  }

  public Deployment update(String deploymentId, DeploymentUpdateParams params) {
    return update(deploymentId, params, RequestOptions.DEFAULT);
  }

  public Deployment update(
      String deploymentId, DeploymentUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(deploymentId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<Deployment> archive(String deploymentId) {
      return archive(deploymentId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Deployment> archive(String deploymentId, RequestOptions options) {
      return Futures.await(async.withResponse().archive(deploymentId, options));
    }

    public ApiResponse<Deployment> create(DeploymentCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Deployment> create(DeploymentCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<Deployment> retrieve(String deploymentId) {
      return retrieve(deploymentId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Deployment> retrieve(String deploymentId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(deploymentId, options));
    }

    public ApiResponse<DeploymentListResponse> list() {
      return list(DeploymentListParams.builder().build());
    }

    public ApiResponse<DeploymentListResponse> list(RequestOptions options) {
      return list(DeploymentListParams.builder().build(), options);
    }

    public ApiResponse<DeploymentListResponse> list(DeploymentListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeploymentListResponse> list(
        DeploymentListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<Deployment> pause(String deploymentId) {
      return pause(deploymentId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Deployment> pause(String deploymentId, RequestOptions options) {
      return Futures.await(async.withResponse().pause(deploymentId, options));
    }

    public ApiResponse<DeploymentRun> run(String deploymentId) {
      return run(deploymentId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeploymentRun> run(String deploymentId, RequestOptions options) {
      return Futures.await(async.withResponse().run(deploymentId, options));
    }

    public ApiResponse<DeploymentListResponse> search() {
      return search(DeploymentSearchParams.builder().build());
    }

    public ApiResponse<DeploymentListResponse> search(RequestOptions options) {
      return search(DeploymentSearchParams.builder().build(), options);
    }

    public ApiResponse<DeploymentListResponse> search(DeploymentSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeploymentListResponse> search(
        DeploymentSearchParams params, RequestOptions options) {
      return Futures.await(async.withResponse().search(params, options));
    }

    public ApiResponse<Deployment> unpause(String deploymentId) {
      return unpause(deploymentId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Deployment> unpause(String deploymentId, RequestOptions options) {
      return Futures.await(async.withResponse().unpause(deploymentId, options));
    }

    public ApiResponse<Deployment> update(String deploymentId, DeploymentUpdateParams params) {
      return update(deploymentId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Deployment> update(
        String deploymentId, DeploymentUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(deploymentId, params, options));
    }
  }
}
