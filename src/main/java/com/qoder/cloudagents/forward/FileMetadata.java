package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class FileMetadata extends JsonModel {
  public FileMetadata(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return field("id").asString();
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

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String iconUrl() {
    return optional("icon_url", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }

  public Long sizeBytes() {
    return optional("size_bytes", JsonValue::asLong);
  }

  public Boolean downloadable() {
    return optional("downloadable", JsonValue::asBoolean);
  }

  public FileScope scope() {
    return optional("scope", FileScope::fromJson);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public ResourceBindingInfo bindingInfo() {
    return optional("binding_info", ResourceBindingInfo::fromJson);
  }
}
