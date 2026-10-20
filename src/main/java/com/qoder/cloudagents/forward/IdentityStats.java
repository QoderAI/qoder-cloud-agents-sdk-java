package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable API response; unknown fields remain accessible through field(). */
public final class IdentityStats extends JsonModel {
  public IdentityStats(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public Long totalIdentities() {
    return optional("total_identities", JsonValue::asLong);
  }

  public Long activeIdentities() {
    return optional("active_identities", JsonValue::asLong);
  }

  public Long totalTemplates() {
    return optional("total_agents", JsonValue::asLong);
  }

  public Long totalSessions() {
    return optional("total_sessions", JsonValue::asLong);
  }
}
