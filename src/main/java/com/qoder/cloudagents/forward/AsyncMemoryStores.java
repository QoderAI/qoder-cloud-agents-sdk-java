package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/** Asynchronous Forward MemoryStores resource. */
public final class AsyncMemoryStores {
  private final ClientEngine engine;
  private final AsyncMemoryStoreMemories memories;
  private final AsyncMemoryStoreMemoryVersions memoryVersions;

  public AsyncMemoryStores(ClientEngine engine) {
    this.engine = engine;
    memories = new AsyncMemoryStoreMemories(engine);
    memoryVersions = new AsyncMemoryStoreMemoryVersions(engine);
  }

  public AsyncMemoryStoreMemories memories() {
    return memories;
  }

  public AsyncMemoryStoreMemoryVersions memoryVersions() {
    return memoryVersions;
  }

  public CompletableFuture<MemoryStoreListResponse> list() {
    return list(MemoryStoreListParams.builder().build());
  }

  public CompletableFuture<MemoryStoreListResponse> list(MemoryStoreListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStoreListResponse> list(
      MemoryStoreListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  /** Requires an Idempotency-Key header; supply it through RequestOptions. */
  public CompletableFuture<MemoryStore> create(MemoryStoreCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStore> create(
      MemoryStoreCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryStore> retrieve(String memoryStoreId) {
    return retrieve(memoryStoreId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStore> retrieve(String memoryStoreId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(memoryStoreId, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryStore> update(
      String memoryStoreId, MemoryStoreUpdateParams params) {
    return update(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStore> update(
      String memoryStoreId, MemoryStoreUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(memoryStoreId, params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedMemoryStore> delete(String memoryStoreId) {
    return delete(memoryStoreId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedMemoryStore> delete(
      String memoryStoreId, RequestOptions options) {
    return Futures.map(withResponse().delete(memoryStoreId, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryStore> archive(String memoryStoreId) {
    return archive(memoryStoreId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStore> archive(String memoryStoreId, RequestOptions options) {
    return Futures.map(withResponse().archive(memoryStoreId, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<MemoryStoreListResponse>> list() {
      return list(MemoryStoreListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<MemoryStoreListResponse>> list(
        MemoryStoreListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStoreListResponse>> list(
        MemoryStoreListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return CursorRequests.list(
          engine,
          new String[] {"memory_stores"},
          params.query(),
          options,
          MemoryStoreListResponse::new);
    }

    /** Requires an Idempotency-Key header; supply it through RequestOptions. */
    public CompletableFuture<ApiResponse<MemoryStore>> create(MemoryStoreCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStore>> create(
        MemoryStoreCreateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"memory_stores"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new MemoryStore(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<MemoryStore>> retrieve(String memoryStoreId) {
      return retrieve(memoryStoreId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStore>> retrieve(
        String memoryStoreId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"memory_stores", memoryStoreId},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new MemoryStore(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<MemoryStore>> update(
        String memoryStoreId, MemoryStoreUpdateParams params) {
      return update(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStore>> update(
        String memoryStoreId, MemoryStoreUpdateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"memory_stores", memoryStoreId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new MemoryStore(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedMemoryStore>> delete(String memoryStoreId) {
      return delete(memoryStoreId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedMemoryStore>> delete(
        String memoryStoreId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"memory_stores", memoryStoreId},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new DeletedMemoryStore(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<MemoryStore>> archive(String memoryStoreId) {
      return archive(memoryStoreId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStore>> archive(
        String memoryStoreId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"memory_stores", memoryStoreId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new MemoryStore(value, response.requestId())));
    }
  }
}
