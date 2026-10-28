package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Environments {
  private final AsyncEnvironments async;

  public Environments(AsyncEnvironments async) {
    this.async = async;
  }

  public EnvironmentWork work() {
    return new EnvironmentWork(async.work());
  }

  public Environment archive(String environmentId) {
    return archive(environmentId, RequestOptions.DEFAULT);
  }

  public Environment archive(String environmentId, RequestOptions options) {
    return Futures.await(async.archive(environmentId, options));
  }

  public Environment create(EnvironmentCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Environment create(EnvironmentCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public DeletedEnvironment delete(String environmentId) {
    return delete(environmentId, RequestOptions.DEFAULT);
  }

  public DeletedEnvironment delete(String environmentId, RequestOptions options) {
    return Futures.await(async.delete(environmentId, options));
  }

  public Environment retrieve(String environmentId) {
    return retrieve(environmentId, RequestOptions.DEFAULT);
  }

  public Environment retrieve(String environmentId, RequestOptions options) {
    return Futures.await(async.retrieve(environmentId, options));
  }

  public EnvironmentListResponse list() {
    return list(EnvironmentListParams.builder().build());
  }

  public EnvironmentListResponse list(RequestOptions options) {
    return list(EnvironmentListParams.builder().build(), options);
  }

  public EnvironmentListResponse list(EnvironmentListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public EnvironmentListResponse list(EnvironmentListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public EnvironmentListResponse search() {
    return search(EnvironmentSearchParams.builder().build());
  }

  public EnvironmentListResponse search(RequestOptions options) {
    return search(EnvironmentSearchParams.builder().build(), options);
  }

  public EnvironmentListResponse search(EnvironmentSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public EnvironmentListResponse search(EnvironmentSearchParams params, RequestOptions options) {
    return Futures.await(async.search(params, options));
  }

  public Environment update(String environmentId, EnvironmentUpdateParams params) {
    return update(environmentId, params, RequestOptions.DEFAULT);
  }

  public Environment update(
      String environmentId, EnvironmentUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(environmentId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<Environment> archive(String environmentId) {
      return archive(environmentId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Environment> archive(String environmentId, RequestOptions options) {
      return Futures.await(async.withResponse().archive(environmentId, options));
    }

    public ApiResponse<Environment> create(EnvironmentCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Environment> create(EnvironmentCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<DeletedEnvironment> delete(String environmentId) {
      return delete(environmentId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedEnvironment> delete(String environmentId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(environmentId, options));
    }

    public ApiResponse<Environment> retrieve(String environmentId) {
      return retrieve(environmentId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Environment> retrieve(String environmentId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(environmentId, options));
    }

    public ApiResponse<EnvironmentListResponse> list() {
      return list(EnvironmentListParams.builder().build());
    }

    public ApiResponse<EnvironmentListResponse> list(RequestOptions options) {
      return list(EnvironmentListParams.builder().build(), options);
    }

    public ApiResponse<EnvironmentListResponse> list(EnvironmentListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<EnvironmentListResponse> list(
        EnvironmentListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<EnvironmentListResponse> search() {
      return search(EnvironmentSearchParams.builder().build());
    }

    public ApiResponse<EnvironmentListResponse> search(RequestOptions options) {
      return search(EnvironmentSearchParams.builder().build(), options);
    }

    public ApiResponse<EnvironmentListResponse> search(EnvironmentSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<EnvironmentListResponse> search(
        EnvironmentSearchParams params, RequestOptions options) {
      return Futures.await(async.withResponse().search(params, options));
    }

    public ApiResponse<Environment> update(String environmentId, EnvironmentUpdateParams params) {
      return update(environmentId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Environment> update(
        String environmentId, EnvironmentUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(environmentId, params, options));
    }
  }
}
