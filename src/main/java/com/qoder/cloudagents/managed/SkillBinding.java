package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonValue;

/** Immutable SkillBinding configuration. Unknown fields are retained through field(). */
public final class SkillBinding extends JsonObject {
  private SkillBinding(JsonValue value) {
    super(value);
  }

  public static SkillBinding fromJson(JsonValue value) {
    return new SkillBinding(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String skillId() {
    return optional("skill_id", JsonValue::asString);
  }

  public String version() {
    return optional("version", JsonValue::asString);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder skillId(String value) {
      return put("skill_id", value);
    }

    public Builder version(String value) {
      return put("version", value);
    }

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public SkillBinding build() {
      require("type", "skill_id");
      return new SkillBinding(json());
    }
  }
}
