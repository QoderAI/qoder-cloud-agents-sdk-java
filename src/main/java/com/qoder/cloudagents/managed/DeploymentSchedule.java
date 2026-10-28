package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DeploymentSchedule extends JsonObject {
  private DeploymentSchedule(JsonValue value) {
    super(value);
  }

  public static DeploymentSchedule fromJson(JsonValue value) {
    return new DeploymentSchedule(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String expression() {
    return optional("expression", JsonValue::asString);
  }

  public String timezone() {
    return optional("timezone", JsonValue::asString);
  }

  public List<String> upcomingRunsAt() {
    return optionalList("upcoming_runs_at", JsonValue::asString);
  }

  public String lastRunAt() {
    return optional("last_run_at", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder expression(String value) {
      return put("expression", value);
    }

    public Builder timezone(String value) {
      return put("timezone", value);
    }

    public Builder upcomingRunsAt(List<String> value) {
      return put("upcoming_runs_at", value);
    }

    public Builder lastRunAt(String value) {
      return put("last_run_at", value);
    }

    public DeploymentSchedule build() {
      require("type", "expression", "timezone");

      return new DeploymentSchedule(json());
    }
  }
}
