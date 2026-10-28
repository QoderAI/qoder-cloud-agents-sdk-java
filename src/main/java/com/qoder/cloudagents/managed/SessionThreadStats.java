package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.math.BigDecimal;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionThreadStats extends JsonObject {
  private SessionThreadStats(JsonValue value) {
    super(value);
  }

  public static SessionThreadStats fromJson(JsonValue value) {
    return new SessionThreadStats(value);
  }

  public BigDecimal activeSeconds() {
    return optional("active_seconds", JsonValue::asDecimal);
  }

  public BigDecimal durationSeconds() {
    return optional("duration_seconds", JsonValue::asDecimal);
  }

  public BigDecimal startupSeconds() {
    return optional("startup_seconds", JsonValue::asDecimal);
  }
}
