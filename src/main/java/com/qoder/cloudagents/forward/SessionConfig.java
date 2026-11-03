package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionConfig extends JsonObject {
  private SessionConfig(JsonValue value) {
    super(value);
  }

  public static SessionConfig fromJson(JsonValue value) {
    return new SessionConfig(value);
  }

  public ModelConfig model() {
    return optional("model", ModelConfig::fromJson);
  }

  public Map<String, String> environmentVariables() {
    return optionalMap("environment_variables", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    /** Omit on create to inherit; null on update restores inheritance. */
    public Builder model(ModelConfig value) {
      return put("model", value);
    }

    public Builder environmentVariables(Map<String, String> value) {
      return put("environment_variables", value);
    }

    public SessionConfig build() {
      return new SessionConfig(json());
    }
  }
}
