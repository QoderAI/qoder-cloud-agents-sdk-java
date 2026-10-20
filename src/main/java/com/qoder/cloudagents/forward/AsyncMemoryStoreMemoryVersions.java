package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/** Asynchronous Forward MemoryStoreMemoryVersions resource. */
public final class AsyncMemoryStoreMemoryVersions {
  private final ClientEngine engine;

  public AsyncMemoryStoreMemoryVersions(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<MemoryVersionListResponse> list(String memoryStoreId) {
    return list(memoryStoreId, MemoryVersionListParams.builder().build());
  }

  public CompletableFuture<MemoryVersionListResponse> list(
      String memoryStoreId, MemoryVersionListParams params) {
    return list(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryVersionListResponse> list(
      String memoryStoreId, MemoryVersionListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(memoryStoreId, params, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryVersion> retrieve(String memoryStoreId, String memoryVersionId) {
    return retrieve(memoryStoreId, memoryVersionId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryVersion> retrieve(
      String memoryStoreId, String memoryVersionId, RequestOptions options) {
    return Futures.map(
        withResponse().retrieve(memoryStoreId, memoryVersionId, options), ApiResponse::data);
  }

  /** Permanently removes this version's content; leaves the current Memory unchanged. */
  public CompletableFuture<MemoryVersion> redact(String memoryStoreId, String memoryVersionId) {
    return redact(memoryStoreId, memoryVersionId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryVersion> redact(
      String memoryStoreId, String memoryVersionId, RequestOptions options) {
    return Futures.map(
        withResponse().redact(memoryStoreId, memoryVersionId, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<MemoryVersionListResponse>> list(String memoryStoreId) {
      return list(memoryStoreId, MemoryVersionListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<MemoryVersionListResponse>> list(
        String memoryStoreId, MemoryVersionListParams params) {
      return list(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryVersionListResponse>> list(
        String memoryStoreId, MemoryVersionListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters must not be null");
      return CursorRequests.list(
          engine,
          new String[] {"memory_stores", memoryStoreId, "memory_versions"},
          params.query(),
          options,
          MemoryVersionListResponse::new);
    }

    public CompletableFuture<ApiResponse<MemoryVersion>> retrieve(
        String memoryStoreId, String memoryVersionId) {
      return retrieve(memoryStoreId, memoryVersionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryVersion>> retrieve(
        String memoryStoreId, String memoryVersionId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"memory_stores", memoryStoreId, "memory_versions", memoryVersionId},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new MemoryVersion(value, response.requestId())));
    }

    /** Permanently removes this version's content; leaves the current Memory unchanged. */
    public CompletableFuture<ApiResponse<MemoryVersion>> redact(
        String memoryStoreId, String memoryVersionId) {
      return redact(memoryStoreId, memoryVersionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryVersion>> redact(
        String memoryStoreId, String memoryVersionId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "POST",
              new String[] {
                "memory_stores", memoryStoreId, "memory_versions", memoryVersionId, "redact"
              },
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new MemoryVersion(value, response.requestId())));
    }
  }
}
