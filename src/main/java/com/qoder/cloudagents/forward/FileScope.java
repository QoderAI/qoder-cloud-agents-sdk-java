package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class FileScope extends JsonObject {
  private FileScope(JsonValue value) {
    super(value);
  }

  public static FileScope fromJson(JsonValue value) {
    return new FileScope(value);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }
}
