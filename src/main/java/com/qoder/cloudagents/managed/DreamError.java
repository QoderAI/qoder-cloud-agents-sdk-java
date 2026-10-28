package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DreamError extends JsonObject {
  private DreamError(JsonValue value) {
    super(value);
  }

  public static DreamError fromJson(JsonValue value) {
    return new DreamError(value);
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
