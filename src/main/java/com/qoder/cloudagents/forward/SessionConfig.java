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

  public Map<String, String> environmentVariables() {
    return optionalMap("environment_variables", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder environmentVariables(Map<String, String> value) {
      return put("environment_variables", value);
    }

    public SessionConfig build() {
      return new SessionConfig(json());
    }
  }
}
