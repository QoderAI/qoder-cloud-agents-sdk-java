package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class ScheduleRunError extends JsonObject {
  private ScheduleRunError(JsonValue value) {
    super(value);
  }

  public static ScheduleRunError fromJson(JsonValue value) {
    return new ScheduleRunError(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String code() {
    return optional("code", JsonValue::asString);
  }

  public String message() {
    return optional("message", JsonValue::asString);
  }
}
