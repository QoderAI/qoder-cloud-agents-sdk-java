package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable API response; unknown fields remain accessible through field(). */
public final class IdentityClearResponse extends JsonModel {
  public IdentityClearResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String completedAt() {
    return optional("completed_at", JsonValue::asString);
  }

  public IdentityClearSummary summary() {
    return optional("summary", IdentityClearSummary::fromJson);
  }
}
