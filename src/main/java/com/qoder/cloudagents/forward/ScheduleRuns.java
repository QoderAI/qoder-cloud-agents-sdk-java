package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class ScheduleRuns {
  private final AsyncScheduleRuns async;

  public ScheduleRuns(AsyncScheduleRuns async) {
    this.async = async;
  }

  public ScheduleRunListResponse list(ScheduleRunListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public ScheduleRunListResponse list(ScheduleRunListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public ScheduleRun retrieve(String runId) {
    return retrieve(runId, ScheduleRunRetrieveParams.builder().build());
  }

  public ScheduleRun retrieve(String runId, RequestOptions options) {
    return retrieve(runId, ScheduleRunRetrieveParams.builder().build(), options);
  }

  public ScheduleRun retrieve(String runId, ScheduleRunRetrieveParams params) {
    return retrieve(runId, params, RequestOptions.DEFAULT);
  }

  public ScheduleRun retrieve(
      String runId, ScheduleRunRetrieveParams params, RequestOptions options) {
    return Futures.await(async.retrieve(runId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<ScheduleRunListResponse> list(ScheduleRunListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<ScheduleRunListResponse> list(
        ScheduleRunListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<ScheduleRun> retrieve(String runId) {
      return retrieve(runId, ScheduleRunRetrieveParams.builder().build());
    }

    public ApiResponse<ScheduleRun> retrieve(String runId, RequestOptions options) {
      return retrieve(runId, ScheduleRunRetrieveParams.builder().build(), options);
    }

    public ApiResponse<ScheduleRun> retrieve(String runId, ScheduleRunRetrieveParams params) {
      return retrieve(runId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<ScheduleRun> retrieve(
        String runId, ScheduleRunRetrieveParams params, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(runId, params, options));
    }
  }
}
