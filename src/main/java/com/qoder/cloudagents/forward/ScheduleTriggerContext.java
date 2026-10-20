package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class ScheduleTriggerContext extends JsonObject {
  private ScheduleTriggerContext(JsonValue value) {
    super(value);
  }

  public static ScheduleTriggerContext fromJson(JsonValue value) {
    return new ScheduleTriggerContext(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String scheduledAt() {
    return optional("scheduled_at", JsonValue::asString);
  }
}
