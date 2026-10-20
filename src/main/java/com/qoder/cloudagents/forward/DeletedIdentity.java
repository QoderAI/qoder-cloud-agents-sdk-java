package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable API response; unknown fields remain accessible through field(). */
public final class DeletedIdentity extends JsonModel {
  public DeletedIdentity(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return field("id").asString();
  }

  public Boolean deleted() {
    return optional("deleted", JsonValue::asBoolean);
  }
}
