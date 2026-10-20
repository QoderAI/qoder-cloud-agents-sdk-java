package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class MCPServerOverride extends JsonObject {
  private MCPServerOverride(JsonValue value) {
    super(value);
  }

  public static MCPServerOverride fromJson(JsonValue value) {
    return new MCPServerOverride(value);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String url() {
    return optional("url", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder url(String value) {
      return put("url", value);
    }

    public MCPServerOverride build() {

      return new MCPServerOverride(json());
    }
  }
}
