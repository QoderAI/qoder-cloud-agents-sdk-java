package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class Dream extends JsonModel {
  public Dream(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static Dream fromJson(JsonValue value) {
    return new Dream(value, null);
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

  public String instructions() {
    return optional("instructions", JsonValue::asString);
  }

  public String sessionId() {
    return optional("session_id", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String endedAt() {
    return optional("ended_at", JsonValue::asString);
  }

  public String archivedAt() {
    return optional("archived_at", JsonValue::asString);
  }

  public List<DreamInput> inputs() {
    return optionalList("inputs", DreamInput::fromJson);
  }

  public List<DreamOutput> outputs() {
    return optionalList("outputs", DreamOutput::fromJson);
  }

  public DreamModelConfig model() {
    return optional("model", DreamModelConfig::fromJson);
  }

  public DreamUsage usage() {
    return optional("usage", DreamUsage::fromJson);
  }

  public DreamError error() {
    return optional("error", DreamError::fromJson);
  }
}
