package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class SessionResources {
  private final AsyncSessionResources async;

  public SessionResources(AsyncSessionResources async) {
    this.async = async;
  }

  public SessionResource add(String sessionId, SessionResourceAddParams params) {
    return add(sessionId, params, RequestOptions.DEFAULT);
  }

  public SessionResource add(
      String sessionId, SessionResourceAddParams params, RequestOptions options) {
    return Futures.await(async.add(sessionId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<SessionResource> add(String sessionId, SessionResourceAddParams params) {
      return add(sessionId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SessionResource> add(
        String sessionId, SessionResourceAddParams params, RequestOptions options) {
      return Futures.await(async.withResponse().add(sessionId, params, options));
    }
  }
}
