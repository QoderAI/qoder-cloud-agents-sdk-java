package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import java.util.List;

/**
 * Runtime configuration patch for an existing Session. Beta fields require the x-qoder-beta request
 * header.
 */
public final class SessionAgentPatch extends JsonObject {
  private SessionAgentPatch(JsonValue value) {
    super(value);
  }

  public static SessionAgentPatch fromJson(JsonValue value) {
    return new SessionAgentPatch(value);
  }

  public ModelConfig model() {
    return optional("model", ModelConfig::fromJson);
  }

  public String system() {
    return optional("system", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
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

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    public Builder model(ModelConfig value) {
      return put("model", value);
    }

    public Builder model(String value) {
      return put("model", value == null ? null : ModelConfig.of(value));
    }

    public Builder system(String value) {
      return put("system", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder description(String value) {
      return put("description", value);
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

    public SessionAgentPatch build() {
      if (json().asObject().isEmpty())
        throw new ConfigurationException("An agent patch must contain a runtime field");
      for (String field : new String[] {"id", "version", "type", "multiagent", "metadata"})
        if (!json().get(field).isMissing())
          throw new ConfigurationException("Session runtime patches cannot contain " + field);
      return new SessionAgentPatch(json());
    }
  }
}
