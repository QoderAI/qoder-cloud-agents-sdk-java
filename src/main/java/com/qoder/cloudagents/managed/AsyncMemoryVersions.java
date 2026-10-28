package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncMemoryVersions {
  private final ClientEngine engine;

  public AsyncMemoryVersions(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<MemoryVersion> retrieve(String memoryStoreId, String memoryVersionId) {
    return retrieve(memoryStoreId, memoryVersionId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryVersion> retrieve(
      String memoryStoreId, String memoryVersionId, RequestOptions options) {
    return Futures.map(
        withResponse().retrieve(memoryStoreId, memoryVersionId, options), ApiResponse::data);
  }

  public CompletableFuture<MemoryVersionListResponse> list(String memoryStoreId) {
    return list(memoryStoreId, MemoryVersionListParams.builder().build());
  }

  public CompletableFuture<MemoryVersionListResponse> list(
      String memoryStoreId, RequestOptions options) {
    return list(memoryStoreId, MemoryVersionListParams.builder().build(), options);
  }

  public CompletableFuture<MemoryVersionListResponse> list(
      String memoryStoreId, MemoryVersionListParams params) {
    return list(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<MemoryVersionListResponse> list(
      String memoryStoreId, MemoryVersionListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(memoryStoreId, params, options), ApiResponse::data);
  }

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
    public CompletableFuture<ApiResponse<MemoryVersion>> retrieve(
        String memoryStoreId, String memoryVersionId) {
      return retrieve(memoryStoreId, memoryVersionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryVersion>> retrieve(
        String memoryStoreId, String memoryVersionId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"memory_stores", memoryStoreId, "memory_versions", memoryVersionId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new MemoryVersion(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<MemoryVersionListResponse>> list(String memoryStoreId) {
      return list(memoryStoreId, MemoryVersionListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<MemoryVersionListResponse>> list(
        String memoryStoreId, RequestOptions options) {
      return list(memoryStoreId, MemoryVersionListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<MemoryVersionListResponse>> list(
        String memoryStoreId, MemoryVersionListParams params) {
      return list(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryVersionListResponse>> list(
        String memoryStoreId, MemoryVersionListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"memory_stores", memoryStoreId, "memory_versions"},
              params.query(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new MemoryVersionListResponse(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<MemoryVersion>> redact(
        String memoryStoreId, String memoryVersionId) {
      return redact(memoryStoreId, memoryVersionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<MemoryVersion>> redact(
        String memoryStoreId, String memoryVersionId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
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
          r -> r.map(v -> v.isNull() ? null : new MemoryVersion(v, r.requestId())));
    }
  }
}
