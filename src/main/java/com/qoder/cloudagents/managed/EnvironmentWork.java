package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class EnvironmentWork {
  private final AsyncEnvironmentWork async;

  public EnvironmentWork(AsyncEnvironmentWork async) {
    this.async = async;
  }

  public WorkItem ack(String environmentId, String workId) {
    return ack(environmentId, workId, RequestOptions.DEFAULT);
  }

  public WorkItem ack(String environmentId, String workId, RequestOptions options) {
    return Futures.await(async.ack(environmentId, workId, options));
  }

  public WorkItem retrieve(String environmentId, String workId) {
    return retrieve(environmentId, workId, RequestOptions.DEFAULT);
  }

  public WorkItem retrieve(String environmentId, String workId, RequestOptions options) {
    return Futures.await(async.retrieve(environmentId, workId, options));
  }

  public WorkHeartbeat heartbeat(String environmentId, String workId) {
    return heartbeat(environmentId, workId, WorkHeartbeatParams.builder().build());
  }

  public WorkHeartbeat heartbeat(String environmentId, String workId, RequestOptions options) {
    return heartbeat(environmentId, workId, WorkHeartbeatParams.builder().build(), options);
  }

  public WorkHeartbeat heartbeat(String environmentId, String workId, WorkHeartbeatParams params) {
    return heartbeat(environmentId, workId, params, RequestOptions.DEFAULT);
  }

  public WorkHeartbeat heartbeat(
      String environmentId, String workId, WorkHeartbeatParams params, RequestOptions options) {
    return Futures.await(async.heartbeat(environmentId, workId, params, options));
  }

  public WorkListResponse list(String environmentId) {
    return list(environmentId, WorkListParams.builder().build());
  }

  public WorkListResponse list(String environmentId, RequestOptions options) {
    return list(environmentId, WorkListParams.builder().build(), options);
  }

  public WorkListResponse list(String environmentId, WorkListParams params) {
    return list(environmentId, params, RequestOptions.DEFAULT);
  }

  public WorkListResponse list(
      String environmentId, WorkListParams params, RequestOptions options) {
    return Futures.await(async.list(environmentId, params, options));
  }

  public WorkItem poll(String environmentId) {
    return poll(environmentId, WorkPollParams.builder().build());
  }

  public WorkItem poll(String environmentId, RequestOptions options) {
    return poll(environmentId, WorkPollParams.builder().build(), options);
  }

  public WorkItem poll(String environmentId, WorkPollParams params) {
    return poll(environmentId, params, RequestOptions.DEFAULT);
  }

  public WorkItem poll(String environmentId, WorkPollParams params, RequestOptions options) {
    return Futures.await(async.poll(environmentId, params, options));
  }

  public WorkQueueStats stats(String environmentId) {
    return stats(environmentId, RequestOptions.DEFAULT);
  }

  public WorkQueueStats stats(String environmentId, RequestOptions options) {
    return Futures.await(async.stats(environmentId, options));
  }

  public WorkItem stop(String environmentId, String workId) {
    return stop(environmentId, workId, WorkStopParams.builder().build());
  }

  public WorkItem stop(String environmentId, String workId, RequestOptions options) {
    return stop(environmentId, workId, WorkStopParams.builder().build(), options);
  }

  public WorkItem stop(String environmentId, String workId, WorkStopParams params) {
    return stop(environmentId, workId, params, RequestOptions.DEFAULT);
  }

  public WorkItem stop(
      String environmentId, String workId, WorkStopParams params, RequestOptions options) {
    return Futures.await(async.stop(environmentId, workId, params, options));
  }

  public WorkItem update(String environmentId, String workId, WorkUpdateParams params) {
    return update(environmentId, workId, params, RequestOptions.DEFAULT);
  }

  public WorkItem update(
      String environmentId, String workId, WorkUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(environmentId, workId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<WorkItem> ack(String environmentId, String workId) {
      return ack(environmentId, workId, RequestOptions.DEFAULT);
    }

    public ApiResponse<WorkItem> ack(String environmentId, String workId, RequestOptions options) {
      return Futures.await(async.withResponse().ack(environmentId, workId, options));
    }

    public ApiResponse<WorkItem> retrieve(String environmentId, String workId) {
      return retrieve(environmentId, workId, RequestOptions.DEFAULT);
    }

    public ApiResponse<WorkItem> retrieve(
        String environmentId, String workId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(environmentId, workId, options));
    }

    public ApiResponse<WorkHeartbeat> heartbeat(String environmentId, String workId) {
      return heartbeat(environmentId, workId, WorkHeartbeatParams.builder().build());
    }

    public ApiResponse<WorkHeartbeat> heartbeat(
        String environmentId, String workId, RequestOptions options) {
      return heartbeat(environmentId, workId, WorkHeartbeatParams.builder().build(), options);
    }

    public ApiResponse<WorkHeartbeat> heartbeat(
        String environmentId, String workId, WorkHeartbeatParams params) {
      return heartbeat(environmentId, workId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<WorkHeartbeat> heartbeat(
        String environmentId, String workId, WorkHeartbeatParams params, RequestOptions options) {
      return Futures.await(async.withResponse().heartbeat(environmentId, workId, params, options));
    }

    public ApiResponse<WorkListResponse> list(String environmentId) {
      return list(environmentId, WorkListParams.builder().build());
    }

    public ApiResponse<WorkListResponse> list(String environmentId, RequestOptions options) {
      return list(environmentId, WorkListParams.builder().build(), options);
    }

    public ApiResponse<WorkListResponse> list(String environmentId, WorkListParams params) {
      return list(environmentId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<WorkListResponse> list(
        String environmentId, WorkListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(environmentId, params, options));
    }

    public ApiResponse<WorkItem> poll(String environmentId) {
      return poll(environmentId, WorkPollParams.builder().build());
    }

    public ApiResponse<WorkItem> poll(String environmentId, RequestOptions options) {
      return poll(environmentId, WorkPollParams.builder().build(), options);
    }

    public ApiResponse<WorkItem> poll(String environmentId, WorkPollParams params) {
      return poll(environmentId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<WorkItem> poll(
        String environmentId, WorkPollParams params, RequestOptions options) {
      return Futures.await(async.withResponse().poll(environmentId, params, options));
    }

    public ApiResponse<WorkQueueStats> stats(String environmentId) {
      return stats(environmentId, RequestOptions.DEFAULT);
    }

    public ApiResponse<WorkQueueStats> stats(String environmentId, RequestOptions options) {
      return Futures.await(async.withResponse().stats(environmentId, options));
    }

    public ApiResponse<WorkItem> stop(String environmentId, String workId) {
      return stop(environmentId, workId, WorkStopParams.builder().build());
    }

    public ApiResponse<WorkItem> stop(String environmentId, String workId, RequestOptions options) {
      return stop(environmentId, workId, WorkStopParams.builder().build(), options);
    }

    public ApiResponse<WorkItem> stop(String environmentId, String workId, WorkStopParams params) {
      return stop(environmentId, workId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<WorkItem> stop(
        String environmentId, String workId, WorkStopParams params, RequestOptions options) {
      return Futures.await(async.withResponse().stop(environmentId, workId, params, options));
    }

    public ApiResponse<WorkItem> update(
        String environmentId, String workId, WorkUpdateParams params) {
      return update(environmentId, workId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<WorkItem> update(
        String environmentId, String workId, WorkUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(environmentId, workId, params, options));
    }
  }
}
