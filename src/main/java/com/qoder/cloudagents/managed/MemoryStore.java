package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class MemoryStore extends JsonModel {
  public MemoryStore(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static MemoryStore fromJson(JsonValue value) {
    return new MemoryStore(value, null);
  }

  public Long entryCount() {
    return optional("entry_count", JsonValue::asLong);
  }

  public Long totalSize() {
    return optional("total_size", JsonValue::asLong);
  }

  public String status() {
    return optional("status", JsonValue::asString);
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

  public String description() {
    return optional("description", JsonValue::asString);
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

  public Map<String, String> metadata() {
    return optionalNullableMap("metadata", JsonValue::asString);
  }
}
