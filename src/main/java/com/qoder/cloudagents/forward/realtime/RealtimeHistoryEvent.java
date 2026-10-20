package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeHistoryEvent extends JsonObject {
  private RealtimeHistoryEvent(JsonValue value) {
    super(value);
  }

  public static RealtimeHistoryEvent fromJson(JsonValue value) {
    return new RealtimeHistoryEvent(value);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String occurredAt() {
    return optional("occurred_at", JsonValue::asString);
  }

  public String role() {
    return optional("role", JsonValue::asString);
  }

  public String text() {
    return optional("text", JsonValue::asString);
  }

  public String workId() {
    return optional("work_id", JsonValue::asString);
  }

  public String objective() {
    return optional("objective", JsonValue::asString);
  }

  public String result() {
    return optional("result", JsonValue::asString);
  }

  public RealtimeError error() {
    return optional("error", RealtimeError::fromJson);
  }

  public String turnId() {
    return optional("turn_id", JsonValue::asString);
  }

  public String userMessageEventId() {
    return optional("user_message_event_id", JsonValue::asString);
  }

  public String announcementId() {
    return optional("announcement_id", JsonValue::asString);
  }

  public String milestoneType() {
    return optional("milestone_type", JsonValue::asString);
  }
}
