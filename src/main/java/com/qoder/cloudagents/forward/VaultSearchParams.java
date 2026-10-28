package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class VaultSearchParams extends JsonObject {
  private final String identityId;

  private VaultSearchParams(JsonValue value) {
    this(value, null);
  }

  private VaultSearchParams(JsonValue value, String identityId) {
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

  public static VaultSearchParams fromJson(JsonValue value) {
    return new VaultSearchParams(value);
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

    public VaultSearchParams build() {
      if (!json().get("identity_id").isMissing())
        throw new ConfigurationException("identityId belongs in the query, not the search body");
      JsonValue limit = json().get("limit");
      if (!limit.isMissing() && !limit.isNull() && (limit.asLong() < 1 || limit.asLong() > 100))
        throw new ConfigurationException("limit must be between 1 and 100");
      return new VaultSearchParams(json(), identityId);
    }
  }
}
