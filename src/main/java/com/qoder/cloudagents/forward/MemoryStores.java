package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;

/** Synchronous Forward MemoryStores resource. */
public final class MemoryStores {
  private final AsyncMemoryStores async;
  private final MemoryStoreMemories memories;
  private final MemoryStoreMemoryVersions memoryVersions;

  public MemoryStores(AsyncMemoryStores async) {
    this.async = async;
    memories = new MemoryStoreMemories(async.memories());
    memoryVersions = new MemoryStoreMemoryVersions(async.memoryVersions());
  }

  public MemoryStoreMemories memories() {
    return memories;
  }

  public MemoryStoreMemoryVersions memoryVersions() {
    return memoryVersions;
  }

  public MemoryStoreListResponse list() {
    return list(MemoryStoreListParams.builder().build());
  }

  public MemoryStoreListResponse list(MemoryStoreListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public MemoryStoreListResponse list(MemoryStoreListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  /** Requires an Idempotency-Key header; supply it through RequestOptions. */
  public MemoryStore create(MemoryStoreCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public MemoryStore create(MemoryStoreCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public MemoryStore retrieve(String memoryStoreId) {
    return retrieve(memoryStoreId, RequestOptions.DEFAULT);
  }

  public MemoryStore retrieve(String memoryStoreId, RequestOptions options) {
    return Futures.await(async.retrieve(memoryStoreId, options));
  }

  public MemoryStore update(String memoryStoreId, MemoryStoreUpdateParams params) {
    return update(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public MemoryStore update(
      String memoryStoreId, MemoryStoreUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(memoryStoreId, params, options));
  }

  public DeletedMemoryStore delete(String memoryStoreId) {
    return delete(memoryStoreId, RequestOptions.DEFAULT);
  }

  public DeletedMemoryStore delete(String memoryStoreId, RequestOptions options) {
    return Futures.await(async.delete(memoryStoreId, options));
  }

  public MemoryStore archive(String memoryStoreId) {
    return archive(memoryStoreId, RequestOptions.DEFAULT);
  }

  public MemoryStore archive(String memoryStoreId, RequestOptions options) {
    return Futures.await(async.archive(memoryStoreId, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<MemoryStoreListResponse> list() {
      return list(MemoryStoreListParams.builder().build());
    }

    public ApiResponse<MemoryStoreListResponse> list(MemoryStoreListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStoreListResponse> list(
        MemoryStoreListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    /** Requires an Idempotency-Key header; supply it through RequestOptions. */
    public ApiResponse<MemoryStore> create(MemoryStoreCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStore> create(MemoryStoreCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<MemoryStore> retrieve(String memoryStoreId) {
      return retrieve(memoryStoreId, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStore> retrieve(String memoryStoreId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(memoryStoreId, options));
    }

    public ApiResponse<MemoryStore> update(String memoryStoreId, MemoryStoreUpdateParams params) {
      return update(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStore> update(
        String memoryStoreId, MemoryStoreUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(memoryStoreId, params, options));
    }

    public ApiResponse<DeletedMemoryStore> delete(String memoryStoreId) {
      return delete(memoryStoreId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedMemoryStore> delete(String memoryStoreId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(memoryStoreId, options));
    }

    public ApiResponse<MemoryStore> archive(String memoryStoreId) {
      return archive(memoryStoreId, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryStore> archive(String memoryStoreId, RequestOptions options) {
      return Futures.await(async.withResponse().archive(memoryStoreId, options));
    }
  }
}
