package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

public final class SkillVersionListParams {
  private final Map<String, String> query;

  private SkillVersionListParams(Builder b) {
    int cursors =
        (b.values.containsKey("page") ? 1 : 0)
            + (b.values.containsKey("before_id") ? 1 : 0)
            + (b.values.containsKey("after_id") ? 1 : 0);
    if (cursors > 1)
      throw new ConfigurationException("page, beforeId and afterId are mutually exclusive");

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

    public Builder page(String value) {
      return put("page", value);
    }

    public Builder identityId(String value) {
      return put("identity_id", value);
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

    public SkillVersionListParams build() {
      return new SkillVersionListParams(this);
    }
  }
}
