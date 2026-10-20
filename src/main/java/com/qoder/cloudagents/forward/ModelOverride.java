package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ResponseValidationException;

/** A model string or partial object patch; omitted ID inherits the stored model override. */
public final class ModelOverride implements JsonSerializable {
  private final JsonValue value;

  private ModelOverride(JsonValue value) {
    if (value == null || (!value.isString() && !value.isObject()))
      throw new ResponseValidationException("Expected model override string or object");
    this.value = value;
  }

  public static ModelOverride of(String id) {
    return fromJson(ModelConfig.of(id).toJsonValue());
  }

  public static ModelOverride fromJson(JsonValue value) {
    return new ModelOverride(value);
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean isString() {
    return value.isString();
  }

  public String id() {
    return isString() ? value.asString() : string("id");
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
    return "ModelOverride";
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

    public ModelOverride build() {
      return new ModelOverride(json());
    }
  }
}
