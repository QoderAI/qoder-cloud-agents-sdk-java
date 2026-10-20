package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class SkillUpdateParams extends JsonObject {
  private SkillUpdateParams(JsonValue value) {
    super(value);
  }

  public static SkillUpdateParams fromJson(JsonValue value) {
    return new SkillUpdateParams(value);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public String content() {
    return optional("content", JsonValue::asString);
  }

  public String contentEncoding() {
    return optional("content_encoding", JsonValue::asString);
  }

  public String iconId() {
    return optional("icon_id", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public Map<String, String> metadata() {
    return optionalMap("metadata", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder description(String value) {
      return put("description", value);
    }

    public Builder content(String value) {
      return put("content", value);
    }

    public Builder contentEncoding(String value) {
      return put("content_encoding", value);
    }

    public Builder iconId(String value) {
      return put("icon_id", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public SkillUpdateParams build() {
      if (!json().get("metadata").isMissing() && json().get("metadata").isNull())
        throw new ConfigurationException("Skill metadata cannot be null");
      if (!json().get("content_encoding").isMissing()) require("content");
      return new SkillUpdateParams(json());
    }
  }
}
