package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.List;

/** Authenticated A2A 0.3/1.0 Card preserving camelCase fields and unknown data. */
public final class TemplateAgentCard extends JsonModel {
  public TemplateAgentCard(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String name() {
    return optionalString("name");
  }

  public String description() {
    return optionalString("description");
  }

  public String version() {
    return optionalString("version");
  }

  public String url() {
    return optionalString("url");
  }

  public List<String> defaultInputModes() {
    return optionalList("defaultInputModes", JsonValue::asString);
  }

  public List<String> defaultOutputModes() {
    return optionalList("defaultOutputModes", JsonValue::asString);
  }

  public List<TemplateA2ASkill> skills() {
    return optionalList("skills", TemplateA2ASkill::fromJson);
  }

  public List<TemplateAgentInterface> supportedInterfaces() {
    return optionalList("supportedInterfaces", TemplateAgentInterface::fromJson);
  }
}
