package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.List;
import java.util.Map;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class EffectiveConfigAgent extends JsonObject {
  private EffectiveConfigAgent(JsonValue value) {
    super(value);
  }

  public static EffectiveConfigAgent fromJson(JsonValue value) {
    return new EffectiveConfigAgent(value);
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

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", value -> value);
  }

  public Long maxToolRounds() {
    return optional("max_tool_rounds", JsonValue::asLong);
  }

  public MultiagentConfig multiagent() {
    return optional("multiagent", MultiagentConfig::fromJson);
  }
}
