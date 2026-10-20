package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class Session extends JsonModel {
  public Session(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public SessionTemplate template() {
    return optional("template", SessionTemplate::fromJson);
  }

  public String sourceType() {
    return optional("source_type", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String title() {
    return optional("title", JsonValue::asString);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", item -> item);
  }

  public SessionConfig config() {
    return optional("config", SessionConfig::fromJson);
  }

  public List<SessionResource> resources() {
    return optionalList("resources", item -> new SessionResource(item, requestId()));
  }

  public SessionStats stats() {
    return optional("stats", SessionStats::fromJson);
  }

  public SessionUsage usage() {
    return optional("usage", SessionUsage::fromJson);
  }

  public List<OutcomeEvaluation> outcomeEvaluations() {
    return optionalList("outcome_evaluations", OutcomeEvaluation::fromJson);
  }

  public String archivedAt() {
    return optional("archived_at", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }
}
