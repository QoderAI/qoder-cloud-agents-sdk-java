package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.List;
import java.util.Map;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class ToolsetOverride extends JsonObject {
  private ToolsetOverride(JsonValue value) {
    super(value);
  }

  public static ToolsetOverride fromJson(JsonValue value) {
    return new ToolsetOverride(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public String mcpServerName() {
    return optional("mcp_server_name", JsonValue::asString);
  }

  public Map<String, ToolOverride> tools() {
    return optionalNullableMap("tools", ToolOverride::fromJson);
  }

  public List<ToolConfig> configs() {
    return optionalList("configs", ToolConfig::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public Builder mcpServerName(String value) {
      return put("mcp_server_name", value);
    }

    public Builder tools(Map<String, ToolOverride> value) {
      return put("tools", value);
    }

    public Builder configs(List<ToolConfig> value) {
      return put("configs", value);
    }

    public ToolsetOverride build() {

      return new ToolsetOverride(json());
    }
  }
}
