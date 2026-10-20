package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncIdentityServiceAccountTokens {
  private final ClientEngine engine;

  public AsyncIdentityServiceAccountTokens(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<ServiceAccountToken> create(
      String identityId, String saKey, ServiceAccountTokenCreateParams params) {
    return create(identityId, saKey, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ServiceAccountToken> create(
      String identityId,
      String saKey,
      ServiceAccountTokenCreateParams params,
      RequestOptions options) {
    return Futures.map(
        withResponse().create(identityId, saKey, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<ServiceAccountToken>> create(
        String identityId, String saKey, ServiceAccountTokenCreateParams params) {
      return create(identityId, saKey, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ServiceAccountToken>> create(
        String identityId,
        String saKey,
        ServiceAccountTokenCreateParams params,
        RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Parameters and options must not be null");
      return Futures.map(
          engine.serviceAccountToken(
              new String[] {"identities", identityId, "service_account_tokens"},
              saKey,
              params.toJsonValue(),
              options),
          r -> r.map(v -> new ServiceAccountToken(v, r.requestId())));
    }
  }
}
