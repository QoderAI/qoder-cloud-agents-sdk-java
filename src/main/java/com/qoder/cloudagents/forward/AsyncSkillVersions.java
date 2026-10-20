package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncSkillVersions {
  private final ClientEngine engine;

  public AsyncSkillVersions(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<SkillVersionListResponse> list(String skillId) {
    return list(skillId, SkillVersionListParams.builder().build());
  }

  public CompletableFuture<SkillVersionListResponse> list(String skillId, RequestOptions options) {
    return list(skillId, SkillVersionListParams.builder().build(), options);
  }

  public CompletableFuture<SkillVersionListResponse> list(
      String skillId, SkillVersionListParams params) {
    return list(skillId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SkillVersionListResponse> list(
      String skillId, SkillVersionListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(skillId, params, options), ApiResponse::data);
  }

  public CompletableFuture<SkillVersion> create(String skillId, SkillVersionCreateParams params) {
    return create(skillId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SkillVersion> create(
      String skillId, SkillVersionCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(skillId, params, options), ApiResponse::data);
  }

  public CompletableFuture<SkillVersion> retrieve(String skillId, String version) {
    return retrieve(skillId, version, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SkillVersion> retrieve(
      String skillId, String version, RequestOptions options) {
    return Futures.map(withResponse().retrieve(skillId, version, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedSkillVersion> delete(String skillId, String version) {
    return delete(skillId, version, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedSkillVersion> delete(
      String skillId, String version, RequestOptions options) {
    return Futures.map(withResponse().delete(skillId, version, options), ApiResponse::data);
  }

  public CompletableFuture<FileDownload> download(String skillId, String version) {
    return download(skillId, version, RequestOptions.DEFAULT);
  }

  public CompletableFuture<FileDownload> download(
      String skillId, String version, RequestOptions options) {
    return Futures.mapOwned(
        withResponse().download(skillId, version, options),
        ApiResponse::data,
        r -> r.data().close());
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<SkillVersionListResponse>> list(String skillId) {
      return list(skillId, SkillVersionListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<SkillVersionListResponse>> list(
        String skillId, RequestOptions options) {
      return list(skillId, SkillVersionListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<SkillVersionListResponse>> list(
        String skillId, SkillVersionListParams params) {
      return list(skillId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SkillVersionListResponse>> list(
        String skillId, SkillVersionListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      if (options.query().containsKey("after_id") || options.query().containsKey("before_id"))
        throw new ConfigurationException("Skill versions use page only");
      return CursorRequests.list(
          engine,
          new String[] {"skills", skillId, "versions"},
          params.query(),
          options,
          SkillVersionListResponse::new);
    }

    public CompletableFuture<ApiResponse<SkillVersion>> create(
        String skillId, SkillVersionCreateParams params) {
      return create(skillId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SkillVersion>> create(
        String skillId, SkillVersionCreateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      Uploads upload =
          new Uploads(Collections.singletonMap("files", params.files()), params.fields());
      try {
        CompletableFuture<ApiResponse<JsonValue>> pending =
            engine.requestBody(
                "POST",
                new String[] {"skills", skillId, "versions"},
                Collections.emptyMap(),
                upload.body(),
                upload.replayable(),
                options);
        pending.whenComplete((v, e) -> upload.close());
        return Futures.map(pending, r -> r.map(v -> new SkillVersion(v, r.requestId())));
      } catch (RuntimeException e) {
        upload.close();
        throw e;
      }
    }

    public CompletableFuture<ApiResponse<SkillVersion>> retrieve(String skillId, String version) {
      return retrieve(skillId, version, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SkillVersion>> retrieve(
        String skillId, String version, RequestOptions options) {
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"skills", skillId, "versions", version},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new SkillVersion(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedSkillVersion>> delete(
        String skillId, String version) {
      return delete(skillId, version, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedSkillVersion>> delete(
        String skillId, String version, RequestOptions options) {
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"skills", skillId, "versions", version},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedSkillVersion(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<FileDownload>> download(String skillId, String version) {
      return download(skillId, version, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<FileDownload>> download(
        String skillId, String version, RequestOptions options) {
      return engine.downloadContent(
          new String[] {"skills", skillId, "versions", version, "content"}, options);
    }
  }
}
