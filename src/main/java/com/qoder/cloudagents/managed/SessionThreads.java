package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class SessionThreads {
  private final AsyncSessionThreads async;

  public SessionThreads(AsyncSessionThreads async) {
    this.async = async;
  }

  public SessionThreadEvents events() {
    return new SessionThreadEvents(async.events());
  }

  public SessionThread archive(String sessionId, String threadId) {
    return archive(sessionId, threadId, RequestOptions.DEFAULT);
  }

  public SessionThread archive(String sessionId, String threadId, RequestOptions options) {
    return Futures.await(async.archive(sessionId, threadId, options));
  }

  public SessionThread retrieve(String sessionId, String threadId) {
    return retrieve(sessionId, threadId, RequestOptions.DEFAULT);
  }

  public SessionThread retrieve(String sessionId, String threadId, RequestOptions options) {
    return Futures.await(async.retrieve(sessionId, threadId, options));
  }

  public SessionThreadListResponse list(String sessionId) {
    return list(sessionId, SessionThreadListParams.builder().build());
  }

  public SessionThreadListResponse list(String sessionId, RequestOptions options) {
    return list(sessionId, SessionThreadListParams.builder().build(), options);
  }

  public SessionThreadListResponse list(String sessionId, SessionThreadListParams params) {
    return list(sessionId, params, RequestOptions.DEFAULT);
  }

  public SessionThreadListResponse list(
      String sessionId, SessionThreadListParams params, RequestOptions options) {
    return Futures.await(async.list(sessionId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<SessionThread> archive(String sessionId, String threadId) {
      return archive(sessionId, threadId, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionThread> archive(
        String sessionId, String threadId, RequestOptions options) {
      return Futures.await(async.withResponse().archive(sessionId, threadId, options));
    }

    public ApiResponse<SessionThread> retrieve(String sessionId, String threadId) {
      return retrieve(sessionId, threadId, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionThread> retrieve(
        String sessionId, String threadId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(sessionId, threadId, options));
    }

    public ApiResponse<SessionThreadListResponse> list(String sessionId) {
      return list(sessionId, SessionThreadListParams.builder().build());
    }

    public ApiResponse<SessionThreadListResponse> list(String sessionId, RequestOptions options) {
      return list(sessionId, SessionThreadListParams.builder().build(), options);
    }

    public ApiResponse<SessionThreadListResponse> list(
        String sessionId, SessionThreadListParams params) {
      return list(sessionId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionThreadListResponse> list(
        String sessionId, SessionThreadListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(sessionId, params, options));
    }
  }
}
