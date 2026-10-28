package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class OAuth {
  private final AsyncOAuth async;

  public OAuth(AsyncOAuth async) {
    this.async = async;
  }

  public OAuthStartResponse start(OAuthStartParams params) {
    return start(params, RequestOptions.DEFAULT);
  }

  public OAuthStartResponse start(OAuthStartParams params, RequestOptions options) {
    return Futures.await(async.start(params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<OAuthStartResponse> start(OAuthStartParams params) {
      return start(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<OAuthStartResponse> start(OAuthStartParams params, RequestOptions options) {
      return Futures.await(async.withResponse().start(params, options));
    }
  }
}
