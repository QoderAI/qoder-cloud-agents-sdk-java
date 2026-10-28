package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Sessions {
  private final AsyncSessions async;

  public Sessions(AsyncSessions async) {
    this.async = async;
  }

  public SessionEvents events() {
    return new SessionEvents(async.events());
  }

  public SessionThreads threads() {
    return new SessionThreads(async.threads());
  }

  public SessionResources resources() {
    return new SessionResources(async.resources());
  }

  public Session archive(String sessionId) {
    return archive(sessionId, RequestOptions.DEFAULT);
  }

  public Session archive(String sessionId, RequestOptions options) {
    return Futures.await(async.archive(sessionId, options));
  }

  public Session cancel(String sessionId) {
    return cancel(sessionId, RequestOptions.DEFAULT);
  }

  public Session cancel(String sessionId, RequestOptions options) {
    return Futures.await(async.cancel(sessionId, options));
  }

  public Session create(SessionCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Session create(SessionCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public DeletedSession delete(String sessionId) {
    return delete(sessionId, RequestOptions.DEFAULT);
  }

  public DeletedSession delete(String sessionId, RequestOptions options) {
    return Futures.await(async.delete(sessionId, options));
  }

  public Session retrieve(String sessionId) {
    return retrieve(sessionId, RequestOptions.DEFAULT);
  }

  public Session retrieve(String sessionId, RequestOptions options) {
    return Futures.await(async.retrieve(sessionId, options));
  }

  public SessionListResponse list() {
    return list(SessionListParams.builder().build());
  }

  public SessionListResponse list(RequestOptions options) {
    return list(SessionListParams.builder().build(), options);
  }

  public SessionListResponse list(SessionListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public SessionListResponse list(SessionListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public SessionListResponse search() {
    return search(SessionSearchParams.builder().build());
  }

  public SessionListResponse search(RequestOptions options) {
    return search(SessionSearchParams.builder().build(), options);
  }

  public SessionListResponse search(SessionSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public SessionListResponse search(SessionSearchParams params, RequestOptions options) {
    return Futures.await(async.search(params, options));
  }

  public Session update(String sessionId, SessionUpdateParams params) {
    return update(sessionId, params, RequestOptions.DEFAULT);
  }

  public Session update(String sessionId, SessionUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(sessionId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<Session> archive(String sessionId) {
      return archive(sessionId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Session> archive(String sessionId, RequestOptions options) {
      return Futures.await(async.withResponse().archive(sessionId, options));
    }

    public ApiResponse<Session> cancel(String sessionId) {
      return cancel(sessionId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Session> cancel(String sessionId, RequestOptions options) {
      return Futures.await(async.withResponse().cancel(sessionId, options));
    }

    public ApiResponse<Session> create(SessionCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Session> create(SessionCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<DeletedSession> delete(String sessionId) {
      return delete(sessionId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedSession> delete(String sessionId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(sessionId, options));
    }

    public ApiResponse<Session> retrieve(String sessionId) {
      return retrieve(sessionId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Session> retrieve(String sessionId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(sessionId, options));
    }

    public ApiResponse<SessionListResponse> list() {
      return list(SessionListParams.builder().build());
    }

    public ApiResponse<SessionListResponse> list(RequestOptions options) {
      return list(SessionListParams.builder().build(), options);
    }

    public ApiResponse<SessionListResponse> list(SessionListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionListResponse> list(SessionListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<SessionListResponse> search() {
      return search(SessionSearchParams.builder().build());
    }

    public ApiResponse<SessionListResponse> search(RequestOptions options) {
      return search(SessionSearchParams.builder().build(), options);
    }

    public ApiResponse<SessionListResponse> search(SessionSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionListResponse> search(
        SessionSearchParams params, RequestOptions options) {
      return Futures.await(async.withResponse().search(params, options));
    }

    public ApiResponse<Session> update(String sessionId, SessionUpdateParams params) {
      return update(sessionId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Session> update(
        String sessionId, SessionUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(sessionId, params, options));
    }
  }
}
