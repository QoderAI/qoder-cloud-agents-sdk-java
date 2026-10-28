package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class EnvironmentConfig extends JsonObject {
  private EnvironmentConfig(JsonValue value) {
    super(value);
  }

  public static EnvironmentConfig fromJson(JsonValue value) {
    return new EnvironmentConfig(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String setupScript() {
    return optional("setup_script", JsonValue::asString);
  }

  public EnvironmentPackages packages() {
    return optional("packages", EnvironmentPackages::fromJson);
  }

  public EnvironmentStorage storage() {
    return optional("storage", EnvironmentStorage::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder setupScript(String value) {
      return put("setup_script", value);
    }

    public Builder packages(EnvironmentPackages value) {
      return put("packages", value);
    }

    public Builder storage(EnvironmentStorage value) {
      return put("storage", value);
    }

    public EnvironmentConfig build() {
      require("type");

      return new EnvironmentConfig(json());
    }
  }
}
