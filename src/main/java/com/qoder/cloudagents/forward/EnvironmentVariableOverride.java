package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class EnvironmentVariableOverride extends JsonObject {
  private EnvironmentVariableOverride(JsonValue value) {
    super(value);
  }

  public static EnvironmentVariableOverride fromJson(JsonValue value) {
    return new EnvironmentVariableOverride(value);
  }

  public String op() {
    return optional("op", JsonValue::asString);
  }

  public String value() {
    return optional("value", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder op(String value) {
      return put("op", value);
    }

    public Builder value(String value) {
      return put("value", value);
    }

    public EnvironmentVariableOverride build() {
      require("op");
      return new EnvironmentVariableOverride(json());
    }
  }
}
