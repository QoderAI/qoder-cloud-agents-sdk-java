package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class Agent extends JsonModel {
  public Agent(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static Agent fromJson(JsonValue value) {
    return new Agent(value, null);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
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

  public Long version() {
    return optional("version", JsonValue::asLong);
  }

  public String archivedAt() {
    return optional("archived_at", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }
}
