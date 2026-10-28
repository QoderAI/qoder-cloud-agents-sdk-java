package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Decisions {
  private final AsyncDecisions async;

  public Decisions(AsyncDecisions async) {
    this.async = async;
  }

  public Decision create(DecisionCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Decision create(DecisionCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public Decision retrieve(String decisionId) {
    return retrieve(decisionId, RequestOptions.DEFAULT);
  }

  public Decision retrieve(String decisionId, RequestOptions options) {
    return Futures.await(async.retrieve(decisionId, options));
  }

  public DecisionListResponse list(DecisionListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public DecisionListResponse list(DecisionListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public DecisionListResponse list() {
    return list(DecisionListParams.builder().build());
  }

  public DecisionListResponse list(RequestOptions options) {
    return list(DecisionListParams.builder().build(), options);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<DecisionListResponse> list() {
      return list(DecisionListParams.builder().build());
    }

    public ApiResponse<DecisionListResponse> list(RequestOptions options) {
      return list(DecisionListParams.builder().build(), options);
    }

    public ApiResponse<Decision> create(DecisionCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Decision> create(DecisionCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<Decision> retrieve(String decisionId) {
      return retrieve(decisionId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Decision> retrieve(String decisionId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(decisionId, options));
    }

    public ApiResponse<DecisionListResponse> list(DecisionListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<DecisionListResponse> list(
        DecisionListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }
  }
}
