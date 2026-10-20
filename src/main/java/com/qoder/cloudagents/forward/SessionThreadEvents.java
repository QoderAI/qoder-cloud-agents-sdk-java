package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.streaming.EventStream;
import java.util.*;

public final class SessionThreadEvents {
  private final AsyncSessionThreadEvents async;

  public SessionThreadEvents(AsyncSessionThreadEvents async) {
    this.async = async;
  }

  public SessionEventListResponse list(String sessionId, String threadId) {
    return list(sessionId, threadId, SessionThreadEventListParams.builder().build());
  }

  public SessionEventListResponse list(
      String sessionId, String threadId, SessionThreadEventListParams params) {
    return list(sessionId, threadId, params, RequestOptions.DEFAULT);
  }

  public SessionEventListResponse list(
      String sessionId,
      String threadId,
      SessionThreadEventListParams params,
      RequestOptions options) {
    return Futures.await(async.list(sessionId, threadId, params, options));
  }

  /** Opens one SSE connection. Close the returned stream after consumption. */
  public EventStream<SessionEvent> streamEvents(String sessionId, String threadId) {
    return streamEvents(sessionId, threadId, SessionThreadEventStreamParams.builder().build());
  }

  public EventStream<SessionEvent> streamEvents(
      String sessionId, String threadId, SessionThreadEventStreamParams params) {
    return streamEvents(sessionId, threadId, params, RequestOptions.DEFAULT);
  }

  public EventStream<SessionEvent> streamEvents(
      String sessionId,
      String threadId,
      SessionThreadEventStreamParams params,
      RequestOptions options) {
    java.util.Objects.requireNonNull(params, "params");
    return Futures.await(async.streamEvents(sessionId, threadId, params, options));
  }

  /** Lazy, explicitly reconnecting subscription. It never resends user input. */
  public EventStream<SessionEvent> resumableStream(String sessionId, String threadId) {
    return resumableStream(sessionId, threadId, SessionThreadEventStreamParams.builder().build());
  }

  public EventStream<SessionEvent> resumableStream(
      String sessionId, String threadId, SessionThreadEventStreamParams params) {
    return resumableStream(sessionId, threadId, params, RequestOptions.DEFAULT);
  }

  public EventStream<SessionEvent> resumableStream(
      String sessionId,
      String threadId,
      SessionThreadEventStreamParams params,
      RequestOptions options) {
    java.util.Objects.requireNonNull(params, "params");
    return async.resumableStream(sessionId, threadId, params, options);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<SessionEventListResponse> list(String sessionId, String threadId) {
      return list(sessionId, threadId, SessionThreadEventListParams.builder().build());
    }

    public ApiResponse<SessionEventListResponse> list(
        String sessionId, String threadId, SessionThreadEventListParams params) {
      return list(sessionId, threadId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionEventListResponse> list(
        String sessionId,
        String threadId,
        SessionThreadEventListParams params,
        RequestOptions options) {
      return Futures.await(async.withResponse().list(sessionId, threadId, params, options));
    }
  }
}
