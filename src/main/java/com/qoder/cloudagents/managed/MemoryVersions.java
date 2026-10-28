package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class MemoryVersions {
  private final AsyncMemoryVersions async;

  public MemoryVersions(AsyncMemoryVersions async) {
    this.async = async;
  }

  public MemoryVersion retrieve(String memoryStoreId, String memoryVersionId) {
    return retrieve(memoryStoreId, memoryVersionId, RequestOptions.DEFAULT);
  }

  public MemoryVersion retrieve(
      String memoryStoreId, String memoryVersionId, RequestOptions options) {
    return Futures.await(async.retrieve(memoryStoreId, memoryVersionId, options));
  }

  public MemoryVersionListResponse list(String memoryStoreId) {
    return list(memoryStoreId, MemoryVersionListParams.builder().build());
  }

  public MemoryVersionListResponse list(String memoryStoreId, RequestOptions options) {
    return list(memoryStoreId, MemoryVersionListParams.builder().build(), options);
  }

  public MemoryVersionListResponse list(String memoryStoreId, MemoryVersionListParams params) {
    return list(memoryStoreId, params, RequestOptions.DEFAULT);
  }

  public MemoryVersionListResponse list(
      String memoryStoreId, MemoryVersionListParams params, RequestOptions options) {
    return Futures.await(async.list(memoryStoreId, params, options));
  }

  public MemoryVersion redact(String memoryStoreId, String memoryVersionId) {
    return redact(memoryStoreId, memoryVersionId, RequestOptions.DEFAULT);
  }

  public MemoryVersion redact(
      String memoryStoreId, String memoryVersionId, RequestOptions options) {
    return Futures.await(async.redact(memoryStoreId, memoryVersionId, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<MemoryVersion> retrieve(String memoryStoreId, String memoryVersionId) {
      return retrieve(memoryStoreId, memoryVersionId, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryVersion> retrieve(
        String memoryStoreId, String memoryVersionId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(memoryStoreId, memoryVersionId, options));
    }

    public ApiResponse<MemoryVersionListResponse> list(String memoryStoreId) {
      return list(memoryStoreId, MemoryVersionListParams.builder().build());
    }

    public ApiResponse<MemoryVersionListResponse> list(
        String memoryStoreId, RequestOptions options) {
      return list(memoryStoreId, MemoryVersionListParams.builder().build(), options);
    }

    public ApiResponse<MemoryVersionListResponse> list(
        String memoryStoreId, MemoryVersionListParams params) {
      return list(memoryStoreId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryVersionListResponse> list(
        String memoryStoreId, MemoryVersionListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(memoryStoreId, params, options));
    }

    public ApiResponse<MemoryVersion> redact(String memoryStoreId, String memoryVersionId) {
      return redact(memoryStoreId, memoryVersionId, RequestOptions.DEFAULT);
    }

    public ApiResponse<MemoryVersion> redact(
        String memoryStoreId, String memoryVersionId, RequestOptions options) {
      return Futures.await(async.withResponse().redact(memoryStoreId, memoryVersionId, options));
    }
  }
}
