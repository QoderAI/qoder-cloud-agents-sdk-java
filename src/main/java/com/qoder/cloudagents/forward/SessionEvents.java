package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.streaming.EventStream;
import java.util.*;

public final class SessionEvents {
  private final AsyncSessionEvents async;

  public SessionEvents(AsyncSessionEvents async) {
    this.async = async;
  }

  public SessionEventListResponse list(String sessionId) {
    return list(sessionId, SessionEventListParams.builder().build());
  }

  public SessionEventListResponse list(String sessionId, SessionEventListParams params) {
    return list(sessionId, params, RequestOptions.DEFAULT);
  }

  public SessionEventListResponse list(
      String sessionId, SessionEventListParams params, RequestOptions options) {
    return Futures.await(async.list(sessionId, params, options));
  }

  public SessionEventSendResponse send(String sessionId, SessionEventSendParams params) {
    return send(sessionId, params, RequestOptions.DEFAULT);
  }

  public SessionEventSendResponse send(
      String sessionId, SessionEventSendParams params, RequestOptions options) {
    return Futures.await(async.send(sessionId, params, options));
  }

  /** Opens one SSE connection. Close the returned stream after consumption. */
  public EventStream<SessionEvent> streamEvents(String sessionId) {
    return streamEvents(sessionId, SessionEventStreamParams.builder().build());
  }

  public EventStream<SessionEvent> streamEvents(String sessionId, SessionEventStreamParams params) {
    return streamEvents(sessionId, params, RequestOptions.DEFAULT);
  }

  public EventStream<SessionEvent> streamEvents(
      String sessionId, SessionEventStreamParams params, RequestOptions options) {
    java.util.Objects.requireNonNull(params, "params");
    return Futures.await(async.streamEvents(sessionId, params, options));
  }

  /** Lazy, explicitly reconnecting subscription. It never resends user input. */
  public EventStream<SessionEvent> resumableStream(String sessionId) {
    return resumableStream(sessionId, SessionEventStreamParams.builder().build());
  }

  public EventStream<SessionEvent> resumableStream(
      String sessionId, SessionEventStreamParams params) {
    return resumableStream(sessionId, params, RequestOptions.DEFAULT);
  }

  public EventStream<SessionEvent> resumableStream(
      String sessionId, SessionEventStreamParams params, RequestOptions options) {
    java.util.Objects.requireNonNull(params, "params");
    return async.resumableStream(sessionId, params, options);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<SessionEventListResponse> list(String sessionId) {
      return list(sessionId, SessionEventListParams.builder().build());
    }

    public ApiResponse<SessionEventListResponse> list(
        String sessionId, SessionEventListParams params) {
      return list(sessionId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionEventListResponse> list(
        String sessionId, SessionEventListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(sessionId, params, options));
    }

    public ApiResponse<SessionEventSendResponse> send(
        String sessionId, SessionEventSendParams params) {
      return send(sessionId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionEventSendResponse> send(
        String sessionId, SessionEventSendParams params, RequestOptions options) {
      return Futures.await(async.withResponse().send(sessionId, params, options));
    }
  }
}
