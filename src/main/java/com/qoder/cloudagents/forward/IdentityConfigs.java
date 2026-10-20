package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.Futures;

public final class IdentityConfigs {
  private final AsyncIdentityConfigs async;

  public IdentityConfigs(AsyncIdentityConfigs async) {
    this.async = async;
  }

  public IdentityConfig retrieve(String identityId, String templateId) {
    return retrieve(identityId, templateId, RequestOptions.DEFAULT);
  }

  public IdentityConfig retrieve(String identityId, String templateId, RequestOptions options) {
    return Futures.await(async.retrieve(identityId, templateId, options));
  }

  public IdentityConfig upsert(
      String identityId, String templateId, IdentityConfigUpsertParams params) {
    return upsert(identityId, templateId, params, RequestOptions.DEFAULT);
  }

  public IdentityConfig upsert(
      String identityId,
      String templateId,
      IdentityConfigUpsertParams params,
      RequestOptions options) {
    return Futures.await(async.upsert(identityId, templateId, params, options));
  }

  public EffectiveConfig getEffective(String identityId, String templateId) {
    return getEffective(identityId, templateId, RequestOptions.DEFAULT);
  }

  public EffectiveConfig getEffective(
      String identityId, String templateId, RequestOptions options) {
    return Futures.await(async.getEffective(identityId, templateId, options));
  }

  public IdentityConfigListResponse list(String identityId) {
    return list(identityId, IdentityConfigListParams.builder().build());
  }

  public IdentityConfigListResponse list(String identityId, IdentityConfigListParams params) {
    return list(identityId, params, RequestOptions.DEFAULT);
  }

  public IdentityConfigListResponse list(
      String identityId, IdentityConfigListParams params, RequestOptions options) {
    return Futures.await(async.list(identityId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<IdentityConfig> retrieve(String identityId, String templateId) {
      return retrieve(identityId, templateId, RequestOptions.DEFAULT);
    }

    public ApiResponse<IdentityConfig> retrieve(
        String identityId, String templateId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(identityId, templateId, options));
    }

    public ApiResponse<IdentityConfig> upsert(
        String identityId, String templateId, IdentityConfigUpsertParams params) {
      return upsert(identityId, templateId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<IdentityConfig> upsert(
        String identityId,
        String templateId,
        IdentityConfigUpsertParams params,
        RequestOptions options) {
      return Futures.await(async.withResponse().upsert(identityId, templateId, params, options));
    }

    public ApiResponse<EffectiveConfig> getEffective(String identityId, String templateId) {
      return getEffective(identityId, templateId, RequestOptions.DEFAULT);
    }

    public ApiResponse<EffectiveConfig> getEffective(
        String identityId, String templateId, RequestOptions options) {
      return Futures.await(async.withResponse().getEffective(identityId, templateId, options));
    }

    public ApiResponse<IdentityConfigListResponse> list(String identityId) {
      return list(identityId, IdentityConfigListParams.builder().build());
    }

    public ApiResponse<IdentityConfigListResponse> list(
        String identityId, IdentityConfigListParams params) {
      return list(identityId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<IdentityConfigListResponse> list(
        String identityId, IdentityConfigListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(identityId, params, options));
    }
  }
}
