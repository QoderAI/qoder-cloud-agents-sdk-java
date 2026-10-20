package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class SkillVersions {
  private final AsyncSkillVersions async;

  public SkillVersions(AsyncSkillVersions async) {
    this.async = async;
  }

  public SkillVersionListResponse list(String skillId) {
    return list(skillId, SkillVersionListParams.builder().build());
  }

  public SkillVersionListResponse list(String skillId, RequestOptions options) {
    return list(skillId, SkillVersionListParams.builder().build(), options);
  }

  public SkillVersionListResponse list(String skillId, SkillVersionListParams params) {
    return list(skillId, params, RequestOptions.DEFAULT);
  }

  public SkillVersionListResponse list(
      String skillId, SkillVersionListParams params, RequestOptions options) {
    return Futures.await(async.list(skillId, params, options));
  }

  public SkillVersion create(String skillId, SkillVersionCreateParams params) {
    return create(skillId, params, RequestOptions.DEFAULT);
  }

  public SkillVersion create(
      String skillId, SkillVersionCreateParams params, RequestOptions options) {
    return Futures.await(async.create(skillId, params, options));
  }

  public SkillVersion retrieve(String skillId, String version) {
    return retrieve(skillId, version, RequestOptions.DEFAULT);
  }

  public SkillVersion retrieve(String skillId, String version, RequestOptions options) {
    return Futures.await(async.retrieve(skillId, version, options));
  }

  public DeletedSkillVersion delete(String skillId, String version) {
    return delete(skillId, version, RequestOptions.DEFAULT);
  }

  public DeletedSkillVersion delete(String skillId, String version, RequestOptions options) {
    return Futures.await(async.delete(skillId, version, options));
  }

  public FileDownload download(String skillId, String version) {
    return download(skillId, version, RequestOptions.DEFAULT);
  }

  public FileDownload download(String skillId, String version, RequestOptions options) {
    return Futures.await(async.download(skillId, version, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<SkillVersionListResponse> list(String skillId) {
      return list(skillId, SkillVersionListParams.builder().build());
    }

    public ApiResponse<SkillVersionListResponse> list(String skillId, RequestOptions options) {
      return list(skillId, SkillVersionListParams.builder().build(), options);
    }

    public ApiResponse<SkillVersionListResponse> list(
        String skillId, SkillVersionListParams params) {
      return list(skillId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SkillVersionListResponse> list(
        String skillId, SkillVersionListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(skillId, params, options));
    }

    public ApiResponse<SkillVersion> create(String skillId, SkillVersionCreateParams params) {
      return create(skillId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SkillVersion> create(
        String skillId, SkillVersionCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(skillId, params, options));
    }

    public ApiResponse<SkillVersion> retrieve(String skillId, String version) {
      return retrieve(skillId, version, RequestOptions.DEFAULT);
    }

    public ApiResponse<SkillVersion> retrieve(
        String skillId, String version, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(skillId, version, options));
    }

    public ApiResponse<DeletedSkillVersion> delete(String skillId, String version) {
      return delete(skillId, version, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedSkillVersion> delete(
        String skillId, String version, RequestOptions options) {
      return Futures.await(async.withResponse().delete(skillId, version, options));
    }

    public ApiResponse<FileDownload> download(String skillId, String version) {
      return download(skillId, version, RequestOptions.DEFAULT);
    }

    public ApiResponse<FileDownload> download(
        String skillId, String version, RequestOptions options) {
      return Futures.await(async.withResponse().download(skillId, version, options));
    }
  }
}
