package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncServiceAccountTokens {
  private final ClientEngine engine;

  public AsyncServiceAccountTokens(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<ServiceAccountToken> create(
      String saKey, ServiceAccountTokenCreateParams params) {
    return create(saKey, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ServiceAccountToken> create(
      String saKey, ServiceAccountTokenCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(saKey, params, options), ApiResponse::data);
  }

  public CompletableFuture<ServiceAccountTokenRevokeResponse> revoke(
      String authTokenId, String saKey, ServiceAccountTokenRevokeParams params) {
    return revoke(authTokenId, saKey, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ServiceAccountTokenRevokeResponse> revoke(
      String authTokenId,
      String saKey,
      ServiceAccountTokenRevokeParams params,
      RequestOptions options) {
    return Futures.map(
        withResponse().revoke(authTokenId, saKey, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<ServiceAccountToken>> create(
        String saKey, ServiceAccountTokenCreateParams params) {
      return create(saKey, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ServiceAccountToken>> create(
        String saKey, ServiceAccountTokenCreateParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Parameters and options must not be null");
      return Futures.map(
          engine.serviceAccountToken(
              new String[] {"service_account_tokens"}, saKey, params.toJsonValue(), options),
          r -> r.map(v -> new ServiceAccountToken(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<ServiceAccountTokenRevokeResponse>> revoke(
        String authTokenId, String saKey, ServiceAccountTokenRevokeParams params) {
      return revoke(authTokenId, saKey, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ServiceAccountTokenRevokeResponse>> revoke(
        String authTokenId,
        String saKey,
        ServiceAccountTokenRevokeParams params,
        RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Parameters and options must not be null");
      return Futures.map(
          engine.serviceAccountToken(
              new String[] {"service_account_tokens", authTokenId, "revoke"},
              saKey,
              params.toJsonValue(),
              options),
          r -> r.map(v -> new ServiceAccountTokenRevokeResponse(v, r.requestId())));
    }
  }
}
