package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ScheduleUpdateParams extends JsonObject {
  private ScheduleUpdateParams(JsonValue value) {
    super(value);
  }

  public static ScheduleUpdateParams fromJson(JsonValue value) {
    return new ScheduleUpdateParams(value);
  }

  public List<SessionResourceSpec> resources() {
    return optionalList("resources", SessionResourceSpec::fromJson);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String environmentId() {
    return optional("environment_id", JsonValue::asString);
  }

  public List<ScheduleInitialEvent> initialEvents() {
    return optionalList("initial_events", ScheduleInitialEvent::fromJson);
  }

  public ScheduleExecution execution() {
    return optional("execution", ScheduleExecution::fromJson);
  }

  public ScheduleTriggerPolicyParams triggerPolicy() {
    return optional("trigger_policy", ScheduleTriggerPolicyParams::fromJson);
  }

  public List<ScheduleSink> sinks() {
    return optionalList("sinks", ScheduleSink::fromJson);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder resources(List<SessionResourceSpec> value) {
      return put("resources", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public Builder environmentId(String value) {
      return put("environment_id", value);
    }

    public Builder initialEvents(List<ScheduleInitialEvent> value) {
      return put("initial_events", value);
    }

    public Builder execution(ScheduleExecution value) {
      return put("execution", value);
    }

    public Builder triggerPolicy(ScheduleTriggerPolicyParams value) {
      return put("trigger_policy", value);
    }

    public Builder sinks(List<ScheduleSink> value) {
      return put("sinks", value);
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public ScheduleUpdateParams build() {
      if (json().get("sinks").isArray()
          && (json().get("sinks").asArray().size() > 1
              || json().get("sinks").asArray().stream().anyMatch(JsonValue::isNull)))
        throw new ConfigurationException("sinks accepts at most one non-null target");
      return new ScheduleUpdateParams(json());
    }
  }
}
