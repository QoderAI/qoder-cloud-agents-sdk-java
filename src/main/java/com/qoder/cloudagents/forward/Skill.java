package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class Skill extends JsonModel {
  public Skill(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String displayTitle() {
    return optional("display_title", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public String source() {
    return optional("source", JsonValue::asString);
  }

  public String latestVersion() {
    return optional("latest_version", JsonValue::asString);
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

  public String content() {
    return optional("content", JsonValue::asString);
  }

  public String contentEncoding() {
    return optional("content_encoding", JsonValue::asString);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public ResourceBindingInfo bindingInfo() {
    return optional("binding_info", ResourceBindingInfo::fromJson);
  }
}
