package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class IdentityTemplate extends JsonObject {
  private IdentityTemplate(JsonValue value) {
    super(value);
  }

  public static IdentityTemplate fromJson(JsonValue value) {
    return new IdentityTemplate(value);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String templateName() {
    return optional("template_name", JsonValue::asString);
  }

  public Long sessionCount() {
    return optional("session_count", JsonValue::asLong);
  }

  public String lastActiveAt() {
    return optional("last_active_at", JsonValue::asString);
  }
}
