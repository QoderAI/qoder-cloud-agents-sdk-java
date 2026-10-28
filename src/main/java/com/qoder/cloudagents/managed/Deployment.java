package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class Deployment extends JsonModel {
  public Deployment(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static Deployment fromJson(JsonValue value) {
    return new Deployment(value, null);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public AgentReference agent() {
    return optional("agent", AgentReference::fromJson);
  }

  public String environmentId() {
    return optional("environment_id", JsonValue::asString);
  }

  public DeploymentSchedule schedule() {
    return optional("schedule", DeploymentSchedule::fromJson);
  }

  public List<SessionEventParam> initialEvents() {
    return optionalList("initial_events", SessionEventParam::fromJson);
  }

  public List<SessionResourceSpec> resources() {
    return optionalList("resources", SessionResourceSpec::fromJson);
  }

  public List<String> vaultIds() {
    return optionalList("vault_ids", JsonValue::asString);
  }

  public Map<String, String> metadata() {
    return optionalNullableMap("metadata", JsonValue::asString);
  }

  public String environmentVariables() {
    return optional("environment_variables", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public DeploymentPauseReason pausedReason() {
    return optional("paused_reason", DeploymentPauseReason::fromJson);
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
