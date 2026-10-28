package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;

/** Immutable API response; unknown fields are preserved. */
public final class DeletedMemory extends JsonModel {
  public DeletedMemory(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return field("id").asString();
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public Boolean deleted() {
    return optional("deleted", JsonValue::asBoolean);
  }
}
