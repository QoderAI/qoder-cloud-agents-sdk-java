package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class SessionResources {
  private final AsyncSessionResources async;

  public SessionResources(AsyncSessionResources async) {
    this.async = async;
  }

  public SessionResource create(String sessionId, SessionResourceCreateParams params) {
    return create(sessionId, params, RequestOptions.DEFAULT);
  }

  public SessionResource create(
      String sessionId, SessionResourceCreateParams params, RequestOptions options) {
    return Futures.await(async.create(sessionId, params, options));
  }

  public DeletedSessionResource delete(String sessionId, String resourceId) {
    return delete(sessionId, resourceId, RequestOptions.DEFAULT);
  }

  public DeletedSessionResource delete(
      String sessionId, String resourceId, RequestOptions options) {
    return Futures.await(async.delete(sessionId, resourceId, options));
  }

  public SessionResource retrieve(String sessionId, String resourceId) {
    return retrieve(sessionId, resourceId, RequestOptions.DEFAULT);
  }

  public SessionResource retrieve(String sessionId, String resourceId, RequestOptions options) {
    return Futures.await(async.retrieve(sessionId, resourceId, options));
  }

  public SessionResourceListResponse list(String sessionId) {
    return list(sessionId, SessionResourceListParams.builder().build());
  }

  public SessionResourceListResponse list(String sessionId, RequestOptions options) {
    return list(sessionId, SessionResourceListParams.builder().build(), options);
  }

  public SessionResourceListResponse list(String sessionId, SessionResourceListParams params) {
    return list(sessionId, params, RequestOptions.DEFAULT);
  }

  public SessionResourceListResponse list(
      String sessionId, SessionResourceListParams params, RequestOptions options) {
    return Futures.await(async.list(sessionId, params, options));
  }

  public SessionResource update(
      String sessionId, String resourceId, SessionResourceUpdateParams params) {
    return update(sessionId, resourceId, params, RequestOptions.DEFAULT);
  }

  public SessionResource update(
      String sessionId,
      String resourceId,
      SessionResourceUpdateParams params,
      RequestOptions options) {
    return Futures.await(async.update(sessionId, resourceId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<SessionResource> create(
        String sessionId, SessionResourceCreateParams params) {
      return create(sessionId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionResource> create(
        String sessionId, SessionResourceCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(sessionId, params, options));
    }

    public ApiResponse<DeletedSessionResource> delete(String sessionId, String resourceId) {
      return delete(sessionId, resourceId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedSessionResource> delete(
        String sessionId, String resourceId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(sessionId, resourceId, options));
    }

    public ApiResponse<SessionResource> retrieve(String sessionId, String resourceId) {
      return retrieve(sessionId, resourceId, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionResource> retrieve(
        String sessionId, String resourceId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(sessionId, resourceId, options));
    }

    public ApiResponse<SessionResourceListResponse> list(String sessionId) {
      return list(sessionId, SessionResourceListParams.builder().build());
    }

    public ApiResponse<SessionResourceListResponse> list(String sessionId, RequestOptions options) {
      return list(sessionId, SessionResourceListParams.builder().build(), options);
    }

    public ApiResponse<SessionResourceListResponse> list(
        String sessionId, SessionResourceListParams params) {
      return list(sessionId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionResourceListResponse> list(
        String sessionId, SessionResourceListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(sessionId, params, options));
    }

    public ApiResponse<SessionResource> update(
        String sessionId, String resourceId, SessionResourceUpdateParams params) {
      return update(sessionId, resourceId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionResource> update(
        String sessionId,
        String resourceId,
        SessionResourceUpdateParams params,
        RequestOptions options) {
      return Futures.await(async.withResponse().update(sessionId, resourceId, params, options));
    }
  }
}
