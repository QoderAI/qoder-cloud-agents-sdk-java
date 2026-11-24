package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.Map;

/** Session-only updates; multiagent null clears the roster without restoring inheritance. */
public final class SessionUpdateConfig extends JsonObject {
  private SessionUpdateConfig(JsonValue value) {
    super(value);
  }

  public static SessionUpdateConfig fromJson(JsonValue value) {
    return new SessionUpdateConfig(value);
  }

  public ModelConfig model() {
    return optional("model", ModelConfig::fromJson);
  }

  public Map<String, String> environmentVariables() {
    return optionalMap("environment_variables", JsonValue::asString);
  }

  public MultiagentConfig multiagent() {
    return optional("multiagent", MultiagentConfig::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder model(ModelConfig value) {
      return put("model", value);
    }

    public Builder environmentVariables(Map<String, String> value) {
      return put("environment_variables", value);
    }

    /** Omit to keep the pinned roster; null saves an empty override, not inheritance. */
    public Builder multiagent(MultiagentConfig value) {
      return put("multiagent", value);
    }

    public SessionUpdateConfig build() {
      return new SessionUpdateConfig(json());
    }
  }
}
