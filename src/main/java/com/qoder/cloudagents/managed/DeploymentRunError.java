package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DeploymentRunError extends JsonObject {
  private DeploymentRunError(JsonValue value) {
    super(value);
  }

  public static DeploymentRunError fromJson(JsonValue value) {
    return new DeploymentRunError(value);
  }

  public String message() {
    return optional("message", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder message(String value) {
      return put("message", value);
    }

    public Builder type(String value) {
      return put("type", value);
    }

    public DeploymentRunError build() {

      return new DeploymentRunError(json());
    }
  }
}
