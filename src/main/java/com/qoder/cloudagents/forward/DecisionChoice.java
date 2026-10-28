package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DecisionChoice extends JsonObject {
  private DecisionChoice(JsonValue value) {
    super(value);
  }

  public static DecisionChoice fromJson(JsonValue value) {
    return new DecisionChoice(value);
  }

  public JsonValue value() {
    return field("value");
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder value(JsonValue value) {
      return put("value", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public Builder value(String value) {
      return put("value", value);
    }

    public Builder value(boolean value) {
      return put("value", value);
    }

    public DecisionChoice build() {
      require("value");
      JsonValue value = json().get("value");
      if (!value.isString() && !value.isBoolean())
        throw new ConfigurationException("Choice values must be strings or booleans");
      if (value.isString() && value.asString().isEmpty())
        throw new ConfigurationException("Choice values must not be empty");
      return new DecisionChoice(json());
    }
  }
}
