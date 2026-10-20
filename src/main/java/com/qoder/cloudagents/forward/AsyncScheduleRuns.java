package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncScheduleRuns {
  private final ClientEngine engine;

  public AsyncScheduleRuns(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<ScheduleRunListResponse> list(ScheduleRunListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ScheduleRunListResponse> list(
      ScheduleRunListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<ScheduleRun> retrieve(String runId) {
    return retrieve(runId, ScheduleRunRetrieveParams.builder().build());
  }

  public CompletableFuture<ScheduleRun> retrieve(String runId, RequestOptions options) {
    return retrieve(runId, ScheduleRunRetrieveParams.builder().build(), options);
  }

  public CompletableFuture<ScheduleRun> retrieve(String runId, ScheduleRunRetrieveParams params) {
    return retrieve(runId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ScheduleRun> retrieve(
      String runId, ScheduleRunRetrieveParams params, RequestOptions options) {
    return Futures.map(withResponse().retrieve(runId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<ScheduleRunListResponse>> list(
        ScheduleRunListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ScheduleRunListResponse>> list(
        ScheduleRunListParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      if (options.query().containsKey("page"))
        throw new ConfigurationException("Unsupported pagination parameter for ScheduleRuns");
      if (options.query().containsKey("identity_id")
          && options.query().get("identity_id").trim().isEmpty())
        throw new ConfigurationException("identity_id is required");
      return CursorRequests.list(
          engine,
          new String[] {"schedule_runs"},
          params.query(),
          options,
          ScheduleRunListResponse::new);
    }

    public CompletableFuture<ApiResponse<ScheduleRun>> retrieve(String runId) {
      return retrieve(runId, ScheduleRunRetrieveParams.builder().build());
    }

    public CompletableFuture<ApiResponse<ScheduleRun>> retrieve(
        String runId, RequestOptions options) {
      return retrieve(runId, ScheduleRunRetrieveParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<ScheduleRun>> retrieve(
        String runId, ScheduleRunRetrieveParams params) {
      return retrieve(runId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ScheduleRun>> retrieve(
        String runId, ScheduleRunRetrieveParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "GET", new String[] {"schedule_runs", runId}, params.query(), null, true, options),
          r -> r.map(v -> new ScheduleRun(v, r.requestId())));
    }
  }
}
