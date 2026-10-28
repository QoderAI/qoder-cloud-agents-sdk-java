package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class WorkItem extends JsonModel {
  public WorkItem(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static WorkItem fromJson(JsonValue value) {
    return new WorkItem(value, null);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String environmentId() {
    return optional("environment_id", JsonValue::asString);
  }

  public String state() {
    return optional("state", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String acknowledgedAt() {
    return optional("acknowledged_at", JsonValue::asString);
  }

  public String startedAt() {
    return optional("started_at", JsonValue::asString);
  }

  public String latestHeartbeatAt() {
    return optional("latest_heartbeat_at", JsonValue::asString);
  }

  public String stopRequestedAt() {
    return optional("stop_requested_at", JsonValue::asString);
  }

  public String stoppedAt() {
    return optional("stopped_at", JsonValue::asString);
  }

  public WorkData data() {
    return optional("data", WorkData::fromJson);
  }

  public Map<String, String> metadata() {
    return optionalNullableMap("metadata", JsonValue::asString);
  }
}
