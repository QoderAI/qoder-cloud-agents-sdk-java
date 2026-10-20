package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonModel;
import com.qoder.cloudagents.core.JsonValue;
import java.util.List;
import java.util.Map;

/** Template details or list summary. Configuration absent from a summary returns null. */
public final class Template extends JsonModel {
  public Template(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return field("id").asString();
  }

  public String type() {
    return field("type").asString();
  }

  public String name() {
    return field("name").asString();
  }

  public String description() {
    return field("description").stringOrNull();
  }

  public String status() {
    return field("status").asString();
  }

  public String createdAt() {
    return field("created_at").stringOrNull();
  }

  public String updatedAt() {
    return field("updated_at").stringOrNull();
  }

  public String environmentId() {
    return field("environment_id").stringOrNull();
  }

  public ModelConfig model() {
    return optional("model", ModelConfig::fromJson);
  }

  public ManagedToolConfig managedToolConfig() {
    return optional("managed_tool_config", ManagedToolConfig::fromJson);
  }

  public String system() {
    return optionalString("system");
  }

  public Long maxToolRounds() {
    return optionalLong("max_tool_rounds");
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

  public MultiagentConfig multiagent() {
    return optional("multiagent", MultiagentConfig::fromJson);
  }

  public Map<String, ResourceBinding> vaults() {
    return optionalMap("vaults", ResourceBinding::fromJson);
  }

  public Map<String, ResourceBinding> files() {
    return optionalMap("files", ResourceBinding::fromJson);
  }

  public Map<String, GitHubRepository> githubRepositories() {
    return optionalMap("github_repositories", GitHubRepository::fromJson);
  }

  public EnvironmentVariables environmentVariables() {
    return optional("environment_variables", EnvironmentVariables::fromJson);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", value -> value);
  }
}
