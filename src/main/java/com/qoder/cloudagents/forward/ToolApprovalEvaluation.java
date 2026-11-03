package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Optional policy evaluation details on tool-use events. Unknown fields are preserved. */
public final class ToolApprovalEvaluation extends JsonObject {
  private ToolApprovalEvaluation(JsonValue value) {
    super(value);
  }

  public static ToolApprovalEvaluation fromJson(JsonValue value) {
    return new ToolApprovalEvaluation(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public ToolApprovalDecision evaluatedPermission() {
    return optional("evaluated_permission", ToolApprovalDecision::fromJson);
  }
}
