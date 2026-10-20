package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeError extends JsonObject {
  private RealtimeError(JsonValue value) {
    super(value);
  }

  public static RealtimeError fromJson(JsonValue value) {
    return new RealtimeError(value);
  }

  public String code() {
    return optional("code", JsonValue::asString);
  }

  public String message() {
    return optional("message", JsonValue::asString);
  }

  public Boolean retryable() {
    return optional("retryable", JsonValue::asBoolean);
  }

  public Long retryAfterMs() {
    return optional("retry_after_ms", JsonValue::asLong);
  }
}
