package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.Map;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class IdentityConfigSpec extends JsonObject {
  private IdentityConfigSpec(JsonValue value) {
    super(value);
  }

  public static IdentityConfigSpec fromJson(JsonValue value) {
    return new IdentityConfigSpec(value);
  }

  public SystemOverride system() {
    return optional("system", SystemOverride::fromJson);
  }

  public ModelOverride model() {
    return optional("model", ModelOverride::fromJson);
  }

  public IdentityTools tools() {
    return optional("tools", IdentityTools::fromJson);
  }

  public Map<String, ManagedToolOverride> managedToolConfig() {
    return optionalNullableMap("managed_tool_config", ManagedToolOverride::fromJson);
  }

  public Map<String, MCPServerOverride> mcpServers() {
    return optionalNullableMap("mcp_servers", MCPServerOverride::fromJson);
  }

  public Map<String, SkillOverride> skills() {
    return optionalNullableMap("skills", SkillOverride::fromJson);
  }

  public Map<String, ToolsetOverride> toolsets() {
    return optionalNullableMap("toolsets", ToolsetOverride::fromJson);
  }

  public Map<String, JsonValue> agentMetadata() {
    return optionalMap("agent_metadata", value -> value);
  }

  public Map<String, ResourceBinding> vaults() {
    return optionalNullableMap("vaults", ResourceBinding::fromJson);
  }

  public Map<String, ResourceBinding> files() {
    return optionalNullableMap("files", ResourceBinding::fromJson);
  }

  public Map<String, GitHubRepository> githubRepositories() {
    return optionalNullableMap("github_repositories", GitHubRepository::fromJson);
  }

  public Map<String, EnvironmentVariableOverride> environmentVariables() {
    return optionalNullableMap("environment_variables", EnvironmentVariableOverride::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder system(SystemOverride value) {
      return put("system", value);
    }

    public Builder model(ModelOverride value) {
      return put("model", value);
    }

    public Builder tools(IdentityTools value) {
      return put("tools", value);
    }

    public Builder managedToolConfig(Map<String, ManagedToolOverride> value) {
      return put("managed_tool_config", value);
    }

    public Builder mcpServers(Map<String, MCPServerOverride> value) {
      return put("mcp_servers", value);
    }

    public Builder skills(Map<String, SkillOverride> value) {
      return put("skills", value);
    }

    public Builder toolsets(Map<String, ToolsetOverride> value) {
      return put("toolsets", value);
    }

    public Builder agentMetadata(Map<String, JsonValue> value) {
      return put("agent_metadata", value);
    }

    public Builder vaults(Map<String, ResourceBinding> value) {
      return put("vaults", value);
    }

    public Builder files(Map<String, ResourceBinding> value) {
      return put("files", value);
    }

    public Builder githubRepositories(Map<String, GitHubRepositoryConfig> value) {
      return put("github_repositories", value);
    }

    public Builder environmentVariables(Map<String, EnvironmentVariableOverride> value) {
      return put("environment_variables", value);
    }

    public IdentityConfigSpec build() {

      return new IdentityConfigSpec(json());
    }
  }
}
