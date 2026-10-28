package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SkillSearchParams extends JsonObject {
  private final String identityId;

  private SkillSearchParams(JsonValue value) {
    this(value, null);
  }

  private SkillSearchParams(JsonValue value, String identityId) {
    super(value);
    this.identityId = identityId;
  }

  public String identityId() {
    return identityId;
  }

  Map<String, String> query() {
    return identityId == null
        ? Collections.emptyMap()
        : Collections.singletonMap("identity_id", identityId);
  }

  public static SkillSearchParams fromJson(JsonValue value) {
    return new SkillSearchParams(value);
  }

  public Map<String, String> metadata() {
    return optionalMap("metadata", JsonValue::asString);
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

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private String identityId;

    private Builder() {}

    public Builder identityId(String value) {
      if (value == null) throw new ConfigurationException("identityId must not be null");
      identityId = value;
      return this;
    }

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public Builder limit(long value) {
      return limit(Long.valueOf(value));
    }

    public Builder limit(Long value) {
      return put("limit", value);
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

    public SkillSearchParams build() {
      if (!json().get("identity_id").isMissing())
        throw new ConfigurationException("identityId belongs in the query, not the search body");
      JsonValue limit = json().get("limit");
      if (!limit.isMissing() && !limit.isNull() && (limit.asLong() < 1 || limit.asLong() > 100))
        throw new ConfigurationException("limit must be between 1 and 100");
      return new SkillSearchParams(json(), identityId);
    }
  }
}
