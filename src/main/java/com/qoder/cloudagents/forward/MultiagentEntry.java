package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonSerializable;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.errors.ConfigurationException;
import com.qoder.cloudagents.errors.ResponseValidationException;

/** A Template/Agent reference string or roster object; Advisor model IDs remain strings. */
public final class MultiagentEntry implements JsonSerializable {
  private final JsonValue value;

  private MultiagentEntry(JsonValue value) {
    if (value == null || (!value.isString() && !value.isObject()))
      throw new ResponseValidationException("Expected a multiagent reference string or object");
    this.value = value;
  }

  public static MultiagentEntry of(String templateId) {
    if (templateId == null || templateId.isEmpty())
      throw new ConfigurationException("Template ID must not be empty");
    return new MultiagentEntry(JsonValue.of(templateId));
  }

  public static MultiagentEntry fromJson(JsonValue value) {
    return new MultiagentEntry(value);
  }

  public boolean isString() {
    return value.isString();
  }

  public String type() {
    return string("type");
  }

  public String templateId() {
    return isString() ? value.asString() : string("template_id");
  }

  public String id() {
    return string("id");
  }

  public String name() {
    return string("name");
  }

  public String model() {
    return string("model");
  }

  public JsonValue field(String name) {
    return value.get(name);
  }

  public String rawJson() {
    return value.toJson();
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
    return "MultiagentEntry";
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public Builder id(String value) {
      return put("id", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder model(String value) {
      return put("model", value);
    }

    public MultiagentEntry build() {
      require("type");
      return new MultiagentEntry(json());
    }
  }
}
