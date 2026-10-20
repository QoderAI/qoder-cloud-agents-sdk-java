package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeWork extends JsonObject {
  private RealtimeWork(JsonValue value) {
    super(value);
  }

  public static RealtimeWork fromJson(JsonValue value) {
    return new RealtimeWork(value);
  }

  public String status() {
    return optional("status", JsonValue::asString);
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

  public String eventId() {
    return optional("event_id", JsonValue::asString);
  }

  public String kind() {
    return optional("kind", JsonValue::asString);
  }

  public String detail() {
    return optional("detail", JsonValue::asString);
  }

  public String title() {
    return optional("title", JsonValue::asString);
  }

  public Boolean isError() {
    return optional("is_error", JsonValue::asBoolean);
  }

  public String announcementId() {
    return optional("announcement_id", JsonValue::asString);
  }

  public String milestoneType() {
    return optional("milestone_type", JsonValue::asString);
  }

  public String summary() {
    return optional("summary", JsonValue::asString);
  }
}
