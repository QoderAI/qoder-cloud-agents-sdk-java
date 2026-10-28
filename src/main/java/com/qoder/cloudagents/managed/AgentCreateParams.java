package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class AgentCreateParams extends JsonObject {
  private AgentCreateParams(JsonValue value) {
    super(value);
  }

  public static AgentCreateParams fromJson(JsonValue value) {
    return new AgentCreateParams(value);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public ModelConfig model() {
    return optional("model", ModelConfig::fromJson);
  }

  public String system() {
    return optional("system", JsonValue::asString);
  }

  public List<Tool> tools() {
    return optionalList("tools", Tool::fromJson);
  }

  public List<MCPServer> mcpServers() {
    return optionalList("mcp_servers", MCPServer::fromJson);
  }

  public List<SkillBinding> skills() {
    return optionalList("skills", SkillBinding::fromJson);
  }

  public Map<String, String> metadata() {
    return optionalNullableMap("metadata", JsonValue::asString);
  }

  public MultiagentConfig multiagent() {
    return optional("multiagent", MultiagentConfig::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public Builder model(ModelConfig value) {
      return put("model", value);
    }

    public Builder model(String value) {
      return put("model", value == null ? null : ModelConfig.of(value));
    }

    public Builder system(String value) {
      return put("system", value);
    }

    public Builder tools(List<Tool> value) {
      return put("tools", value);
    }

    public Builder mcpServers(List<MCPServer> value) {
      return put("mcp_servers", value);
    }

    public Builder skills(List<SkillBinding> value) {
      return put("skills", value);
    }

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public Builder multiagent(MultiagentConfig value) {
      return put("multiagent", value);
    }

    public AgentCreateParams build() {
      require("name", "model");

      return new AgentCreateParams(json());
    }
  }
}
