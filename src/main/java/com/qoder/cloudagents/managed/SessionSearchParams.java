package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionSearchParams extends JsonObject {
  private SessionSearchParams(JsonValue value) {
    super(value);
  }

  public static SessionSearchParams fromJson(JsonValue value) {
    return new SessionSearchParams(value);
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

  public String order() {
    return optional("order", JsonValue::asString);
  }

  public Boolean includeArchived() {
    return optional("include_archived", JsonValue::asBoolean);
  }

  public List<String> statuses() {
    return optionalList("statuses", JsonValue::asString);
  }

  public String agentId() {
    return optional("agent_id", JsonValue::asString);
  }

  public Long agentVersion() {
    return optional("agent_version", JsonValue::asLong);
  }

  public String deploymentId() {
    return optional("deployment_id", JsonValue::asString);
  }

  public String memoryStoreId() {
    return optional("memory_store_id", JsonValue::asString);
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

    public Builder order(String value) {
      return put("order", value);
    }

    public Builder includeArchived(Boolean value) {
      return put("include_archived", value);
    }

    public Builder statuses(List<String> value) {
      return put("statuses", value);
    }

    public Builder agentId(String value) {
      return put("agent_id", value);
    }

    public Builder agentVersion(Long value) {
      return put("agent_version", value);
    }

    public Builder agentVersion(long value) {
      return agentVersion(Long.valueOf(value));
    }

    public Builder deploymentId(String value) {
      return put("deployment_id", value);
    }

    public Builder memoryStoreId(String value) {
      return put("memory_store_id", value);
    }

    public SessionSearchParams build() {
      JsonValue limit = json().get("limit");
      if (!limit.isMissing() && !limit.isNull() && (limit.asLong() < 1 || limit.asLong() > 100))
        throw new ConfigurationException("limit must be between 1 and 100");
      return new SessionSearchParams(json());
    }
  }
}
