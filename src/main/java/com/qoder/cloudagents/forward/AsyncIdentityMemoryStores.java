package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncIdentityMemoryStores {
  private final ClientEngine engine;

  public AsyncIdentityMemoryStores(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<IdentityMemoryStoreListResponse> list(
      String identityId, String templateId) {
    return list(identityId, templateId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<IdentityMemoryStoreListResponse> list(
      String identityId, String templateId, RequestOptions options) {
    return Futures.map(withResponse().list(identityId, templateId, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryStoreMount> mount(
      String identityId, String templateId, IdentityMemoryStoreMountParams params) {
    return mount(identityId, templateId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStoreMount> mount(
      String identityId,
      String templateId,
      IdentityMemoryStoreMountParams params,
      RequestOptions options) {
    return Futures.map(
        withResponse().mount(identityId, templateId, params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedMemoryStoreMount> detach(
      String identityId, String templateId, String memoryStoreId) {
    return detach(identityId, templateId, memoryStoreId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedMemoryStoreMount> detach(
      String identityId, String templateId, String memoryStoreId, RequestOptions options) {
    return Futures.map(
        withResponse().detach(identityId, templateId, memoryStoreId, options), ApiResponse::data);
  }

  public CompletableFuture<IdentityMemoryStoreListResponse> list(
      String identityId, String templateId, IdentityMemoryStoreListParams params) {
    return list(identityId, templateId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<IdentityMemoryStoreListResponse> list(
      String identityId,
      String templateId,
      IdentityMemoryStoreListParams params,
      RequestOptions options) {
    return Futures.map(
        withResponse().list(identityId, templateId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<IdentityMemoryStoreListResponse>> list(
        String identityId, String templateId, IdentityMemoryStoreListParams params) {
      return list(identityId, templateId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<IdentityMemoryStoreListResponse>> list(
        String identityId,
        String templateId,
        IdentityMemoryStoreListParams params,
        RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return CursorRequests.list(
          engine,
          new String[] {"identities", identityId, "templates", templateId, "memory_stores"},
          params.query(),
          options,
          IdentityMemoryStoreListResponse::new);
    }

    public CompletableFuture<ApiResponse<IdentityMemoryStoreListResponse>> list(
        String identityId, String templateId) {
      return list(identityId, templateId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<IdentityMemoryStoreListResponse>> list(
        String identityId, String templateId, RequestOptions options) {
      return list(identityId, templateId, IdentityMemoryStoreListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<MemoryStoreMount>> mount(
        String identityId, String templateId, IdentityMemoryStoreMountParams params) {
      return mount(identityId, templateId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStoreMount>> mount(
        String identityId,
        String templateId,
        IdentityMemoryStoreMountParams params,
        RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"identities", identityId, "templates", templateId, "memory_stores"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new MemoryStoreMount(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedMemoryStoreMount>> detach(
        String identityId, String templateId, String memoryStoreId) {
      return detach(identityId, templateId, memoryStoreId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedMemoryStoreMount>> detach(
        String identityId, String templateId, String memoryStoreId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {
                "identities", identityId, "templates", templateId, "memory_stores", memoryStoreId
              },
              Collections.emptyMap(),
              null,
              true,
              options),
          response ->
              response.map(value -> new DeletedMemoryStoreMount(value, response.requestId())));
    }
  }
}
