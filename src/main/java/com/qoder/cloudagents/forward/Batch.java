package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class Batch extends JsonModel {
  public Batch(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String object() {
    return optional("object", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String inputFileId() {
    return optional("input_file_id", JsonValue::asString);
  }

  public String outputFileId() {
    return optional("output_file_id", JsonValue::asString);
  }

  public String errorFileId() {
    return optional("error_file_id", JsonValue::asString);
  }

  public String completionWindow() {
    return optional("completion_window", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String expiresAt() {
    return optional("expires_at", JsonValue::asString);
  }

  public String errorMessage() {
    return optional("error_message", JsonValue::asString);
  }

  public String queueReason() {
    return optional("queue_reason", JsonValue::asString);
  }

  public Boolean ignoreIdleWindow() {
    return optional("ignore_idle_window", JsonValue::asBoolean);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public BatchRequestCounts requestCounts() {
    return optional("request_counts", BatchRequestCounts::fromJson);
  }

  public BatchUsage usage() {
    return optional("usage", BatchUsage::fromJson);
  }

  public Long total() {
    return optional("total", JsonValue::asLong);
  }

  public Long pending() {
    return optional("pending", JsonValue::asLong);
  }

  public Long running() {
    return optional("running", JsonValue::asLong);
  }

  public Long completed() {
    return optional("completed", JsonValue::asLong);
  }

  public Long failed() {
    return optional("failed", JsonValue::asLong);
  }

  public Long cancelled() {
    return optional("cancelled", JsonValue::asLong);
  }

  public Long expired() {
    return optional("expired", JsonValue::asLong);
  }
}
