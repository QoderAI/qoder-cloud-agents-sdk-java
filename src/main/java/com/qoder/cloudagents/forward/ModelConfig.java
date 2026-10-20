package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import com.qoder.cloudagents.errors.ResponseValidationException;

/** A model ID string or a structured model configuration; wire shape is preserved. */
public final class ModelConfig implements JsonSerializable {
  private final JsonValue value;

  private ModelConfig(JsonValue value) {
    if (value == null || (!value.isString() && !value.isObject()))
      throw new ResponseValidationException("Expected a model string or object");
    this.value = value;
  }

  public static ModelConfig of(String id) {
    if (id == null || id.isEmpty()) throw new ConfigurationException("Model ID must not be empty");
    return new ModelConfig(JsonValue.of(id));
  }

  public static ModelConfig fromJson(JsonValue value) {
    return new ModelConfig(value);
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean isString() {
    return value.isString();
  }

  public String id() {
    return isString() ? value.asString() : value.get("id").asString();
  }

  public String effort() {
    return string("effort");
  }

  public String speed() {
    return string("speed");
  }

  public Long contextWindow() {
    JsonValue field = field("context_window");
    return field.isMissing() || field.isNull() ? null : field.asLong();
  }

  public JsonValue field(String name) {
    return value.get(name);
  }

  private String string(String name) {
    JsonValue field = field(name);
    return field.isMissing() || field.isNull() ? null : field.asString();
  }

  @Override
  public JsonValue toJsonValue() {
    return value;
  }

  @Override
  public String toString() {
    return "ModelConfig";
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder id(String value) {
      return put("id", value);
    }

    public Builder effort(String value) {
      return put("effort", value);
    }

    public Builder speed(String value) {
      return put("speed", value);
    }

    public Builder contextWindow(Long value) {
      return put("context_window", value);
    }

    public ModelConfig build() {
      require("id");
      return new ModelConfig(json());
    }
  }
}
