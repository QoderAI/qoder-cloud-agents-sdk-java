package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncChannelQRSessions {
  private final ClientEngine engine;

  public AsyncChannelQRSessions(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<ChannelQRSession> create(String channelId) {
    return create(channelId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ChannelQRSession> create(String channelId, RequestOptions options) {
    return Futures.map(withResponse().create(channelId, options), ApiResponse::data);
  }

  public CompletableFuture<ChannelQRSession> retrieve(String sessionKey) {
    return retrieve(sessionKey, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ChannelQRSession> retrieve(String sessionKey, RequestOptions options) {
    return Futures.map(withResponse().retrieve(sessionKey, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<ChannelQRSession>> create(String channelId) {
      return create(channelId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ChannelQRSession>> create(
        String channelId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"channels", channelId, "qr_sessions"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new ChannelQRSession(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<ChannelQRSession>> retrieve(String sessionKey) {
      return retrieve(sessionKey, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ChannelQRSession>> retrieve(
        String sessionKey, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"qr_sessions", sessionKey},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new ChannelQRSession(v, r.requestId())));
    }
  }
}
