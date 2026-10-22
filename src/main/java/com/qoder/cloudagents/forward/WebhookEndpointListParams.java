package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

public final class WebhookEndpointListParams {
  private final Map<String, String> query;

  private WebhookEndpointListParams(Builder b) {
    query = Collections.unmodifiableMap(new LinkedHashMap<>(b.values));
  }

  public static Builder builder() {
    return new Builder();
  }

  Map<String, String> query() {
    return query;
  }

  public static final class Builder {
    private final Map<String, String> values = new LinkedHashMap<>();

    public Builder afterId(String value) {
      return put("after_id", value);
    }

    public Builder limit(int value) {
      if (value < 1 || value > 100) throw new ConfigurationException("limit must be 1..100");
      return put("limit", Integer.toString(value));
    }

    private Builder put(String name, String value) {
      if (value == null) throw new ConfigurationException("List parameters cannot be null");
      values.put(name, value);
      return this;
    }

    public WebhookEndpointListParams build() {
      return new WebhookEndpointListParams(this);
    }
  }
}
