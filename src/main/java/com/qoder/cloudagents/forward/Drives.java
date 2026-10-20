package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Drives {
  private final AsyncDrives async;

  public Drives(AsyncDrives async) {
    this.async = async;
  }

  public DriveListEntriesResponse listEntries(DriveListEntriesParams params) {
    return listEntries(params, RequestOptions.DEFAULT);
  }

  public DriveListEntriesResponse listEntries(
      DriveListEntriesParams params, RequestOptions options) {
    return Futures.await(async.listEntries(params, options));
  }

  public DriveUploadUrl createUploadUrl(DriveCreateUploadUrlParams params) {
    return createUploadUrl(params, RequestOptions.DEFAULT);
  }

  public DriveUploadUrl createUploadUrl(DriveCreateUploadUrlParams params, RequestOptions options) {
    return Futures.await(async.createUploadUrl(params, options));
  }

  public DriveDownloadUrl createDownloadUrl(DriveCreateDownloadUrlParams params) {
    return createDownloadUrl(params, RequestOptions.DEFAULT);
  }

  public DriveDownloadUrl createDownloadUrl(
      DriveCreateDownloadUrlParams params, RequestOptions options) {
    return Futures.await(async.createDownloadUrl(params, options));
  }

  public DriveDeleteEntryResponse deleteEntry(DriveDeleteEntryParams params) {
    return deleteEntry(params, RequestOptions.DEFAULT);
  }

  public DriveDeleteEntryResponse deleteEntry(
      DriveDeleteEntryParams params, RequestOptions options) {
    return Futures.await(async.deleteEntry(params, options));
  }

  public DriveClearResponse clear(DriveClearParams params) {
    return clear(params, RequestOptions.DEFAULT);
  }

  public DriveClearResponse clear(DriveClearParams params, RequestOptions options) {
    return Futures.await(async.clear(params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<DriveListEntriesResponse> listEntries(DriveListEntriesParams params) {
      return listEntries(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<DriveListEntriesResponse> listEntries(
        DriveListEntriesParams params, RequestOptions options) {
      return Futures.await(async.withResponse().listEntries(params, options));
    }

    public ApiResponse<DriveUploadUrl> createUploadUrl(DriveCreateUploadUrlParams params) {
      return createUploadUrl(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<DriveUploadUrl> createUploadUrl(
        DriveCreateUploadUrlParams params, RequestOptions options) {
      return Futures.await(async.withResponse().createUploadUrl(params, options));
    }

    public ApiResponse<DriveDownloadUrl> createDownloadUrl(DriveCreateDownloadUrlParams params) {
      return createDownloadUrl(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<DriveDownloadUrl> createDownloadUrl(
        DriveCreateDownloadUrlParams params, RequestOptions options) {
      return Futures.await(async.withResponse().createDownloadUrl(params, options));
    }

    public ApiResponse<DriveDeleteEntryResponse> deleteEntry(DriveDeleteEntryParams params) {
      return deleteEntry(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<DriveDeleteEntryResponse> deleteEntry(
        DriveDeleteEntryParams params, RequestOptions options) {
      return Futures.await(async.withResponse().deleteEntry(params, options));
    }

    public ApiResponse<DriveClearResponse> clear(DriveClearParams params) {
      return clear(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<DriveClearResponse> clear(DriveClearParams params, RequestOptions options) {
      return Futures.await(async.withResponse().clear(params, options));
    }
  }
}
