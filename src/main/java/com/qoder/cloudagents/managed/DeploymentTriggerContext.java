package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DeploymentTriggerContext extends JsonObject {
  private DeploymentTriggerContext(JsonValue value) {
    super(value);
  }

  public static DeploymentTriggerContext fromJson(JsonValue value) {
    return new DeploymentTriggerContext(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String scheduledAt() {
    return optional("scheduled_at", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder scheduledAt(String value) {
      return put("scheduled_at", value);
    }

    public DeploymentTriggerContext build() {

      return new DeploymentTriggerContext(json());
    }
  }
}
