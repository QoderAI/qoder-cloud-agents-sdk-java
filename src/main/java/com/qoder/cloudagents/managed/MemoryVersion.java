package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;

/** Immutable API response; unknown fields are preserved. */
public final class MemoryVersion extends JsonModel {
  public MemoryVersion(JsonValue value, String requestId) {
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

  public String memoryId() {
    return optional("memory_id", JsonValue::asString);
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

  public String operation() {
    return optional("operation", JsonValue::asString);
  }

  public Boolean redacted() {
    return optional("redacted", JsonValue::asBoolean);
  }

  public String redactedAt() {
    return optional("redacted_at", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }
}
