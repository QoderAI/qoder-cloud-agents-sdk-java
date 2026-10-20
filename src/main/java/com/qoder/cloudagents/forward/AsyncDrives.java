package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncDrives {
  private final ClientEngine engine;

  public AsyncDrives(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<DriveListEntriesResponse> listEntries(DriveListEntriesParams params) {
    return listEntries(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DriveListEntriesResponse> listEntries(
      DriveListEntriesParams params, RequestOptions options) {
    return Futures.map(withResponse().listEntries(params, options), ApiResponse::data);
  }

  public CompletableFuture<DriveUploadUrl> createUploadUrl(DriveCreateUploadUrlParams params) {
    return createUploadUrl(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DriveUploadUrl> createUploadUrl(
      DriveCreateUploadUrlParams params, RequestOptions options) {
    return Futures.map(withResponse().createUploadUrl(params, options), ApiResponse::data);
  }

  public CompletableFuture<DriveDownloadUrl> createDownloadUrl(
      DriveCreateDownloadUrlParams params) {
    return createDownloadUrl(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DriveDownloadUrl> createDownloadUrl(
      DriveCreateDownloadUrlParams params, RequestOptions options) {
    return Futures.map(withResponse().createDownloadUrl(params, options), ApiResponse::data);
  }

  public CompletableFuture<DriveDeleteEntryResponse> deleteEntry(DriveDeleteEntryParams params) {
    return deleteEntry(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DriveDeleteEntryResponse> deleteEntry(
      DriveDeleteEntryParams params, RequestOptions options) {
    return Futures.map(withResponse().deleteEntry(params, options), ApiResponse::data);
  }

  public CompletableFuture<DriveClearResponse> clear(DriveClearParams params) {
    return clear(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DriveClearResponse> clear(
      DriveClearParams params, RequestOptions options) {
    return Futures.map(withResponse().clear(params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<DriveListEntriesResponse>> listEntries(
        DriveListEntriesParams params) {
      return listEntries(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DriveListEntriesResponse>> listEntries(
        DriveListEntriesParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Parameters and options must not be null");
      return Futures.map(
          engine.request(
              "GET", new String[] {"drives", "entries"}, params.query(), null, true, options),
          r -> r.map(v -> new DriveListEntriesResponse(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DriveUploadUrl>> createUploadUrl(
        DriveCreateUploadUrlParams params) {
      return createUploadUrl(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DriveUploadUrl>> createUploadUrl(
        DriveCreateUploadUrlParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"drives", "upload-url"},
              params.query(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new DriveUploadUrl(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DriveDownloadUrl>> createDownloadUrl(
        DriveCreateDownloadUrlParams params) {
      return createDownloadUrl(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DriveDownloadUrl>> createDownloadUrl(
        DriveCreateDownloadUrlParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"drives", "download-url"},
              params.query(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new DriveDownloadUrl(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DriveDeleteEntryResponse>> deleteEntry(
        DriveDeleteEntryParams params) {
      return deleteEntry(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DriveDeleteEntryResponse>> deleteEntry(
        DriveDeleteEntryParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Parameters and options must not be null");
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"drives", "entries"},
              params.query(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new DriveDeleteEntryResponse(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DriveClearResponse>> clear(DriveClearParams params) {
      return clear(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DriveClearResponse>> clear(
        DriveClearParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Parameters and options must not be null");
      return Futures.map(
          engine.request("DELETE", new String[] {"drives"}, params.query(), null, true, options),
          r -> r.map(v -> new DriveClearResponse(v, r.requestId())));
    }
  }
}
