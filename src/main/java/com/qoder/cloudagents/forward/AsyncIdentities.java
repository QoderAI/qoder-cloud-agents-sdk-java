package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public final class AsyncIdentities {
  private final AsyncIdentityServiceAccountTokens serviceAccountTokens;
  private final ClientEngine engine;
  private final AsyncIdentityConfigs configs;
  private final AsyncIdentityMemoryStores memoryStores;

  public AsyncIdentities(ClientEngine engine) {
    this.engine = engine;
    serviceAccountTokens = new AsyncIdentityServiceAccountTokens(engine);
    configs = new AsyncIdentityConfigs(engine);
    memoryStores = new AsyncIdentityMemoryStores(engine);
  }

  public AsyncIdentityServiceAccountTokens serviceAccountTokens() {
    return serviceAccountTokens;
  }

  public AsyncIdentityConfigs configs() {
    return configs;
  }

  public AsyncIdentityMemoryStores memoryStores() {
    return memoryStores;
  }

  public CompletableFuture<Identity> create(IdentityCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Identity> create(IdentityCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<Identity> ensureAdmin() {
    return ensureAdmin(RequestOptions.DEFAULT);
  }

  public CompletableFuture<Identity> ensureAdmin(RequestOptions options) {
    return Futures.map(withResponse().ensureAdmin(options), ApiResponse::data);
  }

  public CompletableFuture<IdentityStats> stats() {
    return stats(RequestOptions.DEFAULT);
  }

  public CompletableFuture<IdentityStats> stats(RequestOptions options) {
    return Futures.map(withResponse().stats(options), ApiResponse::data);
  }

  public CompletableFuture<Identity> retrieve(String identityId) {
    return retrieve(identityId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Identity> retrieve(String identityId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(identityId, options), ApiResponse::data);
  }

  public CompletableFuture<Identity> update(String identityId, IdentityUpdateParams params) {
    return update(identityId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Identity> update(
      String identityId, IdentityUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(identityId, params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedIdentity> delete(String identityId) {
    return delete(identityId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedIdentity> delete(String identityId, RequestOptions options) {
    return Futures.map(withResponse().delete(identityId, options), ApiResponse::data);
  }

  public CompletableFuture<IdentityListTemplatesResponse> listTemplates(String identityId) {
    return listTemplates(identityId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<IdentityListTemplatesResponse> listTemplates(
      String identityId, RequestOptions options) {
    return Futures.map(withResponse().listTemplates(identityId, options), ApiResponse::data);
  }

  public CompletableFuture<IdentityClearResponse> clear(
      String identityId, IdentityClearParams params) {
    return clear(identityId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<IdentityClearResponse> clear(
      String identityId, IdentityClearParams params, RequestOptions options) {
    return Futures.map(withResponse().clear(identityId, params, options), ApiResponse::data);
  }

  public CompletableFuture<Identity> disable(String identityId) {
    return disable(identityId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Identity> disable(String identityId, RequestOptions options) {
    return Futures.map(withResponse().disable(identityId, options), ApiResponse::data);
  }

  public CompletableFuture<Identity> enable(String identityId) {
    return enable(identityId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Identity> enable(String identityId, RequestOptions options) {
    return Futures.map(withResponse().enable(identityId, options), ApiResponse::data);
  }

  public CompletableFuture<IdentityListResponse> list() {
    return list(IdentityListParams.builder().build());
  }

  public CompletableFuture<IdentityListResponse> list(IdentityListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<IdentityListResponse> list(
      IdentityListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<Identity>> create(IdentityCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Identity>> create(
        IdentityCreateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"identities"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new Identity(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Identity>> ensureAdmin() {
      return ensureAdmin(RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Identity>> ensureAdmin(RequestOptions options) {

      return Futures.map(
          engine.request(
              "POST",
              new String[] {"identities", "admin", "ensure"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new Identity(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<IdentityStats>> stats() {
      return stats(RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<IdentityStats>> stats(RequestOptions options) {

      return Futures.map(
          engine.request(
              "GET",
              new String[] {"identities", "stats"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new IdentityStats(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Identity>> retrieve(String identityId) {
      return retrieve(identityId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Identity>> retrieve(
        String identityId, RequestOptions options) {

      return Futures.map(
          engine.request(
              "GET",
              new String[] {"identities", identityId},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new Identity(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Identity>> update(
        String identityId, IdentityUpdateParams params) {
      return update(identityId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Identity>> update(
        String identityId, IdentityUpdateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"identities", identityId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new Identity(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedIdentity>> delete(String identityId) {
      return delete(identityId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedIdentity>> delete(
        String identityId, RequestOptions options) {

      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"identities", identityId},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new DeletedIdentity(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<IdentityListTemplatesResponse>> listTemplates(
        String identityId) {
      return listTemplates(identityId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<IdentityListTemplatesResponse>> listTemplates(
        String identityId, RequestOptions options) {

      return Futures.map(
          engine.request(
              "GET",
              new String[] {"identities", identityId, "agents"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response ->
              response.map(
                  value -> new IdentityListTemplatesResponse(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<IdentityClearResponse>> clear(
        String identityId, IdentityClearParams params) {
      return clear(identityId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<IdentityClearResponse>> clear(
        String identityId, IdentityClearParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"identities", identityId, "clear"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response ->
              response.map(value -> new IdentityClearResponse(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Identity>> disable(String identityId) {
      return disable(identityId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Identity>> disable(
        String identityId, RequestOptions options) {

      return Futures.map(
          engine.request(
              "POST",
              new String[] {"identities", identityId, "disable"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new Identity(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Identity>> enable(String identityId) {
      return enable(identityId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Identity>> enable(
        String identityId, RequestOptions options) {

      return Futures.map(
          engine.request(
              "POST",
              new String[] {"identities", identityId, "enable"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new Identity(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<IdentityListResponse>> list() {
      return list(IdentityListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<IdentityListResponse>> list(IdentityListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<IdentityListResponse>> list(
        IdentityListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("List parameters must not be null");
      return CursorRequests.list(
          engine, new String[] {"identities"}, params.query(), options, IdentityListResponse::new);
    }
  }
}
