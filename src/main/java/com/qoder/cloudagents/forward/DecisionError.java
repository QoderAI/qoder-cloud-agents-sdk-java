package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DecisionError extends JsonObject {
  private DecisionError(JsonValue value) {
    super(value);
  }

  public static DecisionError fromJson(JsonValue value) {
    return new DecisionError(value);
  }

  public String code() {
    return optional("code", JsonValue::asString);
  }

  public String message() {
    return optional("message", JsonValue::asString);
  }
}
