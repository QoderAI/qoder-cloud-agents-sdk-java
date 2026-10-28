package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class SkillVersion extends JsonModel {
  public SkillVersion(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String skillId() {
    return optional("skill_id", JsonValue::asString);
  }

  public String version() {
    return optional("version", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public String directory() {
    return optional("directory", JsonValue::asString);
  }

  public String contentSha256() {
    return optional("content_sha256", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public Long contentSize() {
    return optional("content_size", JsonValue::asLong);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }
}
