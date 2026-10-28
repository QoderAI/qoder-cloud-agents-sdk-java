package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;

public final class Files {
  private final AsyncFiles async;

  public Files(AsyncFiles async) {
    this.async = async;
  }

  public FileListResponse list() {
    return list(FileListParams.builder().build());
  }

  public FileListResponse list(FileListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public FileListResponse list(FileListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public FileMetadata upload(FileUploadParams params) {
    return upload(params, RequestOptions.DEFAULT);
  }

  public FileMetadata upload(FileUploadParams params, RequestOptions options) {
    return Futures.await(async.upload(params, options));
  }

  public FileMetadata getMetadata(String fileId) {
    return getMetadata(fileId, RequestOptions.DEFAULT);
  }

  public FileMetadata getMetadata(String fileId, RequestOptions options) {
    return Futures.await(async.getMetadata(fileId, options));
  }

  public DeletedFile delete(String fileId) {
    return delete(fileId, RequestOptions.DEFAULT);
  }

  public DeletedFile delete(String fileId, RequestOptions options) {
    return Futures.await(async.delete(fileId, options));
  }

  public FileDownloadUrl getDownloadUrl(String fileId) {
    return getDownloadUrl(fileId, RequestOptions.DEFAULT);
  }

  public FileDownloadUrl getDownloadUrl(String fileId, RequestOptions options) {
    return Futures.await(async.getDownloadUrl(fileId, options));
  }

  public FileDownload download(String fileId) {
    return download(fileId, RequestOptions.DEFAULT);
  }

  public FileDownload download(String fileId, RequestOptions options) {
    return Futures.await(async.download(fileId, options));
  }

  public FileListResponse search(FileSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public FileListResponse search(FileSearchParams params, RequestOptions options) {
    return Futures.await(async.search(params, options));
  }

  public FileListResponse search() {
    return search(FileSearchParams.builder().build());
  }

  public FileListResponse search(RequestOptions options) {
    return search(FileSearchParams.builder().build(), options);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<FileListResponse> search() {
      return search(FileSearchParams.builder().build());
    }

    public ApiResponse<FileListResponse> search(RequestOptions options) {
      return search(FileSearchParams.builder().build(), options);
    }

    public ApiResponse<FileListResponse> search(FileSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<FileListResponse> search(FileSearchParams params, RequestOptions options) {
      return Futures.await(async.withResponse().search(params, options));
    }

    public ApiResponse<FileListResponse> list() {
      return list(FileListParams.builder().build());
    }

    public ApiResponse<FileListResponse> list(FileListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<FileListResponse> list(FileListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<FileMetadata> upload(FileUploadParams params) {
      return upload(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<FileMetadata> upload(FileUploadParams params, RequestOptions options) {
      return Futures.await(async.withResponse().upload(params, options));
    }

    public ApiResponse<FileMetadata> getMetadata(String fileId) {
      return getMetadata(fileId, RequestOptions.DEFAULT);
    }

    public ApiResponse<FileMetadata> getMetadata(String fileId, RequestOptions options) {
      return Futures.await(async.withResponse().getMetadata(fileId, options));
    }

    public ApiResponse<DeletedFile> delete(String fileId) {
      return delete(fileId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedFile> delete(String fileId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(fileId, options));
    }

    public ApiResponse<FileDownloadUrl> getDownloadUrl(String fileId) {
      return getDownloadUrl(fileId, RequestOptions.DEFAULT);
    }

    public ApiResponse<FileDownloadUrl> getDownloadUrl(String fileId, RequestOptions options) {
      return Futures.await(async.withResponse().getDownloadUrl(fileId, options));
    }

    public ApiResponse<FileDownload> download(String fileId) {
      return download(fileId, RequestOptions.DEFAULT);
    }

    public ApiResponse<FileDownload> download(String fileId, RequestOptions options) {
      return Futures.await(async.withResponse().download(fileId, options));
    }
  }
}
