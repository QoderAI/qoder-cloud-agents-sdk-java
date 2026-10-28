package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable query parameters; filters are retained when paging. */
public final class DreamListParams {
  private final Map<String, String> query;

  private DreamListParams(Builder builder) {
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

    public Builder includeArchived(boolean value) {
      return put("include_archived", Boolean.toString(value));
    }

    public Builder limit(int value) {
      if (value < 1 || value > 100)
        throw new ConfigurationException("limit must be between 1 and 100");
      return put("limit", Integer.toString(value));
    }

    public Builder afterId(String value) {
      return put("after_id", value);
    }

    public Builder beforeId(String value) {
      return put("before_id", value);
    }

    private Builder put(String key, String value) {
      if (value == null) throw new ConfigurationException("Query values cannot be null");
      values.put(key, value);
      return this;
    }

    public DreamListParams build() {
      return new DreamListParams(this);
    }
  }
}
