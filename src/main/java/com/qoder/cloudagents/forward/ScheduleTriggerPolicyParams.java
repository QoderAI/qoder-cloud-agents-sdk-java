package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ScheduleTriggerPolicyParams extends JsonObject {
  private ScheduleTriggerPolicyParams(JsonValue value) {
    super(value);
  }

  public static ScheduleTriggerPolicyParams fromJson(JsonValue value) {
    return new ScheduleTriggerPolicyParams(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String expression() {
    return optional("expression", JsonValue::asString);
  }

  public String timezone() {
    return optional("timezone", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder expression(String value) {
      return put("expression", value);
    }

    public Builder timezone(String value) {
      return put("timezone", value);
    }

    public ScheduleTriggerPolicyParams build() {
      require("type");
      return new ScheduleTriggerPolicyParams(json());
    }
  }
}
