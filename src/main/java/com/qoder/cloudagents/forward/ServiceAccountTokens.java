package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class ServiceAccountTokens {
  private final AsyncServiceAccountTokens async;

  public ServiceAccountTokens(AsyncServiceAccountTokens async) {
    this.async = async;
  }

  public ServiceAccountToken create(String saKey, ServiceAccountTokenCreateParams params) {
    return create(saKey, params, RequestOptions.DEFAULT);
  }

  public ServiceAccountToken create(
      String saKey, ServiceAccountTokenCreateParams params, RequestOptions options) {
    return Futures.await(async.create(saKey, params, options));
  }

  public ServiceAccountTokenRevokeResponse revoke(
      String authTokenId, String saKey, ServiceAccountTokenRevokeParams params) {
    return revoke(authTokenId, saKey, params, RequestOptions.DEFAULT);
  }

  public ServiceAccountTokenRevokeResponse revoke(
      String authTokenId,
      String saKey,
      ServiceAccountTokenRevokeParams params,
      RequestOptions options) {
    return Futures.await(async.revoke(authTokenId, saKey, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<ServiceAccountToken> create(
        String saKey, ServiceAccountTokenCreateParams params) {
      return create(saKey, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<ServiceAccountToken> create(
        String saKey, ServiceAccountTokenCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(saKey, params, options));
    }

    public ApiResponse<ServiceAccountTokenRevokeResponse> revoke(
        String authTokenId, String saKey, ServiceAccountTokenRevokeParams params) {
      return revoke(authTokenId, saKey, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<ServiceAccountTokenRevokeResponse> revoke(
        String authTokenId,
        String saKey,
        ServiceAccountTokenRevokeParams params,
        RequestOptions options) {
      return Futures.await(async.withResponse().revoke(authTokenId, saKey, params, options));
    }
  }
}
