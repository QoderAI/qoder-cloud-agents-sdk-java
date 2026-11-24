package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** A2A 1.0 invocation interface preserving camelCase protocol binding and version fields. */
public final class TemplateAgentInterface extends JsonObject {
  private TemplateAgentInterface(JsonValue value) {
    super(value);
  }

  public static TemplateAgentInterface fromJson(JsonValue value) {
    return new TemplateAgentInterface(value);
  }

  public String url() {
    return optionalString("url");
  }

  public String protocolBinding() {
    return optionalString("protocolBinding");
  }

  public String protocolVersion() {
    return optionalString("protocolVersion");
  }
}
