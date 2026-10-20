package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public final class AsyncIdentityConfigs {
  private final ClientEngine engine;

  public AsyncIdentityConfigs(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<IdentityConfig> retrieve(String identityId, String templateId) {
    return retrieve(identityId, templateId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<IdentityConfig> retrieve(
      String identityId, String templateId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(identityId, templateId, options), ApiResponse::data);
  }

  public CompletableFuture<IdentityConfig> upsert(
      String identityId, String templateId, IdentityConfigUpsertParams params) {
    return upsert(identityId, templateId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<IdentityConfig> upsert(
      String identityId,
      String templateId,
      IdentityConfigUpsertParams params,
      RequestOptions options) {
    return Futures.map(
        withResponse().upsert(identityId, templateId, params, options), ApiResponse::data);
  }

  public CompletableFuture<EffectiveConfig> getEffective(String identityId, String templateId) {
    return getEffective(identityId, templateId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<EffectiveConfig> getEffective(
      String identityId, String templateId, RequestOptions options) {
    return Futures.map(
        withResponse().getEffective(identityId, templateId, options), ApiResponse::data);
  }

  public CompletableFuture<IdentityConfigListResponse> list(String identityId) {
    return list(identityId, IdentityConfigListParams.builder().build());
  }

  public CompletableFuture<IdentityConfigListResponse> list(
      String identityId, IdentityConfigListParams params) {
    return list(identityId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<IdentityConfigListResponse> list(
      String identityId, IdentityConfigListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(identityId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<IdentityConfig>> retrieve(
        String identityId, String templateId) {
      return retrieve(identityId, templateId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<IdentityConfig>> retrieve(
        String identityId, String templateId, RequestOptions options) {

      return Futures.map(
          engine.request(
              "GET",
              new String[] {"identities", identityId, "templates", templateId, "config"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new IdentityConfig(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<IdentityConfig>> upsert(
        String identityId, String templateId, IdentityConfigUpsertParams params) {
      return upsert(identityId, templateId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<IdentityConfig>> upsert(
        String identityId,
        String templateId,
        IdentityConfigUpsertParams params,
        RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"identities", identityId, "templates", templateId, "config"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new IdentityConfig(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<EffectiveConfig>> getEffective(
        String identityId, String templateId) {
      return getEffective(identityId, templateId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<EffectiveConfig>> getEffective(
        String identityId, String templateId, RequestOptions options) {

      return Futures.map(
          engine.request(
              "GET",
              new String[] {"identities", identityId, "templates", templateId, "effective"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new EffectiveConfig(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<IdentityConfigListResponse>> list(String identityId) {
      return list(identityId, IdentityConfigListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<IdentityConfigListResponse>> list(
        String identityId, IdentityConfigListParams params) {
      return list(identityId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<IdentityConfigListResponse>> list(
        String identityId, IdentityConfigListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("List parameters must not be null");
      return CursorRequests.list(
          engine,
          new String[] {"identities", identityId, "templates"},
          params.query(),
          options,
          IdentityConfigListResponse::new);
    }
  }
}
