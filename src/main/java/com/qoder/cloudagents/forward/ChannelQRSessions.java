package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class ChannelQRSessions {
  private final AsyncChannelQRSessions async;

  public ChannelQRSessions(AsyncChannelQRSessions async) {
    this.async = async;
  }

  public ChannelQRSession create(String channelId) {
    return create(channelId, RequestOptions.DEFAULT);
  }

  public ChannelQRSession create(String channelId, RequestOptions options) {
    return Futures.await(async.create(channelId, options));
  }

  public ChannelQRSession retrieve(String sessionKey) {
    return retrieve(sessionKey, RequestOptions.DEFAULT);
  }

  public ChannelQRSession retrieve(String sessionKey, RequestOptions options) {
    return Futures.await(async.retrieve(sessionKey, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<ChannelQRSession> create(String channelId) {
      return create(channelId, RequestOptions.DEFAULT);
    }

    public ApiResponse<ChannelQRSession> create(String channelId, RequestOptions options) {
      return Futures.await(async.withResponse().create(channelId, options));
    }

    public ApiResponse<ChannelQRSession> retrieve(String sessionKey) {
      return retrieve(sessionKey, RequestOptions.DEFAULT);
    }

    public ApiResponse<ChannelQRSession> retrieve(String sessionKey, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(sessionKey, options));
    }
  }
}
