package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class IdentityServiceAccountTokens {
  private final AsyncIdentityServiceAccountTokens async;

  public IdentityServiceAccountTokens(AsyncIdentityServiceAccountTokens async) {
    this.async = async;
  }

  public ServiceAccountToken create(
      String identityId, String saKey, ServiceAccountTokenCreateParams params) {
    return create(identityId, saKey, params, RequestOptions.DEFAULT);
  }

  public ServiceAccountToken create(
      String identityId,
      String saKey,
      ServiceAccountTokenCreateParams params,
      RequestOptions options) {
    return Futures.await(async.create(identityId, saKey, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<ServiceAccountToken> create(
        String identityId, String saKey, ServiceAccountTokenCreateParams params) {
      return create(identityId, saKey, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<ServiceAccountToken> create(
        String identityId,
        String saKey,
        ServiceAccountTokenCreateParams params,
        RequestOptions options) {
      return Futures.await(async.withResponse().create(identityId, saKey, params, options));
    }
  }
}
