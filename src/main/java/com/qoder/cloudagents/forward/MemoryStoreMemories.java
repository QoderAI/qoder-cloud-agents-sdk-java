package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;

/** Synchronous Forward MemoryStoreMemories resource. */
public final class MemoryStoreMemories {
  private final AsyncMemoryStoreMemories async;

  public MemoryStoreMemories(AsyncMemoryStoreMemories async) {
    this.async = async;
  }

  public MemoryListResponse list(String memoryStoreId) {
    return list(memoryStoreId, MemoryListParams.builder().build());
  }

  public MemoryListResponse list(String memoryStoreId, MemoryListParams params) {
    return list(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public MemoryListResponse list(
      String memoryStoreId, MemoryListParams params, RequestOptions options) {
    return Futures.await(async.list(memoryStoreId, params, options));
  }

  public Memory create(String memoryStoreId, MemoryCreateParams params) {
    return create(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public Memory create(String memoryStoreId, MemoryCreateParams params, RequestOptions options) {
    return Futures.await(async.create(memoryStoreId, params, options));
  }

  public Memory retrieve(String memoryStoreId, String memoryId) {
    return retrieve(memoryStoreId, memoryId, RequestOptions.DEFAULT);
  }

  public Memory retrieve(String memoryStoreId, String memoryId, RequestOptions options) {
    return Futures.await(async.retrieve(memoryStoreId, memoryId, options));
  }

  public Memory update(String memoryStoreId, String memoryId, MemoryUpdateParams params) {
    return update(memoryStoreId, memoryId, params, RequestOptions.DEFAULT);
  }

  public Memory update(
      String memoryStoreId, String memoryId, MemoryUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(memoryStoreId, memoryId, params, options));
  }

  public DeletedMemory delete(String memoryStoreId, String memoryId) {
    return delete(memoryStoreId, memoryId, RequestOptions.DEFAULT);
  }

  public DeletedMemory delete(String memoryStoreId, String memoryId, RequestOptions options) {
    return Futures.await(async.delete(memoryStoreId, memoryId, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<MemoryListResponse> list(String memoryStoreId) {
      return list(memoryStoreId, MemoryListParams.builder().build());
    }

    public ApiResponse<MemoryListResponse> list(String memoryStoreId, MemoryListParams params) {
      return list(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryListResponse> list(
        String memoryStoreId, MemoryListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(memoryStoreId, params, options));
    }

    public ApiResponse<Memory> create(String memoryStoreId, MemoryCreateParams params) {
      return create(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Memory> create(
        String memoryStoreId, MemoryCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(memoryStoreId, params, options));
    }

    public ApiResponse<Memory> retrieve(String memoryStoreId, String memoryId) {
      return retrieve(memoryStoreId, memoryId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Memory> retrieve(
        String memoryStoreId, String memoryId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(memoryStoreId, memoryId, options));
    }

    public ApiResponse<Memory> update(
        String memoryStoreId, String memoryId, MemoryUpdateParams params) {
      return update(memoryStoreId, memoryId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Memory> update(
        String memoryStoreId, String memoryId, MemoryUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(memoryStoreId, memoryId, params, options));
    }

    public ApiResponse<DeletedMemory> delete(String memoryStoreId, String memoryId) {
      return delete(memoryStoreId, memoryId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedMemory> delete(
        String memoryStoreId, String memoryId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(memoryStoreId, memoryId, options));
    }
  }
}
