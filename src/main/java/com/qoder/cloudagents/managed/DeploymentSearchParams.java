package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DeploymentSearchParams extends JsonObject {
  private DeploymentSearchParams(JsonValue value) {
    super(value);
  }

  public static DeploymentSearchParams fromJson(JsonValue value) {
    return new DeploymentSearchParams(value);
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

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public Boolean includeArchived() {
    return optional("include_archived", JsonValue::asBoolean);
  }

  public String agentId() {
    return optional("agent_id", JsonValue::asString);
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

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder status(String value) {
      return put("status", value);
    }

    public Builder includeArchived(Boolean value) {
      return put("include_archived", value);
    }

    public Builder agentId(String value) {
      return put("agent_id", value);
    }

    public Builder createdAtGte(String value) {
      return put("created_at[gte]", value);
    }

    public Builder createdAtLte(String value) {
      return put("created_at[lte]", value);
    }

    public DeploymentSearchParams build() {
      JsonValue limit = json().get("limit");
      if (!limit.isMissing() && !limit.isNull() && (limit.asLong() < 1 || limit.asLong() > 100))
        throw new ConfigurationException("limit must be between 1 and 100");
      return new DeploymentSearchParams(json());
    }
  }
}
