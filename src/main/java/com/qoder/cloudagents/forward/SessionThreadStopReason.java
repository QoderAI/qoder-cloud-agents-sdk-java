package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionThreadStopReason extends JsonObject {
  private SessionThreadStopReason(JsonValue value) {
    super(value);
  }

  public static SessionThreadStopReason fromJson(JsonValue value) {
    return new SessionThreadStopReason(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }
}
