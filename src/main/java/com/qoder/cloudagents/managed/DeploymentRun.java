package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class DeploymentRun extends JsonModel {
  public DeploymentRun(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static DeploymentRun fromJson(JsonValue value) {
    return new DeploymentRun(value, null);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String deploymentId() {
    return optional("deployment_id", JsonValue::asString);
  }

  public AgentReference agent() {
    return optional("agent", AgentReference::fromJson);
  }

  public DeploymentRunError error() {
    return optional("error", DeploymentRunError::fromJson);
  }

  public DeploymentTriggerContext triggerContext() {
    return optional("trigger_context", DeploymentTriggerContext::fromJson);
  }

  public String sessionId() {
    return optional("session_id", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }
}
