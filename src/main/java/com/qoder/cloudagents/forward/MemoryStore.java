package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.Map;

/** Immutable API response; unknown fields are preserved. */
public final class MemoryStore extends JsonModel {
  public MemoryStore(JsonValue value, String requestId) {
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

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public Long entryCount() {
    return optional("entry_count", JsonValue::asLong);
  }

  public Long totalSize() {
    return optional("total_size", JsonValue::asLong);
  }

  public Map<String, String> metadata() {
    return optionalMap("metadata", JsonValue::asString);
  }

  public Boolean systemManaged() {
    return optional("system_managed", JsonValue::asBoolean);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }

  public String archivedAt() {
    return optional("archived_at", JsonValue::asString);
  }

  public MemoryStoreBindingInfo bindingInfo() {
    return optional("binding_info", MemoryStoreBindingInfo::fromJson);
  }
}
