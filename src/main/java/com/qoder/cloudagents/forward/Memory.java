package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.Map;

/** Immutable API response; unknown fields are preserved. */
public final class Memory extends JsonModel {
  public Memory(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return field("id").asString();
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String memoryStoreId() {
    return optional("memory_store_id", JsonValue::asString);
  }

  public String path() {
    return optional("path", JsonValue::asString);
  }

  public String content() {
    return optional("content", JsonValue::asString);
  }

  public Long contentSizeBytes() {
    return optional("content_size_bytes", JsonValue::asLong);
  }

  public String contentSha256() {
    return optional("content_sha256", JsonValue::asString);
  }

  public Map<String, String> metadata() {
    return optionalMap("metadata", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }
}
