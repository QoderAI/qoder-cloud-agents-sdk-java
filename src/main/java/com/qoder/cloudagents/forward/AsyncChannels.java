package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncChannels {
  private final ClientEngine engine;

  public AsyncChannels(ClientEngine engine) {
    this.engine = engine;
  }

  public AsyncChannelQRSessions qrSessions() {
    return new AsyncChannelQRSessions(engine);
  }

  public CompletableFuture<ChannelListResponse> list() {
    return list(ChannelListParams.builder().build());
  }

  public CompletableFuture<ChannelListResponse> list(RequestOptions options) {
    return list(ChannelListParams.builder().build(), options);
  }

  public CompletableFuture<ChannelListResponse> list(ChannelListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ChannelListResponse> list(
      ChannelListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<Channel> create(ChannelCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Channel> create(ChannelCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<Channel> retrieve(String channelId) {
    return retrieve(channelId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Channel> retrieve(String channelId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(channelId, options), ApiResponse::data);
  }

  public CompletableFuture<Channel> update(String channelId) {
    return update(channelId, ChannelUpdateParams.builder().build());
  }

  public CompletableFuture<Channel> update(String channelId, RequestOptions options) {
    return update(channelId, ChannelUpdateParams.builder().build(), options);
  }

  public CompletableFuture<Channel> update(String channelId, ChannelUpdateParams params) {
    return update(channelId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Channel> update(
      String channelId, ChannelUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(channelId, params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedChannel> delete(String channelId) {
    return delete(channelId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedChannel> delete(String channelId, RequestOptions options) {
    return Futures.map(withResponse().delete(channelId, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<ChannelListResponse>> list() {
      return list(ChannelListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<ChannelListResponse>> list(RequestOptions options) {
      return list(ChannelListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<ChannelListResponse>> list(ChannelListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ChannelListResponse>> list(
        ChannelListParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      if (options.query().containsKey("page"))
        throw new ConfigurationException("Channel pagination uses afterId or beforeId");
      return CursorRequests.list(
          engine, new String[] {"channels"}, params.query(), options, ChannelListResponse::new);
    }

    public CompletableFuture<ApiResponse<Channel>> create(ChannelCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Channel>> create(
        ChannelCreateParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"channels"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new Channel(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Channel>> retrieve(String channelId) {
      return retrieve(channelId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Channel>> retrieve(
        String channelId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"channels", channelId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Channel(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Channel>> update(String channelId) {
      return update(channelId, ChannelUpdateParams.builder().build());
    }

    public CompletableFuture<ApiResponse<Channel>> update(
        String channelId, RequestOptions options) {
      return update(channelId, ChannelUpdateParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<Channel>> update(
        String channelId, ChannelUpdateParams params) {
      return update(channelId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Channel>> update(
        String channelId, ChannelUpdateParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"channels", channelId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new Channel(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedChannel>> delete(String channelId) {
      return delete(channelId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedChannel>> delete(
        String channelId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"channels", channelId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedChannel(v, r.requestId())));
    }
  }
}
