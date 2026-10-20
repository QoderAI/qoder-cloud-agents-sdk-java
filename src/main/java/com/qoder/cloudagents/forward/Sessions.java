package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Sessions {
  private final AsyncSessions async;
  private final SessionEvents events;
  private final SessionResources resources;
  private final SessionThreads threads;

  public Sessions(AsyncSessions async) {
    this.async = async;
    events = new SessionEvents(async.events());
    resources = new SessionResources(async.resources());
    threads = new SessionThreads(async.threads());
  }

  public SessionEvents events() {
    return events;
  }

  public SessionResources resources() {
    return resources;
  }

  public SessionThreads threads() {
    return threads;
  }

  public SessionListResponse list() {
    return list(SessionListParams.builder().build());
  }

  public SessionListResponse list(SessionListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public SessionListResponse list(SessionListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public Session create(SessionCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Session create(SessionCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public Session retrieve(String sessionId) {
    return retrieve(sessionId, RequestOptions.DEFAULT);
  }

  public Session retrieve(String sessionId, RequestOptions options) {
    return Futures.await(async.retrieve(sessionId, options));
  }

  public Session update(String sessionId, SessionUpdateParams params) {
    return update(sessionId, params, RequestOptions.DEFAULT);
  }

  public Session update(String sessionId, SessionUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(sessionId, params, options));
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

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<SessionListResponse> list() {
      return list(SessionListParams.builder().build());
    }

    public ApiResponse<SessionListResponse> list(SessionListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionListResponse> list(SessionListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<Session> create(SessionCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Session> create(SessionCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<Session> retrieve(String sessionId) {
      return retrieve(sessionId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Session> retrieve(String sessionId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(sessionId, options));
    }

    public ApiResponse<Session> update(String sessionId, SessionUpdateParams params) {
      return update(sessionId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Session> update(
        String sessionId, SessionUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(sessionId, params, options));
    }

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
  }
}
