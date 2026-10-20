package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.ApiResponse;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.core.internal.Futures;

public final class Templates {
  private final AsyncTemplates async;

  public Templates(AsyncTemplates async) {
    this.async = async;
  }

  public Template retrieve(String id) {
    return retrieve(id, RequestOptions.DEFAULT);
  }

  public Template retrieve(String id, RequestOptions options) {
    return Futures.await(async.retrieve(id, options));
  }

  public Template create(TemplateCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Template create(TemplateCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public Template update(String id, TemplateUpdateParams params) {
    return update(id, params, RequestOptions.DEFAULT);
  }

  public Template update(String id, TemplateUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(id, params, options));
  }

  public Template archive(String id) {
    return archive(id, RequestOptions.DEFAULT);
  }

  public Template archive(String id, RequestOptions options) {
    return Futures.await(async.archive(id, options));
  }

  public Template clone(String id, TemplateCloneParams params) {
    return clone(id, params, RequestOptions.DEFAULT);
  }

  public Template clone(String id, TemplateCloneParams params, RequestOptions options) {
    return Futures.await(async.clone(id, params, options));
  }

  public TemplateListResponse list() {
    return list(TemplateListParams.builder().build());
  }

  public TemplateListResponse list(TemplateListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public TemplateListResponse list(TemplateListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {

    public ApiResponse<Template> retrieve(String id) {
      return retrieve(id, RequestOptions.DEFAULT);
    }

    public ApiResponse<Template> retrieve(String id, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(id, options));
    }

    public ApiResponse<Template> create(TemplateCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Template> create(TemplateCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<Template> update(String id, TemplateUpdateParams params) {
      return update(id, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Template> update(
        String id, TemplateUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(id, params, options));
    }

    public ApiResponse<Template> archive(String id) {
      return archive(id, RequestOptions.DEFAULT);
    }

    public ApiResponse<Template> archive(String id, RequestOptions options) {
      return Futures.await(async.withResponse().archive(id, options));
    }

    public ApiResponse<Template> clone(String id, TemplateCloneParams params) {
      return clone(id, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Template> clone(
        String id, TemplateCloneParams params, RequestOptions options) {
      return Futures.await(async.withResponse().clone(id, params, options));
    }

    public ApiResponse<TemplateListResponse> list(TemplateListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<TemplateListResponse> list(
        TemplateListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }
  }
}
