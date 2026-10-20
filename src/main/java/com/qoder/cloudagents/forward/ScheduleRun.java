package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class ScheduleRun extends JsonModel {
  public ScheduleRun(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String source() {
    return optional("source", JsonValue::asString);
  }

  public String sourceSessionId() {
    return optional("source_session_id", JsonValue::asString);
  }

  public String scheduleId() {
    return optional("schedule_id", JsonValue::asString);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String sessionId() {
    return optional("session_id", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String resultPayload() {
    return optional("result_payload", JsonValue::asString);
  }

  public String errorMessage() {
    return optional("error_message", JsonValue::asString);
  }

  public String pushSink() {
    return optional("push_sink", JsonValue::asString);
  }

  public String pushStatus() {
    return optional("push_status", JsonValue::asString);
  }

  public String pushFinishedAt() {
    return optional("push_finished_at", JsonValue::asString);
  }

  public String triggeredAt() {
    return optional("triggered_at", JsonValue::asString);
  }

  public String startedAt() {
    return optional("started_at", JsonValue::asString);
  }

  public String completedAt() {
    return optional("completed_at", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public Long attempt() {
    return optional("attempt", JsonValue::asLong);
  }

  public Long durationMs() {
    return optional("duration_ms", JsonValue::asLong);
  }

  public ScheduleTriggerContext triggerContext() {
    return optional("trigger_context", ScheduleTriggerContext::fromJson);
  }

  public ScheduleRunError error() {
    return optional("error", ScheduleRunError::fromJson);
  }
}
