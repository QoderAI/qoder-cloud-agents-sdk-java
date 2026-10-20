package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable API response; unknown fields remain accessible through field(). */
public final class EffectiveConfig extends JsonModel {
  public EffectiveConfig(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String agentEffectiveHash() {
    return optional("agent_effective_hash", JsonValue::asString);
  }

  public String sessionEffectiveHash() {
    return optional("session_effective_hash", JsonValue::asString);
  }

  public String effectiveHash() {
    return optional("effective_hash", JsonValue::asString);
  }

  public EffectiveConfigAgent agent() {
    return optional("agent", EffectiveConfigAgent::fromJson);
  }

  public EffectiveConfigSession session() {
    return optional("session", EffectiveConfigSession::fromJson);
  }
}
