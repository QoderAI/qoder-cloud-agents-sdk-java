package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import com.qoder.cloudagents.streaming.EventStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncSessionEvents {
  private final ClientEngine engine;

  public AsyncSessionEvents(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<SessionEventListResponse> list(String sessionId) {
    return list(sessionId, SessionEventListParams.builder().build());
  }

  public CompletableFuture<SessionEventListResponse> list(
      String sessionId, SessionEventListParams params) {
    return list(sessionId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionEventListResponse> list(
      String sessionId, SessionEventListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(sessionId, params, options), ApiResponse::data);
  }

  public CompletableFuture<SessionEventSendResponse> send(
      String sessionId, SessionEventSendParams params) {
    return send(sessionId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SessionEventSendResponse> send(
      String sessionId, SessionEventSendParams params, RequestOptions options) {
    return Futures.map(withResponse().send(sessionId, params, options), ApiResponse::data);
  }

  /** Opens one SSE connection. Close the returned stream after consumption. */
  public CompletableFuture<EventStream<SessionEvent>> streamEvents(String sessionId) {
    return streamEvents(sessionId, SessionEventStreamParams.builder().build());
  }

  public CompletableFuture<EventStream<SessionEvent>> streamEvents(
      String sessionId, SessionEventStreamParams params) {
    return streamEvents(sessionId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<EventStream<SessionEvent>> streamEvents(
      String sessionId, SessionEventStreamParams params, RequestOptions options) {
    java.util.Objects.requireNonNull(params, "params");
    return SessionStreams.create(
            engine,
            new String[] {"sessions", sessionId, "events", "stream"},
            params.query(),
            params.lastEventId(),
            options,
            false,
            false)
        .openAsync();
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
    return SessionStreams.create(
        engine,
        new String[] {"sessions", sessionId, "events", "stream"},
        params.query(),
        params.lastEventId(),
        options,
        true,
        false);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<SessionEventListResponse>> list(String sessionId) {
      return list(sessionId, SessionEventListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<SessionEventListResponse>> list(
        String sessionId, SessionEventListParams params) {
      return list(sessionId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionEventListResponse>> list(
        String sessionId, SessionEventListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return CursorRequests.list(
          engine,
          new String[] {"sessions", sessionId, "events"},
          params.query(),
          options,
          SessionEventListResponse::new);
    }

    public CompletableFuture<ApiResponse<SessionEventSendResponse>> send(
        String sessionId, SessionEventSendParams params) {
      return send(sessionId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SessionEventSendResponse>> send(
        String sessionId, SessionEventSendParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"sessions", sessionId, "events"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response ->
              response.map(value -> new SessionEventSendResponse(value, response.requestId())));
    }
  }
}
