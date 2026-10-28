package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncMemories {
  private final ClientEngine engine;

  public AsyncMemories(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<Memory> create(String memoryStoreId, MemoryCreateParams params) {
    return create(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Memory> create(
      String memoryStoreId, MemoryCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(memoryStoreId, params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedMemory> delete(String memoryStoreId, String memoryId) {
    return delete(memoryStoreId, memoryId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedMemory> delete(
      String memoryStoreId, String memoryId, RequestOptions options) {
    return Futures.map(withResponse().delete(memoryStoreId, memoryId, options), ApiResponse::data);
  }

  public CompletableFuture<Memory> retrieve(String memoryStoreId, String memoryId) {
    return retrieve(memoryStoreId, memoryId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Memory> retrieve(
      String memoryStoreId, String memoryId, RequestOptions options) {
    return Futures.map(
        withResponse().retrieve(memoryStoreId, memoryId, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryListResponse> list(String memoryStoreId) {
    return list(memoryStoreId, MemoryListParams.builder().build());
  }

  public CompletableFuture<MemoryListResponse> list(String memoryStoreId, RequestOptions options) {
    return list(memoryStoreId, MemoryListParams.builder().build(), options);
  }

  public CompletableFuture<MemoryListResponse> list(String memoryStoreId, MemoryListParams params) {
    return list(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryListResponse> list(
      String memoryStoreId, MemoryListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(memoryStoreId, params, options), ApiResponse::data);
  }

  public CompletableFuture<Memory> update(
      String memoryStoreId, String memoryId, MemoryUpdateParams params) {
    return update(memoryStoreId, memoryId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Memory> update(
      String memoryStoreId, String memoryId, MemoryUpdateParams params, RequestOptions options) {
    return Futures.map(
        withResponse().update(memoryStoreId, memoryId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<Memory>> create(
        String memoryStoreId, MemoryCreateParams params) {
      return create(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Memory>> create(
        String memoryStoreId, MemoryCreateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"memory_stores", memoryStoreId, "memories"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Memory(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedMemory>> delete(
        String memoryStoreId, String memoryId) {
      return delete(memoryStoreId, memoryId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedMemory>> delete(
        String memoryStoreId, String memoryId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"memory_stores", memoryStoreId, "memories", memoryId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedMemory(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Memory>> retrieve(String memoryStoreId, String memoryId) {
      return retrieve(memoryStoreId, memoryId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Memory>> retrieve(
        String memoryStoreId, String memoryId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"memory_stores", memoryStoreId, "memories", memoryId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Memory(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<MemoryListResponse>> list(String memoryStoreId) {
      return list(memoryStoreId, MemoryListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<MemoryListResponse>> list(
        String memoryStoreId, RequestOptions options) {
      return list(memoryStoreId, MemoryListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<MemoryListResponse>> list(
        String memoryStoreId, MemoryListParams params) {
      return list(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryListResponse>> list(
        String memoryStoreId, MemoryListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"memory_stores", memoryStoreId, "memories"},
              params.query(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new MemoryListResponse(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Memory>> update(
        String memoryStoreId, String memoryId, MemoryUpdateParams params) {
      return update(memoryStoreId, memoryId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Memory>> update(
        String memoryStoreId, String memoryId, MemoryUpdateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"memory_stores", memoryStoreId, "memories", memoryId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Memory(v, r.requestId())));
    }
  }
}
