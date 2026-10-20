package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeConversation extends JsonModel {
  public RealtimeConversation(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String title() {
    return optional("title", JsonValue::asString);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public RealtimeConfig config() {
    return optional("config", RealtimeConfig::fromJson);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }
}
