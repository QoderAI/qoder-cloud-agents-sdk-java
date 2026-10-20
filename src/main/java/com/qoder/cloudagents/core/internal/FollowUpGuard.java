package com.qoder.cloudagents.core.internal;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * OkHttp still follows 503/Retry-After:0 and coalesced 421 when connection retries are disabled.
 */
final class FollowUpGuard {
  private int code;
  private String message;

  static final Interceptor APPLICATION =
      chain -> {
        FollowUpGuard state = new FollowUpGuard();
        Response response =
            chain.proceed(chain.request().newBuilder().tag(FollowUpGuard.class, state).build());
        return state.code == 0
            ? response
            : response.newBuilder().code(state.code).message(state.message).build();
      };
  static final Interceptor NETWORK =
      chain -> {
        Response response = chain.proceed(chain.request());
        if (response.code() == 503 || response.code() == 421) {
          FollowUpGuard state = chain.request().tag(FollowUpGuard.class);
          if (state == null) {
            response.close();
            throw new IOException("Missing retry guard");
          }
          state.code = response.code();
          state.message = response.message();
          // Hide only from OkHttp's follow-up loop, then restore before the SDK observes it.
          return response
              .newBuilder()
              .code(400)
              .message("SDK retry policy owns this response")
              .build();
        }
        return response;
      };
}
