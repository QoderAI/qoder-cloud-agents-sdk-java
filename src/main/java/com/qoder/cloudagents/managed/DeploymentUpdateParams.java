package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DeploymentUpdateParams extends JsonObject {
  private DeploymentUpdateParams(JsonValue value) {
    super(value);
  }

  public static DeploymentUpdateParams fromJson(JsonValue value) {
    return new DeploymentUpdateParams(value);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public AgentReference agent() {
    return optional("agent", AgentReference::fromJson);
  }

  public String environmentId() {
    return optional("environment_id", JsonValue::asString);
  }

  public DeploymentSchedule schedule() {
    return optional("schedule", DeploymentSchedule::fromJson);
  }

  public List<SessionEventParam> initialEvents() {
    return optionalList("initial_events", SessionEventParam::fromJson);
  }

  public List<SessionResourceSpec> resources() {
    return optionalList("resources", SessionResourceSpec::fromJson);
  }

  public List<String> vaultIds() {
    return optionalList("vault_ids", JsonValue::asString);
  }

  public Map<String, String> metadata() {
    return optionalNullableMap("metadata", JsonValue::asString);
  }

  public String environmentVariables() {
    return optional("environment_variables", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public Builder agent(AgentReference value) {
      return put("agent", value);
    }

    public Builder agent(String value) {
      return put("agent", value == null ? null : AgentReference.of(value));
    }

    public Builder environmentId(String value) {
      return put("environment_id", value);
    }

    public Builder schedule(DeploymentSchedule value) {
      return put("schedule", value);
    }

    public Builder initialEvents(List<SessionEventParam> value) {
      return put("initial_events", value);
    }

    public Builder resources(List<SessionResourceSpec> value) {
      return put("resources", value);
    }

    public Builder vaultIds(List<String> value) {
      return put("vault_ids", value);
    }

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public Builder environmentVariables(String value) {
      return put("environment_variables", value);
    }

    public DeploymentUpdateParams build() {
      JsonValue events = json().get("initial_events");
      if (!events.isMissing()
          && !events.isNull()
          && (events.asArray().isEmpty() || events.asArray().size() > 50))
        throw new ConfigurationException("initialEvents requires 1-50 events");
      return new DeploymentUpdateParams(json());
    }
  }
}
