package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class BatchTaskError extends JsonObject {
  private BatchTaskError(JsonValue value) {
    super(value);
  }

  public static BatchTaskError fromJson(JsonValue value) {
    return new BatchTaskError(value);
  }

  public String code() {
    return optional("code", JsonValue::asString);
  }

  public String message() {
    return optional("message", JsonValue::asString);
  }
}
