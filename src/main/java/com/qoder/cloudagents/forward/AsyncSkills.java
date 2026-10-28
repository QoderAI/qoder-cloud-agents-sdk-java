package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncSkills {
  private final ClientEngine engine;

  public AsyncSkills(ClientEngine engine) {
    this.engine = engine;
  }

  public AsyncSkillVersions versions() {
    return new AsyncSkillVersions(engine);
  }

  public CompletableFuture<SkillListResponse> list() {
    return list(SkillListParams.builder().build());
  }

  public CompletableFuture<SkillListResponse> list(RequestOptions options) {
    return list(SkillListParams.builder().build(), options);
  }

  public CompletableFuture<SkillListResponse> list(SkillListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SkillListResponse> list(SkillListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<Skill> create(SkillCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Skill> create(SkillCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<Skill> retrieve(String skillId) {
    return retrieve(skillId, SkillRetrieveParams.builder().build());
  }

  public CompletableFuture<Skill> retrieve(String skillId, RequestOptions options) {
    return retrieve(skillId, SkillRetrieveParams.builder().build(), options);
  }

  public CompletableFuture<Skill> retrieve(String skillId, SkillRetrieveParams params) {
    return retrieve(skillId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Skill> retrieve(
      String skillId, SkillRetrieveParams params, RequestOptions options) {
    return Futures.map(withResponse().retrieve(skillId, params, options), ApiResponse::data);
  }

  /**
   * @deprecated Append an immutable Skill version instead.
   */
  @Deprecated
  public CompletableFuture<Skill> update(String skillId, SkillUpdateParams params) {
    return update(skillId, params, RequestOptions.DEFAULT);
  }

  /**
   * @deprecated Append an immutable Skill version instead.
   */
  @Deprecated
  public CompletableFuture<Skill> update(
      String skillId, SkillUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(skillId, params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedSkill> delete(String skillId) {
    return delete(skillId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedSkill> delete(String skillId, RequestOptions options) {
    return Futures.map(withResponse().delete(skillId, options), ApiResponse::data);
  }

  public CompletableFuture<SkillListResponse> search(SkillSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<SkillListResponse> search(
      SkillSearchParams params, RequestOptions options) {
    return Futures.map(withResponse().search(params, options), ApiResponse::data);
  }

  public CompletableFuture<SkillListResponse> search() {
    return search(SkillSearchParams.builder().build());
  }

  public CompletableFuture<SkillListResponse> search(RequestOptions options) {
    return search(SkillSearchParams.builder().build(), options);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<SkillListResponse>> search() {
      return search(SkillSearchParams.builder().build());
    }

    public CompletableFuture<ApiResponse<SkillListResponse>> search(RequestOptions options) {
      return search(SkillSearchParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<SkillListResponse>> search(SkillSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SkillListResponse>> search(
        SkillSearchParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return SearchRequests.search(
          engine,
          new String[] {"skills", "search"},
          params.toJsonValue(),
          params.query(),
          options,
          SkillListResponse::new);
    }

    public CompletableFuture<ApiResponse<SkillListResponse>> list() {
      return list(SkillListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<SkillListResponse>> list(RequestOptions options) {
      return list(SkillListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<SkillListResponse>> list(SkillListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<SkillListResponse>> list(
        SkillListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      Map<String, String> query = new LinkedHashMap<>(params.query());
      query.putAll(options.query());
      if (query.containsKey("source") && query.containsKey("before_id"))
        throw new ConfigurationException("source does not support beforeId");
      return CursorRequests.list(
          engine, new String[] {"skills"}, params.query(), options, SkillListResponse::new);
    }

    public CompletableFuture<ApiResponse<Skill>> create(SkillCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Skill>> create(
        SkillCreateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      Uploads upload =
          new Uploads(
              params.files().isEmpty()
                  ? Collections.singletonMap("file", Collections.singletonList(params.file()))
                  : Collections.singletonMap("files", params.files()),
              params.fields());
      try {
        CompletableFuture<ApiResponse<JsonValue>> pending =
            engine.requestBody(
                "POST",
                new String[] {"skills"},
                Collections.emptyMap(),
                upload.body(),
                upload.replayable(),
                options);
        pending.whenComplete((v, e) -> upload.close());
        return Futures.map(pending, r -> r.map(v -> new Skill(v, r.requestId())));
      } catch (RuntimeException e) {
        upload.close();
        throw e;
      }
    }

    public CompletableFuture<ApiResponse<Skill>> retrieve(String skillId) {
      return retrieve(skillId, SkillRetrieveParams.builder().build());
    }

    public CompletableFuture<ApiResponse<Skill>> retrieve(String skillId, RequestOptions options) {
      return retrieve(skillId, SkillRetrieveParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<Skill>> retrieve(
        String skillId, SkillRetrieveParams params) {
      return retrieve(skillId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Skill>> retrieve(
        String skillId, SkillRetrieveParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "GET", new String[] {"skills", skillId}, params.query(), null, true, options),
          r -> r.map(v -> new Skill(v, r.requestId())));
    }

    /**
     * @deprecated Append an immutable Skill version instead.
     */
    @Deprecated
    public CompletableFuture<ApiResponse<Skill>> update(String skillId, SkillUpdateParams params) {
      return update(skillId, params, RequestOptions.DEFAULT);
    }

    /**
     * @deprecated Append an immutable Skill version instead.
     */
    @Deprecated
    public CompletableFuture<ApiResponse<Skill>> update(
        String skillId, SkillUpdateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "PUT",
              new String[] {"skills", skillId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new Skill(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedSkill>> delete(String skillId) {
      return delete(skillId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedSkill>> delete(
        String skillId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"skills", skillId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedSkill(v, r.requestId())));
    }
  }
}
