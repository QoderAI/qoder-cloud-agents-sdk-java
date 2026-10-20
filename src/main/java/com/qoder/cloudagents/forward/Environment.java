package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class Environment extends JsonModel {
  public Environment(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return field("id").asString();
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public EnvironmentConfig config() {
    return optional("config", EnvironmentConfig::fromJson);
  }

  public Map<String, String> metadata() {
    return optionalMap("metadata", JsonValue::asString);
  }

  public String archivedAt() {
    return optional("archived_at", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String iconUrl() {
    return optional("icon_url", JsonValue::asString);
  }

  public ResourceBindingInfo bindingInfo() {
    return optional("binding_info", ResourceBindingInfo::fromJson);
  }
}
