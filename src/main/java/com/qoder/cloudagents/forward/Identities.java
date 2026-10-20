package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.Futures;

public final class Identities {
  private final IdentityServiceAccountTokens serviceAccountTokens;
  private final AsyncIdentities async;
  private final IdentityConfigs configs;
  private final IdentityMemoryStores memoryStores;

  public Identities(AsyncIdentities async) {
    this.async = async;
    serviceAccountTokens = new IdentityServiceAccountTokens(async.serviceAccountTokens());
    configs = new IdentityConfigs(async.configs());
    memoryStores = new IdentityMemoryStores(async.memoryStores());
  }

  public IdentityServiceAccountTokens serviceAccountTokens() {
    return serviceAccountTokens;
  }

  public IdentityConfigs configs() {
    return configs;
  }

  public IdentityMemoryStores memoryStores() {
    return memoryStores;
  }

  public Identity create(IdentityCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Identity create(IdentityCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public Identity ensureAdmin() {
    return ensureAdmin(RequestOptions.DEFAULT);
  }

  public Identity ensureAdmin(RequestOptions options) {
    return Futures.await(async.ensureAdmin(options));
  }

  public IdentityStats stats() {
    return stats(RequestOptions.DEFAULT);
  }

  public IdentityStats stats(RequestOptions options) {
    return Futures.await(async.stats(options));
  }

  public Identity retrieve(String identityId) {
    return retrieve(identityId, RequestOptions.DEFAULT);
  }

  public Identity retrieve(String identityId, RequestOptions options) {
    return Futures.await(async.retrieve(identityId, options));
  }

  public Identity update(String identityId, IdentityUpdateParams params) {
    return update(identityId, params, RequestOptions.DEFAULT);
  }

  public Identity update(String identityId, IdentityUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(identityId, params, options));
  }

  public DeletedIdentity delete(String identityId) {
    return delete(identityId, RequestOptions.DEFAULT);
  }

  public DeletedIdentity delete(String identityId, RequestOptions options) {
    return Futures.await(async.delete(identityId, options));
  }

  public IdentityListTemplatesResponse listTemplates(String identityId) {
    return listTemplates(identityId, RequestOptions.DEFAULT);
  }

  public IdentityListTemplatesResponse listTemplates(String identityId, RequestOptions options) {
    return Futures.await(async.listTemplates(identityId, options));
  }

  public IdentityClearResponse clear(String identityId, IdentityClearParams params) {
    return clear(identityId, params, RequestOptions.DEFAULT);
  }

  public IdentityClearResponse clear(
      String identityId, IdentityClearParams params, RequestOptions options) {
    return Futures.await(async.clear(identityId, params, options));
  }

  public Identity disable(String identityId) {
    return disable(identityId, RequestOptions.DEFAULT);
  }

  public Identity disable(String identityId, RequestOptions options) {
    return Futures.await(async.disable(identityId, options));
  }

  public Identity enable(String identityId) {
    return enable(identityId, RequestOptions.DEFAULT);
  }

  public Identity enable(String identityId, RequestOptions options) {
    return Futures.await(async.enable(identityId, options));
  }

  public IdentityListResponse list() {
    return list(IdentityListParams.builder().build());
  }

  public IdentityListResponse list(IdentityListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public IdentityListResponse list(IdentityListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<Identity> create(IdentityCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Identity> create(IdentityCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<Identity> ensureAdmin() {
      return ensureAdmin(RequestOptions.DEFAULT);
    }

    public ApiResponse<Identity> ensureAdmin(RequestOptions options) {
      return Futures.await(async.withResponse().ensureAdmin(options));
    }

    public ApiResponse<IdentityStats> stats() {
      return stats(RequestOptions.DEFAULT);
    }

    public ApiResponse<IdentityStats> stats(RequestOptions options) {
      return Futures.await(async.withResponse().stats(options));
    }

    public ApiResponse<Identity> retrieve(String identityId) {
      return retrieve(identityId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Identity> retrieve(String identityId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(identityId, options));
    }

    public ApiResponse<Identity> update(String identityId, IdentityUpdateParams params) {
      return update(identityId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Identity> update(
        String identityId, IdentityUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(identityId, params, options));
    }

    public ApiResponse<DeletedIdentity> delete(String identityId) {
      return delete(identityId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedIdentity> delete(String identityId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(identityId, options));
    }

    public ApiResponse<IdentityListTemplatesResponse> listTemplates(String identityId) {
      return listTemplates(identityId, RequestOptions.DEFAULT);
    }

    public ApiResponse<IdentityListTemplatesResponse> listTemplates(
        String identityId, RequestOptions options) {
      return Futures.await(async.withResponse().listTemplates(identityId, options));
    }

    public ApiResponse<IdentityClearResponse> clear(String identityId, IdentityClearParams params) {
      return clear(identityId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<IdentityClearResponse> clear(
        String identityId, IdentityClearParams params, RequestOptions options) {
      return Futures.await(async.withResponse().clear(identityId, params, options));
    }

    public ApiResponse<Identity> disable(String identityId) {
      return disable(identityId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Identity> disable(String identityId, RequestOptions options) {
      return Futures.await(async.withResponse().disable(identityId, options));
    }

    public ApiResponse<Identity> enable(String identityId) {
      return enable(identityId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Identity> enable(String identityId, RequestOptions options) {
      return Futures.await(async.withResponse().enable(identityId, options));
    }

    public ApiResponse<IdentityListResponse> list() {
      return list(IdentityListParams.builder().build());
    }

    public ApiResponse<IdentityListResponse> list(IdentityListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<IdentityListResponse> list(
        IdentityListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }
  }
}
