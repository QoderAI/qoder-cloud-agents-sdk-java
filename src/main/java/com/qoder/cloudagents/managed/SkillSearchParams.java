package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SkillSearchParams extends JsonObject {
  private SkillSearchParams(JsonValue value) {
    super(value);
  }

  public static SkillSearchParams fromJson(JsonValue value) {
    return new SkillSearchParams(value);
  }

  public Map<String, String> metadata() {
    return optionalNullableMap("metadata", JsonValue::asString);
  }

  public Long limit() {
    return optional("limit", JsonValue::asLong);
  }

  public String page() {
    return optional("page", JsonValue::asString);
  }

  public String source() {
    return optional("source", JsonValue::asString);
  }

  public String displayTitle() {
    return optional("display_title", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public Builder limit(Long value) {
      return put("limit", value);
    }

    public Builder limit(long value) {
      return limit(Long.valueOf(value));
    }

    public Builder page(String value) {
      return put("page", value);
    }

    public Builder source(String value) {
      return put("source", value);
    }

    public Builder displayTitle(String value) {
      return put("display_title", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public SkillSearchParams build() {
      JsonValue limit = json().get("limit");
      if (!limit.isMissing() && !limit.isNull() && (limit.asLong() < 1 || limit.asLong() > 100))
        throw new ConfigurationException("limit must be between 1 and 100");
      return new SkillSearchParams(json());
    }
  }
}
