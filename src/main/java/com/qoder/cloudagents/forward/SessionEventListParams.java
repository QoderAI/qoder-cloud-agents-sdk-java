package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable cursor filters; later pages retain the same selectors. */
public final class SessionEventListParams {
  private final Map<String, String> query;

  private SessionEventListParams(Builder b) {
    if (b.values.containsKey("after_id") && b.values.containsKey("before_id"))
      throw new ConfigurationException("afterId and beforeId are mutually exclusive");
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

    public Builder limit(int value) {
      if (value < 1 || value > 100)
        throw new ConfigurationException("limit must be between 1 and 100");
      return put("limit", Integer.toString(value));
    }

    public Builder order(String value) {
      return put("order", value);
    }

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder includeToolCalls(boolean value) {
      return put("include_tool_calls", Boolean.toString(value));
    }

    public Builder includeThinking(boolean value) {
      return put("include_thinking", Boolean.toString(value));
    }

    public Builder afterId(String value) {
      return put("after_id", value);
    }

    public Builder beforeId(String value) {
      return put("before_id", value);
    }

    /** Uses the documented comma-separated query form. */
    public Builder types(List<String> values) {
      if (values == null || values.isEmpty())
        throw new ConfigurationException("types must not be empty");
      for (String value : values)
        if (value == null || value.isEmpty() || value.contains(","))
          throw new ConfigurationException(
              "Each filter value must be nonempty and contain no comma");
      return put("type", String.join(",", values));
    }

    private Builder put(String key, String value) {
      if (value == null) throw new ConfigurationException("List parameters cannot be null");
      values.put(key, value);
      return this;
    }

    public SessionEventListParams build() {
      return new SessionEventListParams(this);
    }
  }
}
