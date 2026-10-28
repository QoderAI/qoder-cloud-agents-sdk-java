package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class Session extends JsonModel {
  public Session(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static Session fromJson(JsonValue value) {
    return new Session(value, null);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public Agent agent() {
    return optional("agent", v -> new Agent(v, null));
  }

  public String environmentId() {
    return optional("environment_id", JsonValue::asString);
  }

  public SessionBudget budget() {
    return optional("budget", SessionBudget::fromJson);
  }

  public String title() {
    return optional("title", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public Map<String, JsonValue> metadata() {
    return optionalNullableMap("metadata", v -> v);
  }

  public Map<String, String> environmentVariables() {
    return optionalNullableMap("environment_variables", JsonValue::asString);
  }

  public List<SessionResource> resources() {
    return optionalList("resources", v -> new SessionResource(v, null));
  }

  public List<String> vaultIds() {
    return optionalList("vault_ids", JsonValue::asString);
  }

  public String deploymentId() {
    return optional("deployment_id", JsonValue::asString);
  }

  public List<OutcomeEvaluation> outcomeEvaluations() {
    return optionalList("outcome_evaluations", OutcomeEvaluation::fromJson);
  }

  public SessionStats stats() {
    return optional("stats", SessionStats::fromJson);
  }

  public SessionUsage usage() {
    return optional("usage", SessionUsage::fromJson);
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
