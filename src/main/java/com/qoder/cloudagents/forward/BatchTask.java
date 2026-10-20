package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class BatchTask extends JsonModel {
  public BatchTask(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String customId() {
    return optional("custom_id", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String startedAt() {
    return optional("started_at", JsonValue::asString);
  }

  public String completedAt() {
    return optional("completed_at", JsonValue::asString);
  }

  public String outputSummary() {
    return optional("output_summary", JsonValue::asString);
  }

  public BatchUsage usage() {
    return optional("usage", BatchUsage::fromJson);
  }

  public List<BatchTaskArtifact> artifacts() {
    return optionalList("artifacts", BatchTaskArtifact::fromJson);
  }

  public BatchTaskError error() {
    return optional("error", BatchTaskError::fromJson);
  }
}
