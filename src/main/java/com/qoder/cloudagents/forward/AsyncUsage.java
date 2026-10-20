package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncUsage {
  private final ClientEngine engine;

  public AsyncUsage(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<IdentityUsageListResponse> listIdentities(UsageListParams params) {
    return listIdentities(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<IdentityUsageListResponse> listIdentities(
      UsageListParams params, RequestOptions options) {
    return Futures.map(withResponse().listIdentities(params, options), ApiResponse::data);
  }

  public CompletableFuture<TemplateUsageListResponse> listTemplates(UsageListParams params) {
    return listTemplates(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<TemplateUsageListResponse> listTemplates(
      UsageListParams params, RequestOptions options) {
    return Futures.map(withResponse().listTemplates(params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<IdentityUsageListResponse>> listIdentities(
        UsageListParams params) {
      return listIdentities(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<IdentityUsageListResponse>> listIdentities(
        UsageListParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Parameters and options must not be null");
      Map<String, String> query = new LinkedHashMap<>(params.query());
      query.putAll(options.query());
      return CursorRequests.list(
          engine,
          new String[] {"usage", "identities"},
          UsageListParams.validate(query),
          options.toBuilder().clearQuery().build(),
          IdentityUsageListResponse::new);
    }

    public CompletableFuture<ApiResponse<TemplateUsageListResponse>> listTemplates(
        UsageListParams params) {
      return listTemplates(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<TemplateUsageListResponse>> listTemplates(
        UsageListParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Parameters and options must not be null");
      Map<String, String> query = new LinkedHashMap<>(params.query());
      query.putAll(options.query());
      return CursorRequests.list(
          engine,
          new String[] {"usage", "templates"},
          UsageListParams.validate(query),
          options.toBuilder().clearQuery().build(),
          TemplateUsageListResponse::new);
    }
  }
}
