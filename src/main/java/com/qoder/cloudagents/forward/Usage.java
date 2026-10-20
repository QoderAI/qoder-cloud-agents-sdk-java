package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Usage {
  private final AsyncUsage async;

  public Usage(AsyncUsage async) {
    this.async = async;
  }

  public IdentityUsageListResponse listIdentities(UsageListParams params) {
    return listIdentities(params, RequestOptions.DEFAULT);
  }

  public IdentityUsageListResponse listIdentities(UsageListParams params, RequestOptions options) {
    return Futures.await(async.listIdentities(params, options));
  }

  public TemplateUsageListResponse listTemplates(UsageListParams params) {
    return listTemplates(params, RequestOptions.DEFAULT);
  }

  public TemplateUsageListResponse listTemplates(UsageListParams params, RequestOptions options) {
    return Futures.await(async.listTemplates(params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<IdentityUsageListResponse> listIdentities(UsageListParams params) {
      return listIdentities(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<IdentityUsageListResponse> listIdentities(
        UsageListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().listIdentities(params, options));
    }

    public ApiResponse<TemplateUsageListResponse> listTemplates(UsageListParams params) {
      return listTemplates(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<TemplateUsageListResponse> listTemplates(
        UsageListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().listTemplates(params, options));
    }
  }
}
