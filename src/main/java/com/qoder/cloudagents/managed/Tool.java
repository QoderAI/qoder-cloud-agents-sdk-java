package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonValue;
import java.util.List;

/** Immutable Tool configuration. Unknown fields are retained through field(). */
public final class Tool extends JsonObject {
  private Tool(JsonValue value) {
    super(value);
  }

  public static Tool fromJson(JsonValue value) {
    return new Tool(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public List<String> enabledTools() {
    return optionalList("enabled_tools", JsonValue::asString);
  }

  public List<String> disallowedTools() {
    return optionalList("disallowed_tools", JsonValue::asString);
  }

  public List<ToolConfig> configs() {
    return optionalList("configs", ToolConfig::fromJson);
  }

  public String mcpServerName() {
    return optional("mcp_server_name", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public JsonValue inputSchema() {
    return field("input_schema");
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder enabledTools(List<String> value) {
      return put("enabled_tools", value);
    }

    public Builder disallowedTools(List<String> value) {
      return put("disallowed_tools", value);
    }

    public Builder configs(List<ToolConfig> value) {
      return put("configs", value);
    }

    public Builder mcpServerName(String value) {
      return put("mcp_server_name", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public Builder inputSchema(JsonValue value) {
      return put("input_schema", value);
    }

    public Tool build() {
      require("type");
      return new Tool(json());
    }
  }
}
