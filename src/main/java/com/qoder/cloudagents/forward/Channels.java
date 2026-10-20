package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Channels {
  private final AsyncChannels async;

  public Channels(AsyncChannels async) {
    this.async = async;
  }

  public ChannelQRSessions qrSessions() {
    return new ChannelQRSessions(async.qrSessions());
  }

  public ChannelListResponse list() {
    return list(ChannelListParams.builder().build());
  }

  public ChannelListResponse list(RequestOptions options) {
    return list(ChannelListParams.builder().build(), options);
  }

  public ChannelListResponse list(ChannelListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public ChannelListResponse list(ChannelListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public Channel create(ChannelCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Channel create(ChannelCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public Channel retrieve(String channelId) {
    return retrieve(channelId, RequestOptions.DEFAULT);
  }

  public Channel retrieve(String channelId, RequestOptions options) {
    return Futures.await(async.retrieve(channelId, options));
  }

  public Channel update(String channelId) {
    return update(channelId, ChannelUpdateParams.builder().build());
  }

  public Channel update(String channelId, RequestOptions options) {
    return update(channelId, ChannelUpdateParams.builder().build(), options);
  }

  public Channel update(String channelId, ChannelUpdateParams params) {
    return update(channelId, params, RequestOptions.DEFAULT);
  }

  public Channel update(String channelId, ChannelUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(channelId, params, options));
  }

  public DeletedChannel delete(String channelId) {
    return delete(channelId, RequestOptions.DEFAULT);
  }

  public DeletedChannel delete(String channelId, RequestOptions options) {
    return Futures.await(async.delete(channelId, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<ChannelListResponse> list() {
      return list(ChannelListParams.builder().build());
    }

    public ApiResponse<ChannelListResponse> list(RequestOptions options) {
      return list(ChannelListParams.builder().build(), options);
    }

    public ApiResponse<ChannelListResponse> list(ChannelListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<ChannelListResponse> list(ChannelListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<Channel> create(ChannelCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Channel> create(ChannelCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<Channel> retrieve(String channelId) {
      return retrieve(channelId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Channel> retrieve(String channelId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(channelId, options));
    }

    public ApiResponse<Channel> update(String channelId) {
      return update(channelId, ChannelUpdateParams.builder().build());
    }

    public ApiResponse<Channel> update(String channelId, RequestOptions options) {
      return update(channelId, ChannelUpdateParams.builder().build(), options);
    }

    public ApiResponse<Channel> update(String channelId, ChannelUpdateParams params) {
      return update(channelId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Channel> update(
        String channelId, ChannelUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(channelId, params, options));
    }

    public ApiResponse<DeletedChannel> delete(String channelId) {
      return delete(channelId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedChannel> delete(String channelId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(channelId, options));
    }
  }
}
