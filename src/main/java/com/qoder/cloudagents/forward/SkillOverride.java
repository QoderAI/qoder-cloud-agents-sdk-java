package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class SkillOverride extends JsonObject {
  private SkillOverride(JsonValue value) {
    super(value);
  }

  public static SkillOverride fromJson(JsonValue value) {
    return new SkillOverride(value);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String version() {
    return optional("version", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder version(String value) {
      return put("version", value);
    }

    public SkillOverride build() {

      return new SkillOverride(json());
    }
  }
}
