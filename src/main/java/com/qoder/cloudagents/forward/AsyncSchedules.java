package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncSchedules {
  private final ClientEngine engine;

  public AsyncSchedules(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<ScheduleListResponse> list() {
    return list(ScheduleListParams.builder().build());
  }

  public CompletableFuture<ScheduleListResponse> list(RequestOptions options) {
    return list(ScheduleListParams.builder().build(), options);
  }

  public CompletableFuture<ScheduleListResponse> list(ScheduleListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ScheduleListResponse> list(
      ScheduleListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<Schedule> create(ScheduleCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Schedule> create(ScheduleCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<ScheduleArchiveManyResponse> archiveMany(
      ScheduleArchiveManyParams params) {
    return archiveMany(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ScheduleArchiveManyResponse> archiveMany(
      ScheduleArchiveManyParams params, RequestOptions options) {
    return Futures.map(withResponse().archiveMany(params, options), ApiResponse::data);
  }

  public CompletableFuture<Schedule> retrieve(String scheduleId) {
    return retrieve(scheduleId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Schedule> retrieve(String scheduleId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(scheduleId, options), ApiResponse::data);
  }

  public CompletableFuture<Schedule> update(String scheduleId, ScheduleUpdateParams params) {
    return update(scheduleId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Schedule> update(
      String scheduleId, ScheduleUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(scheduleId, params, options), ApiResponse::data);
  }

  public CompletableFuture<Schedule> archive(String scheduleId) {
    return archive(scheduleId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Schedule> archive(String scheduleId, RequestOptions options) {
    return Futures.map(withResponse().archive(scheduleId, options), ApiResponse::data);
  }

  public CompletableFuture<Schedule> pause(String scheduleId) {
    return pause(scheduleId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Schedule> pause(String scheduleId, RequestOptions options) {
    return Futures.map(withResponse().pause(scheduleId, options), ApiResponse::data);
  }

  public CompletableFuture<ScheduleRun> run(String scheduleId) {
    return run(scheduleId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ScheduleRun> run(String scheduleId, RequestOptions options) {
    return Futures.map(withResponse().run(scheduleId, options), ApiResponse::data);
  }

  public CompletableFuture<Schedule> unpause(String scheduleId) {
    return unpause(scheduleId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Schedule> unpause(String scheduleId, RequestOptions options) {
    return Futures.map(withResponse().unpause(scheduleId, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<ScheduleListResponse>> list() {
      return list(ScheduleListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<ScheduleListResponse>> list(RequestOptions options) {
      return list(ScheduleListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<ScheduleListResponse>> list(ScheduleListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ScheduleListResponse>> list(
        ScheduleListParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      if (options.query().containsKey("page"))
        throw new ConfigurationException("Unsupported pagination parameter for Schedules");
      return CursorRequests.list(
          engine, new String[] {"schedules"}, params.query(), options, ScheduleListResponse::new);
    }

    public CompletableFuture<ApiResponse<Schedule>> create(ScheduleCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Schedule>> create(
        ScheduleCreateParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"schedules"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new Schedule(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<ScheduleArchiveManyResponse>> archiveMany(
        ScheduleArchiveManyParams params) {
      return archiveMany(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ScheduleArchiveManyResponse>> archiveMany(
        ScheduleArchiveManyParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"schedules", "archive"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new ScheduleArchiveManyResponse(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Schedule>> retrieve(String scheduleId) {
      return retrieve(scheduleId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Schedule>> retrieve(
        String scheduleId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"schedules", scheduleId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Schedule(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Schedule>> update(
        String scheduleId, ScheduleUpdateParams params) {
      return update(scheduleId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Schedule>> update(
        String scheduleId, ScheduleUpdateParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"schedules", scheduleId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new Schedule(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Schedule>> archive(String scheduleId) {
      return archive(scheduleId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Schedule>> archive(
        String scheduleId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"schedules", scheduleId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Schedule(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Schedule>> pause(String scheduleId) {
      return pause(scheduleId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Schedule>> pause(
        String scheduleId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"schedules", scheduleId, "pause"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Schedule(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<ScheduleRun>> run(String scheduleId) {
      return run(scheduleId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ScheduleRun>> run(
        String scheduleId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"schedules", scheduleId, "run"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new ScheduleRun(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Schedule>> unpause(String scheduleId) {
      return unpause(scheduleId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Schedule>> unpause(
        String scheduleId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"schedules", scheduleId, "unpause"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new Schedule(v, r.requestId())));
    }
  }
}
