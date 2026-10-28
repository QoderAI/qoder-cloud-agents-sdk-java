package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class MemoryStores {
  private final AsyncMemoryStores async;

  public MemoryStores(AsyncMemoryStores async) {
    this.async = async;
  }

  public Memories memories() {
    return new Memories(async.memories());
  }

  public MemoryVersions versions() {
    return new MemoryVersions(async.versions());
  }

  public MemoryStore archive(String memoryStoreId) {
    return archive(memoryStoreId, RequestOptions.DEFAULT);
  }

  public MemoryStore archive(String memoryStoreId, RequestOptions options) {
    return Futures.await(async.archive(memoryStoreId, options));
  }

  public MemoryStore create(MemoryStoreCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public MemoryStore create(MemoryStoreCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public DeletedMemoryStore delete(String memoryStoreId) {
    return delete(memoryStoreId, RequestOptions.DEFAULT);
  }

  public DeletedMemoryStore delete(String memoryStoreId, RequestOptions options) {
    return Futures.await(async.delete(memoryStoreId, options));
  }

  public MemoryStore retrieve(String memoryStoreId) {
    return retrieve(memoryStoreId, RequestOptions.DEFAULT);
  }

  public MemoryStore retrieve(String memoryStoreId, RequestOptions options) {
    return Futures.await(async.retrieve(memoryStoreId, options));
  }

  public MemoryStoreListResponse list() {
    return list(MemoryStoreListParams.builder().build());
  }

  public MemoryStoreListResponse list(RequestOptions options) {
    return list(MemoryStoreListParams.builder().build(), options);
  }

  public MemoryStoreListResponse list(MemoryStoreListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public MemoryStoreListResponse list(MemoryStoreListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public MemoryStoreListResponse search() {
    return search(MemoryStoreSearchParams.builder().build());
  }

  public MemoryStoreListResponse search(RequestOptions options) {
    return search(MemoryStoreSearchParams.builder().build(), options);
  }

  public MemoryStoreListResponse search(MemoryStoreSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public MemoryStoreListResponse search(MemoryStoreSearchParams params, RequestOptions options) {
    return Futures.await(async.search(params, options));
  }

  public MemoryStore update(String memoryStoreId, MemoryStoreUpdateParams params) {
    return update(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public MemoryStore update(
      String memoryStoreId, MemoryStoreUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(memoryStoreId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<MemoryStore> archive(String memoryStoreId) {
      return archive(memoryStoreId, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStore> archive(String memoryStoreId, RequestOptions options) {
      return Futures.await(async.withResponse().archive(memoryStoreId, options));
    }

    public ApiResponse<MemoryStore> create(MemoryStoreCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStore> create(MemoryStoreCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<DeletedMemoryStore> delete(String memoryStoreId) {
      return delete(memoryStoreId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedMemoryStore> delete(String memoryStoreId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(memoryStoreId, options));
    }

    public ApiResponse<MemoryStore> retrieve(String memoryStoreId) {
      return retrieve(memoryStoreId, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStore> retrieve(String memoryStoreId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(memoryStoreId, options));
    }

    public ApiResponse<MemoryStoreListResponse> list() {
      return list(MemoryStoreListParams.builder().build());
    }

    public ApiResponse<MemoryStoreListResponse> list(RequestOptions options) {
      return list(MemoryStoreListParams.builder().build(), options);
    }

    public ApiResponse<MemoryStoreListResponse> list(MemoryStoreListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStoreListResponse> list(
        MemoryStoreListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<MemoryStoreListResponse> search() {
      return search(MemoryStoreSearchParams.builder().build());
    }

    public ApiResponse<MemoryStoreListResponse> search(RequestOptions options) {
      return search(MemoryStoreSearchParams.builder().build(), options);
    }

    public ApiResponse<MemoryStoreListResponse> search(MemoryStoreSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStoreListResponse> search(
        MemoryStoreSearchParams params, RequestOptions options) {
      return Futures.await(async.withResponse().search(params, options));
    }

    public ApiResponse<MemoryStore> update(String memoryStoreId, MemoryStoreUpdateParams params) {
      return update(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStore> update(
        String memoryStoreId, MemoryStoreUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(memoryStoreId, params, options));
    }
  }
}
