package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonValue;
import java.util.List;
import java.util.Map;

/**
 * Shared create/update fields. An unset field is omitted; a null setter writes JSON null.
 * Server-side validation decides which fields accept null. Use omit() to remove a set field.
 * Request headers belong in RequestOptions.
 */
public abstract class TemplateConfigBuilder<B extends TemplateConfigBuilder<B>>
    extends JsonObjectBuilder<B> {
  protected TemplateConfigBuilder() {}

  public B name(String value) {
    return put("name", value);
  }

  public B description(String value) {
    return put("description", value);
  }

  public B model(ModelConfig value) {
    return put("model", value);
  }

  public B modelId(String value) {
    return put("model", value);
  }

  public B system(String value) {
    return put("system", value);
  }

  public B maxToolRounds(Long value) {
    return put("max_tool_rounds", value);
  }

  public B environmentId(String value) {
    return put("environment_id", value);
  }

  public B tools(List<Tool> value) {
    return put("tools", value);
  }

  public B managedToolConfig(ManagedToolConfig value) {
    return put("managed_tool_config", value);
  }

  public B mcpServers(List<MCPServer> value) {
    return put("mcp_servers", value);
  }

  public B skills(List<SkillBinding> value) {
    return put("skills", value);
  }

  public B multiagent(MultiagentConfig value) {
    return put("multiagent", value);
  }

  public B vaults(Map<String, ResourceBinding> value) {
    return put("vaults", value);
  }

  public B files(Map<String, ResourceBinding> value) {
    return put("files", value);
  }

  public B githubRepositories(Map<String, GitHubRepositoryConfig> value) {
    return put("github_repositories", value);
  }

  public B environmentVariables(EnvironmentVariables value) {
    return put("environment_variables", value);
  }

  public B metadata(Map<String, JsonValue> value) {
    return put("metadata", value);
  }
}
