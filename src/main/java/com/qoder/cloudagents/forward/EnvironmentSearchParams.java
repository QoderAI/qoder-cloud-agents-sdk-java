package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class EnvironmentSearchParams extends JsonObject {
  private final String identityId;

  private EnvironmentSearchParams(JsonValue value) {
    this(value, null);
  }

  private EnvironmentSearchParams(JsonValue value, String identityId) {
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

  public static EnvironmentSearchParams fromJson(JsonValue value) {
    return new EnvironmentSearchParams(value);
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

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public Boolean includeArchived() {
    return optional("include_archived", JsonValue::asBoolean);
  }

  public String createdAtGte() {
    return optional("created_at[gte]", JsonValue::asString);
  }

  public String createdAtLte() {
    return optional("created_at[lte]", JsonValue::asString);
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

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder includeArchived(Boolean value) {
      return put("include_archived", value);
    }

    public Builder createdAtGte(String value) {
      return put("created_at[gte]", value);
    }

    public Builder createdAtLte(String value) {
      return put("created_at[lte]", value);
    }

    public EnvironmentSearchParams build() {
      if (!json().get("identity_id").isMissing())
        throw new ConfigurationException("identityId belongs in the query, not the search body");
      JsonValue limit = json().get("limit");
      if (!limit.isMissing() && !limit.isNull() && (limit.asLong() < 1 || limit.asLong() > 100))
        throw new ConfigurationException("limit must be between 1 and 100");
      return new EnvironmentSearchParams(json(), identityId);
    }
  }
}
