package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncMemoryStores {
  private final ClientEngine engine;

  public AsyncMemoryStores(ClientEngine engine) {
    this.engine = engine;
  }

  public AsyncMemories memories() {
    return new AsyncMemories(engine);
  }

  public AsyncMemoryVersions versions() {
    return new AsyncMemoryVersions(engine);
  }

  public CompletableFuture<MemoryStore> archive(String memoryStoreId) {
    return archive(memoryStoreId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStore> archive(String memoryStoreId, RequestOptions options) {
    return Futures.map(withResponse().archive(memoryStoreId, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryStore> create(MemoryStoreCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStore> create(
      MemoryStoreCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedMemoryStore> delete(String memoryStoreId) {
    return delete(memoryStoreId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedMemoryStore> delete(
      String memoryStoreId, RequestOptions options) {
    return Futures.map(withResponse().delete(memoryStoreId, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryStore> retrieve(String memoryStoreId) {
    return retrieve(memoryStoreId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStore> retrieve(String memoryStoreId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(memoryStoreId, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryStoreListResponse> list() {
    return list(MemoryStoreListParams.builder().build());
  }

  public CompletableFuture<MemoryStoreListResponse> list(RequestOptions options) {
    return list(MemoryStoreListParams.builder().build(), options);
  }

  public CompletableFuture<MemoryStoreListResponse> list(MemoryStoreListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStoreListResponse> list(
      MemoryStoreListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryStoreListResponse> search() {
    return search(MemoryStoreSearchParams.builder().build());
  }

  public CompletableFuture<MemoryStoreListResponse> search(RequestOptions options) {
    return search(MemoryStoreSearchParams.builder().build(), options);
  }

  public CompletableFuture<MemoryStoreListResponse> search(MemoryStoreSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStoreListResponse> search(
      MemoryStoreSearchParams params, RequestOptions options) {
    return Futures.map(withResponse().search(params, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryStore> update(
      String memoryStoreId, MemoryStoreUpdateParams params) {
    return update(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryStore> update(
      String memoryStoreId, MemoryStoreUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(memoryStoreId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<MemoryStore>> archive(String memoryStoreId) {
      return archive(memoryStoreId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStore>> archive(
        String memoryStoreId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"memory_stores", memoryStoreId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new MemoryStore(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<MemoryStore>> create(MemoryStoreCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStore>> create(
        MemoryStoreCreateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"memory_stores"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new MemoryStore(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedMemoryStore>> delete(String memoryStoreId) {
      return delete(memoryStoreId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedMemoryStore>> delete(
        String memoryStoreId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"memory_stores", memoryStoreId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedMemoryStore(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<MemoryStore>> retrieve(String memoryStoreId) {
      return retrieve(memoryStoreId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStore>> retrieve(
        String memoryStoreId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"memory_stores", memoryStoreId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new MemoryStore(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<MemoryStoreListResponse>> list() {
      return list(MemoryStoreListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<MemoryStoreListResponse>> list(RequestOptions options) {
      return list(MemoryStoreListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<MemoryStoreListResponse>> list(
        MemoryStoreListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStoreListResponse>> list(
        MemoryStoreListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine,
          new String[] {"memory_stores"},
          params.query(),
          options,
          MemoryStoreListResponse::new);
    }

    public CompletableFuture<ApiResponse<MemoryStoreListResponse>> search() {
      return search(MemoryStoreSearchParams.builder().build());
    }

    public CompletableFuture<ApiResponse<MemoryStoreListResponse>> search(RequestOptions options) {
      return search(MemoryStoreSearchParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<MemoryStoreListResponse>> search(
        MemoryStoreSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStoreListResponse>> search(
        MemoryStoreSearchParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return SearchRequests.search(
          engine,
          new String[] {"memory_stores", "search"},
          params.toJsonValue(),
          Collections.emptyMap(),
          options,
          MemoryStoreListResponse::new);
    }

    public CompletableFuture<ApiResponse<MemoryStore>> update(
        String memoryStoreId, MemoryStoreUpdateParams params) {
      return update(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryStore>> update(
        String memoryStoreId, MemoryStoreUpdateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"memory_stores", memoryStoreId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new MemoryStore(v, r.requestId())));
    }
  }
}
