package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public final class AsyncFiles {
  private final ClientEngine engine;

  public AsyncFiles(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<FileListResponse> list() {
    return list(FileListParams.builder().build());
  }

  public CompletableFuture<FileListResponse> list(FileListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<FileListResponse> list(FileListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<FileMetadata> upload(FileUploadParams params) {
    return upload(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<FileMetadata> upload(FileUploadParams params, RequestOptions options) {
    return Futures.map(withResponse().upload(params, options), ApiResponse::data);
  }

  public CompletableFuture<FileMetadata> getMetadata(String fileId) {
    return getMetadata(fileId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<FileMetadata> getMetadata(String fileId, RequestOptions options) {
    return Futures.map(withResponse().getMetadata(fileId, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedFile> delete(String fileId) {
    return delete(fileId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedFile> delete(String fileId, RequestOptions options) {
    return Futures.map(withResponse().delete(fileId, options), ApiResponse::data);
  }

  public CompletableFuture<FileDownloadUrl> getDownloadUrl(String fileId) {
    return getDownloadUrl(fileId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<FileDownloadUrl> getDownloadUrl(String fileId, RequestOptions options) {
    return Futures.map(withResponse().getDownloadUrl(fileId, options), ApiResponse::data);
  }

  public CompletableFuture<FileDownload> download(String fileId) {
    return download(fileId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<FileDownload> download(String fileId, RequestOptions options) {
    return Futures.mapOwned(
        withResponse().download(fileId, options), ApiResponse::data, r -> r.data().close());
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<FileListResponse>> list() {
      return list(FileListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<FileListResponse>> list(FileListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<FileListResponse>> list(
        FileListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("List parameters are required");
      return CursorRequests.list(
          engine, new String[] {"files"}, params.query(), options, FileListResponse::new);
    }

    public CompletableFuture<ApiResponse<FileMetadata>> upload(FileUploadParams params) {
      return upload(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<FileMetadata>> upload(
        FileUploadParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Upload parameters are required");
      Uploads upload = new Uploads(params);
      try {
        CompletableFuture<ApiResponse<JsonValue>> pending =
            engine.requestBody(
                "POST",
                new String[] {"files"},
                Collections.emptyMap(),
                upload.body(),
                upload.replayable(),
                options);
        pending.whenComplete((v, e) -> upload.close());
        return Futures.map(pending, r -> r.map(v -> new FileMetadata(v, r.requestId())));
      } catch (RuntimeException e) {
        upload.close();
        throw e;
      }
    }

    public CompletableFuture<ApiResponse<FileMetadata>> getMetadata(String fileId) {
      return getMetadata(fileId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<FileMetadata>> getMetadata(
        String fileId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "GET", new String[] {"files", fileId}, Collections.emptyMap(), null, true, options),
          r -> r.map(v -> new FileMetadata(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedFile>> delete(String fileId) {
      return delete(fileId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedFile>> delete(
        String fileId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"files", fileId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedFile(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<FileDownloadUrl>> getDownloadUrl(String fileId) {
      return getDownloadUrl(fileId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<FileDownloadUrl>> getDownloadUrl(
        String fileId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"files", fileId, "content"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new FileDownloadUrl(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<FileDownload>> download(String fileId) {
      return download(fileId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<FileDownload>> download(
        String fileId, RequestOptions options) {
      return engine.download(new String[] {"files", fileId, "content"}, options);
    }
  }
}
