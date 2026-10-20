package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionTemplate extends JsonObject {
  private SessionTemplate(JsonValue value) {
    super(value);
  }

  public static SessionTemplate fromJson(JsonValue value) {
    return new SessionTemplate(value);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public ModelConfig model() {
    return optional("model", ModelConfig::fromJson);
  }

  public Long version() {
    return optional("version", JsonValue::asLong);
  }
}
