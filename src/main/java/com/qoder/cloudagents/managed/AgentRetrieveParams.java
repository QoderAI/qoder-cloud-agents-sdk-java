package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable query parameters for the Managed API. */
public final class AgentRetrieveParams {
  private final Map<String, String> query;

  private AgentRetrieveParams(Builder b) {
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

    public Builder version(long value) {
      if (value <= 0) throw new ConfigurationException("Value must be positive");
      return put("version", String.valueOf(value));
    }

    private Builder put(String key, String value) {
      if (value == null) throw new ConfigurationException("Query parameters cannot be null");
      values.put(key, value);
      return this;
    }

    public AgentRetrieveParams build() {
      return new AgentRetrieveParams(this);
    }
  }
}
