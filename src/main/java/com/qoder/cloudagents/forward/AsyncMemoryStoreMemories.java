package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/** Asynchronous Forward MemoryStoreMemories resource. */
public final class AsyncMemoryStoreMemories {
  private final ClientEngine engine;

  public AsyncMemoryStoreMemories(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<MemoryListResponse> list(String memoryStoreId) {
    return list(memoryStoreId, MemoryListParams.builder().build());
  }

  public CompletableFuture<MemoryListResponse> list(String memoryStoreId, MemoryListParams params) {
    return list(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryListResponse> list(
      String memoryStoreId, MemoryListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(memoryStoreId, params, options), ApiResponse::data);
  }

  public CompletableFuture<Memory> create(String memoryStoreId, MemoryCreateParams params) {
    return create(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Memory> create(
      String memoryStoreId, MemoryCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(memoryStoreId, params, options), ApiResponse::data);
  }

  public CompletableFuture<Memory> retrieve(String memoryStoreId, String memoryId) {
    return retrieve(memoryStoreId, memoryId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Memory> retrieve(
      String memoryStoreId, String memoryId, RequestOptions options) {
    return Futures.map(
        withResponse().retrieve(memoryStoreId, memoryId, options), ApiResponse::data);
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

  public CompletableFuture<DeletedMemory> delete(String memoryStoreId, String memoryId) {
    return delete(memoryStoreId, memoryId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedMemory> delete(
      String memoryStoreId, String memoryId, RequestOptions options) {
    return Futures.map(withResponse().delete(memoryStoreId, memoryId, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<MemoryListResponse>> list(String memoryStoreId) {
      return list(memoryStoreId, MemoryListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<MemoryListResponse>> list(
        String memoryStoreId, MemoryListParams params) {
      return list(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryListResponse>> list(
        String memoryStoreId, MemoryListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return CursorRequests.list(
          engine,
          new String[] {"memory_stores", memoryStoreId, "memories"},
          params.query(),
          options,
          MemoryListResponse::new);
    }

    public CompletableFuture<ApiResponse<Memory>> create(
        String memoryStoreId, MemoryCreateParams params) {
      return create(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Memory>> create(
        String memoryStoreId, MemoryCreateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"memory_stores", memoryStoreId, "memories"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new Memory(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Memory>> retrieve(String memoryStoreId, String memoryId) {
      return retrieve(memoryStoreId, memoryId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Memory>> retrieve(
        String memoryStoreId, String memoryId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"memory_stores", memoryStoreId, "memories", memoryId},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new Memory(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Memory>> update(
        String memoryStoreId, String memoryId, MemoryUpdateParams params) {
      return update(memoryStoreId, memoryId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Memory>> update(
        String memoryStoreId, String memoryId, MemoryUpdateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"memory_stores", memoryStoreId, "memories", memoryId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new Memory(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedMemory>> delete(
        String memoryStoreId, String memoryId) {
      return delete(memoryStoreId, memoryId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedMemory>> delete(
        String memoryStoreId, String memoryId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"memory_stores", memoryStoreId, "memories", memoryId},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new DeletedMemory(value, response.requestId())));
    }
  }
}
