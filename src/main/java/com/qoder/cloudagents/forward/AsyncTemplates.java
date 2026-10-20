package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.ClientEngine;
import com.qoder.cloudagents.core.internal.CursorRequests;
import com.qoder.cloudagents.core.internal.Futures;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncTemplates {
  private final ClientEngine engine;

  public AsyncTemplates(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<Template> retrieve(String id) {
    return retrieve(id, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Template> retrieve(String id, RequestOptions options) {
    return Futures.map(withResponse().retrieve(id, options), ApiResponse::data);
  }

  public CompletableFuture<Template> create(TemplateCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Template> create(TemplateCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<Template> update(String id, TemplateUpdateParams params) {
    return update(id, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Template> update(
      String id, TemplateUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(id, params, options), ApiResponse::data);
  }

  public CompletableFuture<Template> archive(String id) {
    return archive(id, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Template> archive(String id, RequestOptions options) {
    return Futures.map(withResponse().archive(id, options), ApiResponse::data);
  }

  public CompletableFuture<Template> clone(String id, TemplateCloneParams params) {
    return clone(id, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Template> clone(
      String id, TemplateCloneParams params, RequestOptions options) {
    return Futures.map(withResponse().clone(id, params, options), ApiResponse::data);
  }

  public CompletableFuture<TemplateListResponse> list() {
    return list(TemplateListParams.builder().build());
  }

  public CompletableFuture<TemplateListResponse> list(TemplateListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<TemplateListResponse> list(
      TemplateListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {

    public CompletableFuture<ApiResponse<Template>> retrieve(String id) {
      return retrieve(id, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Template>> retrieve(String id, RequestOptions options) {

      return Futures.map(
          engine.request(
              "GET", new String[] {"templates", id}, Collections.emptyMap(), null, true, options),
          response -> response.map(value -> new Template(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Template>> create(TemplateCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Template>> create(
        TemplateCreateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Template parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"templates"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new Template(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Template>> update(String id, TemplateUpdateParams params) {
      return update(id, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Template>> update(
        String id, TemplateUpdateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Template parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"templates", id},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new Template(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Template>> archive(String id) {
      return archive(id, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Template>> archive(String id, RequestOptions options) {

      return Futures.map(
          engine.request(
              "POST",
              new String[] {"templates", id, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          response -> response.map(value -> new Template(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<Template>> clone(String id, TemplateCloneParams params) {
      return clone(id, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Template>> clone(
        String id, TemplateCloneParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Template parameters must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"templates", id, "clone"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          response -> response.map(value -> new Template(value, response.requestId())));
    }

    public CompletableFuture<ApiResponse<TemplateListResponse>> list(TemplateListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<TemplateListResponse>> list(
        TemplateListParams params, RequestOptions options) {
      if (params == null || options == null)
        throw new ConfigurationException("List parameters and options must not be null");
      return CursorRequests.list(
          engine, new String[] {"templates"}, params.query(), options, TemplateListResponse::new);
    }
  }
}
