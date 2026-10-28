package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.Map;

/** Immutable API response; unknown fields remain accessible through field(). */
public final class Identity extends JsonModel {
  public Identity(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public Boolean autoMemoryStore() {
    return optional("auto_memory_store", JsonValue::asBoolean);
  }

  public String id() {
    return field("id").asString();
  }

  public String externalId() {
    return optional("external_id", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String identityType() {
    return optional("identity_type", JsonValue::asString);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", value -> value);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }
}
