package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionCreateParams extends JsonObject {
  private SessionCreateParams(JsonValue value) {
    super(value);
  }

  public static SessionCreateParams fromJson(JsonValue value) {
    return new SessionCreateParams(value);
  }

  public AgentReference agent() {
    return optional("agent", AgentReference::fromJson);
  }

  public String environmentId() {
    return optional("environment_id", JsonValue::asString);
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

  public List<SessionResourceSpec> resources() {
    return optionalList("resources", SessionResourceSpec::fromJson);
  }

  public List<String> vaultIds() {
    return optionalList("vault_ids", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder agent(AgentReference value) {
      return put("agent", value);
    }

    public Builder agent(String value) {
      return put("agent", value == null ? null : AgentReference.of(value));
    }

    public Builder environmentId(String value) {
      return put("environment_id", value);
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

    public Builder resources(List<SessionResourceSpec> value) {
      return put("resources", value);
    }

    public Builder vaultIds(List<String> value) {
      return put("vault_ids", value);
    }

    public SessionCreateParams build() {
      require("agent", "environment_id");

      return new SessionCreateParams(json());
    }
  }
}
