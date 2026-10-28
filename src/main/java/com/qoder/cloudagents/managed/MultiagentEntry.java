package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;

/** String Agent shorthand or an Agent, self, or Advisor roster entry. */
public final class MultiagentEntry implements JsonSerializable {
  private final JsonValue value;

  private MultiagentEntry(JsonValue value) {
    if (value == null || (!value.isString() && !value.isObject()))
      throw new ResponseValidationException("Expected an Agent ID or roster object");
    this.value = value;
  }

  public static MultiagentEntry of(String id) {
    if (id == null || id.isEmpty()) throw new ConfigurationException("Agent ID must not be empty");
    return new MultiagentEntry(JsonValue.of(id));
  }

  public static MultiagentEntry fromJson(JsonValue value) {
    return new MultiagentEntry(value);
  }

  public boolean isString() {
    return value.isString();
  }

  private String string(String name) {
    JsonValue v = value.get(name);
    return v.isMissing() || v.isNull() ? null : v.asString();
  }

  public String type() {
    return string("type");
  }

  public String id() {
    return isString() ? value.asString() : string("id");
  }

  public JsonValue version() {
    return value.get("version");
  }

  public ModelConfig model() {
    JsonValue v = value.get("model");
    return v.isMissing() || v.isNull() ? null : ModelConfig.fromJson(v);
  }

  public Agent agent() {
    return isString() ? null : new Agent(value, null);
  }

  public JsonValue field(String name) {
    return value.get(name);
  }

  public JsonValue toJsonValue() {
    return value;
  }

  public String toString() {
    return "MultiagentEntry";
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    public Builder type(String value) {
      return put("type", value);
    }

    public Builder id(String value) {
      return put("id", value);
    }

    public Builder version(long value) {
      if (value <= 0) throw new ConfigurationException("version must be positive");
      return put("version", value);
    }

    public Builder version(String value) {
      if (value == null || (!"latest".equals(value) && !value.matches("[1-9][0-9]*")))
        throw new ConfigurationException("version must be latest or a positive integer string");
      return put("version", value);
    }

    public Builder model(String value) {
      return put("model", value);
    }

    public MultiagentEntry build() {
      require("type");
      String t = json().get("type").asString();
      if ("agent".equals(t)) require("id");
      if ("advisor".equals(t)) require("model");
      return new MultiagentEntry(json());
    }
  }
}
