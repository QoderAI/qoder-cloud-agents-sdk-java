package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class Schedule extends JsonModel {
  public Schedule(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public List<SessionResourceSpec> resources() {
    return optionalList("resources", SessionResourceSpec::fromJson);
  }

  public Long initialEventsVersion() {
    return optional("initial_events_version", JsonValue::asLong);
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

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String environmentId() {
    return optional("environment_id", JsonValue::asString);
  }

  public String archivedAt() {
    return optional("archived_at", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }

  public List<ScheduleInitialEvent> initialEvents() {
    return optionalList("initial_events", ScheduleInitialEvent::fromJson);
  }

  public ScheduleExecution execution() {
    return optional("execution", ScheduleExecution::fromJson);
  }

  public ScheduleTriggerPolicy triggerPolicy() {
    return optional("trigger_policy", ScheduleTriggerPolicy::fromJson);
  }

  public List<ScheduleSink> sinks() {
    return optionalList("sinks", ScheduleSink::fromJson);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public JsonValue pausedReason() {
    return field("paused_reason");
  }
}
