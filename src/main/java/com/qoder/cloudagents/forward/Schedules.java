package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Schedules {
  private final AsyncSchedules async;

  public Schedules(AsyncSchedules async) {
    this.async = async;
  }

  public ScheduleListResponse list() {
    return list(ScheduleListParams.builder().build());
  }

  public ScheduleListResponse list(RequestOptions options) {
    return list(ScheduleListParams.builder().build(), options);
  }

  public ScheduleListResponse list(ScheduleListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public ScheduleListResponse list(ScheduleListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public Schedule create(ScheduleCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Schedule create(ScheduleCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public ScheduleArchiveManyResponse archiveMany(ScheduleArchiveManyParams params) {
    return archiveMany(params, RequestOptions.DEFAULT);
  }

  public ScheduleArchiveManyResponse archiveMany(
      ScheduleArchiveManyParams params, RequestOptions options) {
    return Futures.await(async.archiveMany(params, options));
  }

  public Schedule retrieve(String scheduleId) {
    return retrieve(scheduleId, RequestOptions.DEFAULT);
  }

  public Schedule retrieve(String scheduleId, RequestOptions options) {
    return Futures.await(async.retrieve(scheduleId, options));
  }

  public Schedule update(String scheduleId, ScheduleUpdateParams params) {
    return update(scheduleId, params, RequestOptions.DEFAULT);
  }

  public Schedule update(String scheduleId, ScheduleUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(scheduleId, params, options));
  }

  public Schedule archive(String scheduleId) {
    return archive(scheduleId, RequestOptions.DEFAULT);
  }

  public Schedule archive(String scheduleId, RequestOptions options) {
    return Futures.await(async.archive(scheduleId, options));
  }

  public Schedule pause(String scheduleId) {
    return pause(scheduleId, RequestOptions.DEFAULT);
  }

  public Schedule pause(String scheduleId, RequestOptions options) {
    return Futures.await(async.pause(scheduleId, options));
  }

  public ScheduleRun run(String scheduleId) {
    return run(scheduleId, RequestOptions.DEFAULT);
  }

  public ScheduleRun run(String scheduleId, RequestOptions options) {
    return Futures.await(async.run(scheduleId, options));
  }

  public Schedule unpause(String scheduleId) {
    return unpause(scheduleId, RequestOptions.DEFAULT);
  }

  public Schedule unpause(String scheduleId, RequestOptions options) {
    return Futures.await(async.unpause(scheduleId, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<ScheduleListResponse> list() {
      return list(ScheduleListParams.builder().build());
    }

    public ApiResponse<ScheduleListResponse> list(RequestOptions options) {
      return list(ScheduleListParams.builder().build(), options);
    }

    public ApiResponse<ScheduleListResponse> list(ScheduleListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<ScheduleListResponse> list(
        ScheduleListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<Schedule> create(ScheduleCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Schedule> create(ScheduleCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<ScheduleArchiveManyResponse> archiveMany(ScheduleArchiveManyParams params) {
      return archiveMany(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<ScheduleArchiveManyResponse> archiveMany(
        ScheduleArchiveManyParams params, RequestOptions options) {
      return Futures.await(async.withResponse().archiveMany(params, options));
    }

    public ApiResponse<Schedule> retrieve(String scheduleId) {
      return retrieve(scheduleId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Schedule> retrieve(String scheduleId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(scheduleId, options));
    }

    public ApiResponse<Schedule> update(String scheduleId, ScheduleUpdateParams params) {
      return update(scheduleId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Schedule> update(
        String scheduleId, ScheduleUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(scheduleId, params, options));
    }

    public ApiResponse<Schedule> archive(String scheduleId) {
      return archive(scheduleId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Schedule> archive(String scheduleId, RequestOptions options) {
      return Futures.await(async.withResponse().archive(scheduleId, options));
    }

    public ApiResponse<Schedule> pause(String scheduleId) {
      return pause(scheduleId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Schedule> pause(String scheduleId, RequestOptions options) {
      return Futures.await(async.withResponse().pause(scheduleId, options));
    }

    public ApiResponse<ScheduleRun> run(String scheduleId) {
      return run(scheduleId, RequestOptions.DEFAULT);
    }

    public ApiResponse<ScheduleRun> run(String scheduleId, RequestOptions options) {
      return Futures.await(async.withResponse().run(scheduleId, options));
    }

    public ApiResponse<Schedule> unpause(String scheduleId) {
      return unpause(scheduleId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Schedule> unpause(String scheduleId, RequestOptions options) {
      return Futures.await(async.withResponse().unpause(scheduleId, options));
    }
  }
}
