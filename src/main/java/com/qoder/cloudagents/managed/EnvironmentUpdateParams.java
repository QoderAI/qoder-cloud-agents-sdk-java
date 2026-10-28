package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class EnvironmentUpdateParams extends JsonObject {
  private EnvironmentUpdateParams(JsonValue value) {
    super(value);
  }

  public static EnvironmentUpdateParams fromJson(JsonValue value) {
    return new EnvironmentUpdateParams(value);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public EnvironmentConfig config() {
    return optional("config", EnvironmentConfig::fromJson);
  }

  public Map<String, String> metadata() {
    return optionalMap("metadata", JsonValue::asString);
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

    public Builder config(EnvironmentConfig value) {
      return put("config", value);
    }

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public EnvironmentUpdateParams build() {
      return new EnvironmentUpdateParams(json());
    }
  }
}
