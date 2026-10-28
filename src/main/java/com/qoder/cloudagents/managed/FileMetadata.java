package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class FileMetadata extends JsonModel {
  public FileMetadata(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static FileMetadata fromJson(JsonValue value) {
    return new FileMetadata(value, null);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String filename() {
    return optional("filename", JsonValue::asString);
  }

  public String mimeType() {
    return optional("mime_type", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public FileScope scope() {
    return optional("scope", FileScope::fromJson);
  }

  public Long sizeBytes() {
    return optional("size_bytes", JsonValue::asLong);
  }

  public Boolean downloadable() {
    return optional("downloadable", JsonValue::asBoolean);
  }

  public Map<String, JsonValue> metadata() {
    return optionalNullableMap("metadata", v -> v);
  }
}
