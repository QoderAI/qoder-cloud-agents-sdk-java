package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Automatic approval decision; reasonCode may be absent. Unknown fields are preserved. */
public final class ToolApprovalDecision extends JsonObject {
  private ToolApprovalDecision(JsonValue value) {
    super(value);
  }

  public static ToolApprovalDecision fromJson(JsonValue value) {
    return new ToolApprovalDecision(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String reasonCode() {
    return optional("reason_code", JsonValue::asString);
  }
}
