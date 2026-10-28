package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncDecisions {
  private final ClientEngine engine;

  public AsyncDecisions(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<Decision> create(DecisionCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Decision> create(DecisionCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<Decision> retrieve(String decisionId) {
    return retrieve(decisionId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Decision> retrieve(String decisionId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(decisionId, options), ApiResponse::data);
  }

  public CompletableFuture<DecisionListResponse> list(DecisionListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DecisionListResponse> list(
      DecisionListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<DecisionListResponse> list() {
    return list(DecisionListParams.builder().build());
  }

  public CompletableFuture<DecisionListResponse> list(RequestOptions options) {
    return list(DecisionListParams.builder().build(), options);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<DecisionListResponse>> list() {
      return list(DecisionListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<DecisionListResponse>> list(RequestOptions options) {
      return list(DecisionListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<Decision>> create(DecisionCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Decision>> create(
        DecisionCreateParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"decisions"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new Decision(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Decision>> retrieve(String decisionId) {
      return retrieve(decisionId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Decision>> retrieve(
        String decisionId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"decisions", decisionId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Decision(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DecisionListResponse>> list(DecisionListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DecisionListResponse>> list(
        DecisionListParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return CursorRequests.list(
          engine, new String[] {"decisions"}, params.query(), options, DecisionListResponse::new);
    }
  }
}
