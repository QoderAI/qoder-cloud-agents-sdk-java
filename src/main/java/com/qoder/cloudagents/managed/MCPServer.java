package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonValue;

/** Immutable MCPServer configuration. Unknown fields are retained through field(). */
public final class MCPServer extends JsonObject {
  private MCPServer(JsonValue value) {
    super(value);
  }

  public static MCPServer fromJson(JsonValue value) {
    return new MCPServer(value);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String url() {
    return optional("url", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder url(String value) {
      return put("url", value);
    }

    public Builder type(String value) {
      return put("type", value);
    }

    public MCPServer build() {
      require("name", "url");
      return new MCPServer(json());
    }
  }
}
