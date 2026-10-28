package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Dreams {
  private final AsyncDreams async;

  public Dreams(AsyncDreams async) {
    this.async = async;
  }

  public Dream archive(String dreamId) {
    return archive(dreamId, RequestOptions.DEFAULT);
  }

  public Dream archive(String dreamId, RequestOptions options) {
    return Futures.await(async.archive(dreamId, options));
  }

  public Dream cancel(String dreamId) {
    return cancel(dreamId, RequestOptions.DEFAULT);
  }

  public Dream cancel(String dreamId, RequestOptions options) {
    return Futures.await(async.cancel(dreamId, options));
  }

  public Dream create(DreamCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Dream create(DreamCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public Dream retrieve(String dreamId) {
    return retrieve(dreamId, RequestOptions.DEFAULT);
  }

  public Dream retrieve(String dreamId, RequestOptions options) {
    return Futures.await(async.retrieve(dreamId, options));
  }

  public DreamListResponse list() {
    return list(DreamListParams.builder().build());
  }

  public DreamListResponse list(RequestOptions options) {
    return list(DreamListParams.builder().build(), options);
  }

  public DreamListResponse list(DreamListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public DreamListResponse list(DreamListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<Dream> archive(String dreamId) {
      return archive(dreamId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Dream> archive(String dreamId, RequestOptions options) {
      return Futures.await(async.withResponse().archive(dreamId, options));
    }

    public ApiResponse<Dream> cancel(String dreamId) {
      return cancel(dreamId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Dream> cancel(String dreamId, RequestOptions options) {
      return Futures.await(async.withResponse().cancel(dreamId, options));
    }

    public ApiResponse<Dream> create(DreamCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Dream> create(DreamCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<Dream> retrieve(String dreamId) {
      return retrieve(dreamId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Dream> retrieve(String dreamId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(dreamId, options));
    }

    public ApiResponse<DreamListResponse> list() {
      return list(DreamListParams.builder().build());
    }

    public ApiResponse<DreamListResponse> list(RequestOptions options) {
      return list(DreamListParams.builder().build(), options);
    }

    public ApiResponse<DreamListResponse> list(DreamListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<DreamListResponse> list(DreamListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }
  }
}
