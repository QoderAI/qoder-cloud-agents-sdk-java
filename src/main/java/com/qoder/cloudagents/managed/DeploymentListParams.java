package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable query parameters for the Managed API. */
public final class DeploymentListParams {
  private final Map<String, String> query;

  private DeploymentListParams(Builder b) {
    int cursors = 0;
    for (String key : Arrays.asList("page", "after_id", "before_id"))
      if (b.values.containsKey(key)) cursors++;
    if (cursors > 1)
      throw new ConfigurationException("page, afterId and beforeId are mutually exclusive");
    query = Collections.unmodifiableMap(new LinkedHashMap<>(b.values));
  }

  Map<String, String> query() {
    return query;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private final Map<String, String> values = new LinkedHashMap<>();

    public Builder limit(long value) {
      if (value < 1 || value > 100)
        throw new ConfigurationException("limit must be between 1 and 100");
      return put("limit", String.valueOf(value));
    }

    public Builder page(String value) {
      return put("page", value);
    }

    public Builder afterId(String value) {
      return put("after_id", value);
    }

    public Builder beforeId(String value) {
      return put("before_id", value);
    }

    public Builder status(String value) {
      return put("status", value);
    }

    public Builder includeArchived(boolean value) {
      return put("include_archived", String.valueOf(value));
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

    private Builder put(String key, String value) {
      if (value == null) throw new ConfigurationException("Query parameters cannot be null");
      values.put(key, value);
      return this;
    }

    public DeploymentListParams build() {
      return new DeploymentListParams(this);
    }
  }
}
