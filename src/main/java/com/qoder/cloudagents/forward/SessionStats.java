package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.math.BigDecimal;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionStats extends JsonObject {
  private SessionStats(JsonValue value) {
    super(value);
  }

  public static SessionStats fromJson(JsonValue value) {
    return new SessionStats(value);
  }

  public BigDecimal activeSeconds() {
    return optional("active_seconds", JsonValue::asDecimal);
  }

  public BigDecimal durationSeconds() {
    return optional("duration_seconds", JsonValue::asDecimal);
  }
}
