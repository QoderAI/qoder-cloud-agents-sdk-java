package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.Map;

/** Immutable API response; unknown fields remain accessible through field(). */
public final class IdentityConfig extends JsonModel {
  public IdentityConfig(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String id() {
    return field("id").asString();
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String effectiveHash() {
    return optional("effective_hash", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }

  public IdentityConfigSpec identityConfig() {
    return optional("identity_config", IdentityConfigSpec::fromJson);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", value -> value);
  }
}
