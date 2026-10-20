package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ResourceBindingInfo extends JsonObject {
  private ResourceBindingInfo(JsonValue value) {
    super(value);
  }

  public static ResourceBindingInfo fromJson(JsonValue value) {
    return new ResourceBindingInfo(value);
  }

  public Long agentTemplateCount() {
    return optional("agent_template_count", JsonValue::asLong);
  }
}
