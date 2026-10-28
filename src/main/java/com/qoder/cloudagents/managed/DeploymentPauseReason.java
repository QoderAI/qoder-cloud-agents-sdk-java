package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DeploymentPauseReason extends JsonObject {
  private DeploymentPauseReason(JsonValue value) {
    super(value);
  }

  public static DeploymentPauseReason fromJson(JsonValue value) {
    return new DeploymentPauseReason(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String message() {
    return optional("message", JsonValue::asString);
  }

  public String code() {
    return optional("code", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder message(String value) {
      return put("message", value);
    }

    public Builder code(String value) {
      return put("code", value);
    }

    public DeploymentPauseReason build() {

      return new DeploymentPauseReason(json());
    }
  }
}
