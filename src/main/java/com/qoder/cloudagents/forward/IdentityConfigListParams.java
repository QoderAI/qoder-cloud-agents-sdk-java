package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable query parameters; pagination retains filters across pages. */
public final class IdentityConfigListParams {
  private final Map<String, String> query;

  private IdentityConfigListParams(Builder builder) {
    if (builder.values.containsKey("after_id") && builder.values.containsKey("before_id"))
      throw new ConfigurationException("afterId and beforeId are mutually exclusive");
    query = Collections.unmodifiableMap(new LinkedHashMap<>(builder.values));
  }

  public static Builder builder() {
    return new Builder();
  }

  Map<String, String> query() {
    return query;
  }

  public static final class Builder {
    private final Map<String, String> values = new LinkedHashMap<>();

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public Builder status(String value) {
      return put("status", value);
    }

    public Builder limit(int value) {
      if (value < 1 || value > 100) throw new ConfigurationException("limit must be from 1 to 100");
      return put("limit", Integer.toString(value));
    }

    public Builder afterId(String value) {
      return put("after_id", value);
    }

    public Builder beforeId(String value) {
      return put("before_id", value);
    }

    private Builder put(String key, String value) {
      if (value == null) throw new ConfigurationException("List parameters cannot be null");
      values.put(key, value);
      return this;
    }

    public IdentityConfigListParams build() {
      return new IdentityConfigListParams(this);
    }
  }
}
