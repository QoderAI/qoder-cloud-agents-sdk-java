package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncEnvironmentWork {
  private final ClientEngine engine;

  public AsyncEnvironmentWork(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<WorkItem> ack(String environmentId, String workId) {
    return ack(environmentId, workId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WorkItem> ack(
      String environmentId, String workId, RequestOptions options) {
    return Futures.map(withResponse().ack(environmentId, workId, options), ApiResponse::data);
  }

  public CompletableFuture<WorkItem> retrieve(String environmentId, String workId) {
    return retrieve(environmentId, workId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WorkItem> retrieve(
      String environmentId, String workId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(environmentId, workId, options), ApiResponse::data);
  }

  public CompletableFuture<WorkHeartbeat> heartbeat(String environmentId, String workId) {
    return heartbeat(environmentId, workId, WorkHeartbeatParams.builder().build());
  }

  public CompletableFuture<WorkHeartbeat> heartbeat(
      String environmentId, String workId, RequestOptions options) {
    return heartbeat(environmentId, workId, WorkHeartbeatParams.builder().build(), options);
  }

  public CompletableFuture<WorkHeartbeat> heartbeat(
      String environmentId, String workId, WorkHeartbeatParams params) {
    return heartbeat(environmentId, workId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WorkHeartbeat> heartbeat(
      String environmentId, String workId, WorkHeartbeatParams params, RequestOptions options) {
    return Futures.map(
        withResponse().heartbeat(environmentId, workId, params, options), ApiResponse::data);
  }

  public CompletableFuture<WorkListResponse> list(String environmentId) {
    return list(environmentId, WorkListParams.builder().build());
  }

  public CompletableFuture<WorkListResponse> list(String environmentId, RequestOptions options) {
    return list(environmentId, WorkListParams.builder().build(), options);
  }

  public CompletableFuture<WorkListResponse> list(String environmentId, WorkListParams params) {
    return list(environmentId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WorkListResponse> list(
      String environmentId, WorkListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(environmentId, params, options), ApiResponse::data);
  }

  public CompletableFuture<WorkItem> poll(String environmentId) {
    return poll(environmentId, WorkPollParams.builder().build());
  }

  public CompletableFuture<WorkItem> poll(String environmentId, RequestOptions options) {
    return poll(environmentId, WorkPollParams.builder().build(), options);
  }

  public CompletableFuture<WorkItem> poll(String environmentId, WorkPollParams params) {
    return poll(environmentId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WorkItem> poll(
      String environmentId, WorkPollParams params, RequestOptions options) {
    return Futures.map(withResponse().poll(environmentId, params, options), ApiResponse::data);
  }

  public CompletableFuture<WorkQueueStats> stats(String environmentId) {
    return stats(environmentId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WorkQueueStats> stats(String environmentId, RequestOptions options) {
    return Futures.map(withResponse().stats(environmentId, options), ApiResponse::data);
  }

  public CompletableFuture<WorkItem> stop(String environmentId, String workId) {
    return stop(environmentId, workId, WorkStopParams.builder().build());
  }

  public CompletableFuture<WorkItem> stop(
      String environmentId, String workId, RequestOptions options) {
    return stop(environmentId, workId, WorkStopParams.builder().build(), options);
  }

  public CompletableFuture<WorkItem> stop(
      String environmentId, String workId, WorkStopParams params) {
    return stop(environmentId, workId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WorkItem> stop(
      String environmentId, String workId, WorkStopParams params, RequestOptions options) {
    return Futures.map(
        withResponse().stop(environmentId, workId, params, options), ApiResponse::data);
  }

  public CompletableFuture<WorkItem> update(
      String environmentId, String workId, WorkUpdateParams params) {
    return update(environmentId, workId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WorkItem> update(
      String environmentId, String workId, WorkUpdateParams params, RequestOptions options) {
    return Futures.map(
        withResponse().update(environmentId, workId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<WorkItem>> ack(String environmentId, String workId) {
      return ack(environmentId, workId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WorkItem>> ack(
        String environmentId, String workId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"environments", environmentId, "work", workId, "ack"},
              Collections.emptyMap(),
              null,
              false,
              options.toBuilder().maxRetries(0).build()),
          r -> r.map(v -> v.isNull() ? null : new WorkItem(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<WorkItem>> retrieve(String environmentId, String workId) {
      return retrieve(environmentId, workId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WorkItem>> retrieve(
        String environmentId, String workId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"environments", environmentId, "work", workId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new WorkItem(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<WorkHeartbeat>> heartbeat(
        String environmentId, String workId) {
      return heartbeat(environmentId, workId, WorkHeartbeatParams.builder().build());
    }

    public CompletableFuture<ApiResponse<WorkHeartbeat>> heartbeat(
        String environmentId, String workId, RequestOptions options) {
      return heartbeat(environmentId, workId, WorkHeartbeatParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<WorkHeartbeat>> heartbeat(
        String environmentId, String workId, WorkHeartbeatParams params) {
      return heartbeat(environmentId, workId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WorkHeartbeat>> heartbeat(
        String environmentId, String workId, WorkHeartbeatParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"environments", environmentId, "work", workId, "heartbeat"},
              params.query(),
              null,
              false,
              options.toBuilder().maxRetries(0).build()),
          r -> r.map(v -> v.isNull() ? null : new WorkHeartbeat(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<WorkListResponse>> list(String environmentId) {
      return list(environmentId, WorkListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<WorkListResponse>> list(
        String environmentId, RequestOptions options) {
      return list(environmentId, WorkListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<WorkListResponse>> list(
        String environmentId, WorkListParams params) {
      return list(environmentId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WorkListResponse>> list(
        String environmentId, WorkListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine,
          new String[] {"environments", environmentId, "work"},
          params.query(),
          options,
          WorkListResponse::new);
    }

    public CompletableFuture<ApiResponse<WorkItem>> poll(String environmentId) {
      return poll(environmentId, WorkPollParams.builder().build());
    }

    public CompletableFuture<ApiResponse<WorkItem>> poll(
        String environmentId, RequestOptions options) {
      return poll(environmentId, WorkPollParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<WorkItem>> poll(
        String environmentId, WorkPollParams params) {
      return poll(environmentId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WorkItem>> poll(
        String environmentId, WorkPollParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"environments", environmentId, "work", "poll"},
              params.query(),
              null,
              false,
              options.toBuilder().maxRetries(0).build()),
          r -> r.map(v -> v.isNull() ? null : new WorkItem(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<WorkQueueStats>> stats(String environmentId) {
      return stats(environmentId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WorkQueueStats>> stats(
        String environmentId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"environments", environmentId, "work", "stats"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new WorkQueueStats(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<WorkItem>> stop(String environmentId, String workId) {
      return stop(environmentId, workId, WorkStopParams.builder().build());
    }

    public CompletableFuture<ApiResponse<WorkItem>> stop(
        String environmentId, String workId, RequestOptions options) {
      return stop(environmentId, workId, WorkStopParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<WorkItem>> stop(
        String environmentId, String workId, WorkStopParams params) {
      return stop(environmentId, workId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WorkItem>> stop(
        String environmentId, String workId, WorkStopParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"environments", environmentId, "work", workId, "stop"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new WorkItem(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<WorkItem>> update(
        String environmentId, String workId, WorkUpdateParams params) {
      return update(environmentId, workId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WorkItem>> update(
        String environmentId, String workId, WorkUpdateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"environments", environmentId, "work", workId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new WorkItem(v, r.requestId())));
    }
  }
}
