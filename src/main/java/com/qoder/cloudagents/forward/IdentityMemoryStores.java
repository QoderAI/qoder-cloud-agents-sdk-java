package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class IdentityMemoryStores {
  private final AsyncIdentityMemoryStores async;

  public IdentityMemoryStores(AsyncIdentityMemoryStores async) {
    this.async = async;
  }

  public IdentityMemoryStoreListResponse list(String identityId, String templateId) {
    return list(identityId, templateId, RequestOptions.DEFAULT);
  }

  public IdentityMemoryStoreListResponse list(
      String identityId, String templateId, RequestOptions options) {
    return Futures.await(async.list(identityId, templateId, options));
  }

  public MemoryStoreMount mount(
      String identityId, String templateId, IdentityMemoryStoreMountParams params) {
    return mount(identityId, templateId, params, RequestOptions.DEFAULT);
  }

  public MemoryStoreMount mount(
      String identityId,
      String templateId,
      IdentityMemoryStoreMountParams params,
      RequestOptions options) {
    return Futures.await(async.mount(identityId, templateId, params, options));
  }

  public DeletedMemoryStoreMount detach(
      String identityId, String templateId, String memoryStoreId) {
    return detach(identityId, templateId, memoryStoreId, RequestOptions.DEFAULT);
  }

  public DeletedMemoryStoreMount detach(
      String identityId, String templateId, String memoryStoreId, RequestOptions options) {
    return Futures.await(async.detach(identityId, templateId, memoryStoreId, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<IdentityMemoryStoreListResponse> list(String identityId, String templateId) {
      return list(identityId, templateId, RequestOptions.DEFAULT);
    }

    public ApiResponse<IdentityMemoryStoreListResponse> list(
        String identityId, String templateId, RequestOptions options) {
      return Futures.await(async.withResponse().list(identityId, templateId, options));
    }

    public ApiResponse<MemoryStoreMount> mount(
        String identityId, String templateId, IdentityMemoryStoreMountParams params) {
      return mount(identityId, templateId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStoreMount> mount(
        String identityId,
        String templateId,
        IdentityMemoryStoreMountParams params,
        RequestOptions options) {
      return Futures.await(async.withResponse().mount(identityId, templateId, params, options));
    }

    public ApiResponse<DeletedMemoryStoreMount> detach(
        String identityId, String templateId, String memoryStoreId) {
      return detach(identityId, templateId, memoryStoreId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedMemoryStoreMount> detach(
        String identityId, String templateId, String memoryStoreId, RequestOptions options) {
      return Futures.await(
          async.withResponse().detach(identityId, templateId, memoryStoreId, options));
    }
  }
}
