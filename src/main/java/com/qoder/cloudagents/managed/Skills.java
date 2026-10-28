package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Skills {
  private final AsyncSkills async;

  public Skills(AsyncSkills async) {
    this.async = async;
  }

  public SkillVersions versions() {
    return new SkillVersions(async.versions());
  }

  public SkillListResponse list() {
    return list(SkillListParams.builder().build());
  }

  public SkillListResponse list(RequestOptions options) {
    return list(SkillListParams.builder().build(), options);
  }

  public SkillListResponse list(SkillListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public SkillListResponse list(SkillListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public Skill create(SkillCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Skill create(SkillCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public Skill retrieve(String skillId) {
    return retrieve(skillId, SkillRetrieveParams.builder().build());
  }

  public Skill retrieve(String skillId, RequestOptions options) {
    return retrieve(skillId, SkillRetrieveParams.builder().build(), options);
  }

  public Skill retrieve(String skillId, SkillRetrieveParams params) {
    return retrieve(skillId, params, RequestOptions.DEFAULT);
  }

  public Skill retrieve(String skillId, SkillRetrieveParams params, RequestOptions options) {
    return Futures.await(async.retrieve(skillId, params, options));
  }

  /**
   * @deprecated Append an immutable Skill version instead.
   */
  @Deprecated
  public Skill update(String skillId, SkillUpdateParams params) {
    return update(skillId, params, RequestOptions.DEFAULT);
  }

  /**
   * @deprecated Append an immutable Skill version instead.
   */
  @Deprecated
  public Skill update(String skillId, SkillUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(skillId, params, options));
  }

  public DeletedSkill delete(String skillId) {
    return delete(skillId, RequestOptions.DEFAULT);
  }

  public DeletedSkill delete(String skillId, RequestOptions options) {
    return Futures.await(async.delete(skillId, options));
  }

  public SkillListResponse search(SkillSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public SkillListResponse search(SkillSearchParams params, RequestOptions options) {
    return Futures.await(async.search(params, options));
  }

  public SkillListResponse search() {
    return search(SkillSearchParams.builder().build());
  }

  public SkillListResponse search(RequestOptions options) {
    return search(SkillSearchParams.builder().build(), options);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<SkillListResponse> search() {
      return search(SkillSearchParams.builder().build());
    }

    public ApiResponse<SkillListResponse> search(RequestOptions options) {
      return search(SkillSearchParams.builder().build(), options);
    }

    public ApiResponse<SkillListResponse> search(SkillSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SkillListResponse> search(SkillSearchParams params, RequestOptions options) {
      return Futures.await(async.withResponse().search(params, options));
    }

    public ApiResponse<SkillListResponse> list() {
      return list(SkillListParams.builder().build());
    }

    public ApiResponse<SkillListResponse> list(RequestOptions options) {
      return list(SkillListParams.builder().build(), options);
    }

    public ApiResponse<SkillListResponse> list(SkillListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<SkillListResponse> list(SkillListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<Skill> create(SkillCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Skill> create(SkillCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<Skill> retrieve(String skillId) {
      return retrieve(skillId, SkillRetrieveParams.builder().build());
    }

    public ApiResponse<Skill> retrieve(String skillId, RequestOptions options) {
      return retrieve(skillId, SkillRetrieveParams.builder().build(), options);
    }

    public ApiResponse<Skill> retrieve(String skillId, SkillRetrieveParams params) {
      return retrieve(skillId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Skill> retrieve(
        String skillId, SkillRetrieveParams params, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(skillId, params, options));
    }

    /**
     * @deprecated Append an immutable Skill version instead.
     */
    @Deprecated
    public ApiResponse<Skill> update(String skillId, SkillUpdateParams params) {
      return update(skillId, params, RequestOptions.DEFAULT);
    }

    /**
     * @deprecated Append an immutable Skill version instead.
     */
    @Deprecated
    public ApiResponse<Skill> update(
        String skillId, SkillUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(skillId, params, options));
    }

    public ApiResponse<DeletedSkill> delete(String skillId) {
      return delete(skillId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedSkill> delete(String skillId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(skillId, options));
    }
  }
}
