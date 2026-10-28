package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;

/** An Agent ID or an explicit version reference; preserves the wire representation. */
public final class AgentReference implements JsonSerializable {
  private final JsonValue value;

  private AgentReference(JsonValue value) {
    if (value == null || (!value.isString() && !value.isObject()))
      throw new ResponseValidationException("Expected an Agent ID or object");
    this.value = value;
  }

  public static AgentReference of(String id) {
    if (id == null || id.isEmpty()) throw new ConfigurationException("Agent ID must not be empty");
    return new AgentReference(JsonValue.of(id));
  }

  public static AgentReference fromJson(JsonValue value) {
    return new AgentReference(value);
  }

  public boolean isString() {
    return value.isString();
  }

  public String id() {
    return isString() ? value.asString() : value.get("id").asString();
  }

  public String type() {
    JsonValue v = value.get("type");
    return v.isMissing() || v.isNull() ? null : v.asString();
  }

  public Long version() {
    JsonValue v = value.get("version");
    return v.isMissing() || v.isNull() ? null : v.asLong();
  }

  public JsonValue field(String name) {
    return value.get(name);
  }

  public JsonValue toJsonValue() {
    return value;
  }

  public String toString() {
    return "AgentReference";
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    public Builder id(String value) {
      return put("id", value);
    }

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder version(long value) {
      if (value < 0) throw new ConfigurationException("version must not be negative");
      return put("version", value);
    }

    public AgentReference build() {
      require("id", "type");
      if (!"agent".equals(json().get("type").asString()))
        throw new ConfigurationException("Reference type must be agent");
      return new AgentReference(json());
    }
  }
}
