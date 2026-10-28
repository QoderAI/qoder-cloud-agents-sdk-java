package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionUpdateParams extends JsonObject {
  private SessionUpdateParams(JsonValue value) {
    super(value);
  }

  public static SessionUpdateParams fromJson(JsonValue value) {
    return new SessionUpdateParams(value);
  }

  public SessionAgentPatch agent() {
    return optional("agent", SessionAgentPatch::fromJson);
  }

  public SessionBudget budget() {
    return optional("budget", SessionBudget::fromJson);
  }

  public String title() {
    return optional("title", JsonValue::asString);
  }

  public Map<String, JsonValue> metadata() {
    return optionalNullableMap("metadata", v -> v);
  }

  public Map<String, String> environmentVariables() {
    return optionalNullableMap("environment_variables", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder agent(SessionAgentPatch value) {
      return put("agent", value);
    }

    public Builder budget(SessionBudget value) {
      return put("budget", value);
    }

    public Builder title(String value) {
      return put("title", value);
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public Builder environmentVariables(Map<String, String> value) {
      return put("environment_variables", value);
    }

    public SessionUpdateParams build() {
      JsonValue agent = json().get("agent");
      if (!agent.isMissing() && !agent.isNull() && !agent.isObject())
        throw new ConfigurationException("Session agent updates require a runtime patch");
      return new SessionUpdateParams(json());
    }
  }
}
