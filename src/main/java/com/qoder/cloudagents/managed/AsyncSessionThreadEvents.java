package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import com.qoder.cloudagents.streaming.EventStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncSessionThreadEvents {
  private final ClientEngine engine;

  public AsyncSessionThreadEvents(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<SessionEventListResponse> list(String sessionId, String threadId) {
    return list(sessionId, threadId, SessionThreadEventListParams.builder().build());
  }

  public CompletableFuture<SessionEventListResponse> list(
      String sessionId, String threadId, SessionThreadEventListParams params) {
    return list(sessionId, threadId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionEventListResponse> list(
      String sessionId,
      String threadId,
      SessionThreadEventListParams params,
      RequestOptions options) {
    return Futures.map(
        withResponse().list(sessionId, threadId, params, options), ApiResponse::data);
  }

  /** Opens one SSE connection. Close the returned stream after consumption. */
  public CompletableFuture<EventStream<SessionEvent>> streamEvents(
      String sessionId, String threadId) {
    return streamEvents(sessionId, threadId, SessionThreadEventStreamParams.builder().build());
  }

  public CompletableFuture<EventStream<SessionEvent>> streamEvents(
      String sessionId, String threadId, SessionThreadEventStreamParams params) {
    return streamEvents(sessionId, threadId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<EventStream<SessionEvent>> streamEvents(
      String sessionId,
      String threadId,
      SessionThreadEventStreamParams params,
      RequestOptions options) {
    java.util.Objects.requireNonNull(params, "params");
    return SessionStreams.create(
            engine,
            new String[] {"sessions", sessionId, "threads", threadId, "stream"},
            params.query(),
            params.lastEventId(),
            options,
            false,
            true)
        .openAsync();
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
    return SessionStreams.create(
        engine,
        new String[] {"sessions", sessionId, "threads", threadId, "stream"},
        params.query(),
        params.lastEventId(),
        options,
        true,
        true);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<SessionEventListResponse>> list(
        String sessionId, String threadId) {
      return list(sessionId, threadId, SessionThreadEventListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<SessionEventListResponse>> list(
        String sessionId, String threadId, SessionThreadEventListParams params) {
      return list(sessionId, threadId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionEventListResponse>> list(
        String sessionId,
        String threadId,
        SessionThreadEventListParams params,
        RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return CursorRequests.list(
          engine,
          new String[] {"sessions", sessionId, "threads", threadId, "events"},
          params.query(),
          options,
          SessionEventListResponse::new);
    }
  }
}
