package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable query parameters for the Managed API. */
public final class WorkPollParams {
  private final Map<String, String> query;

  private WorkPollParams(Builder b) {
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

    public Builder blockMs(long value) {
      if (value < 1 || value > 999)
        throw new ConfigurationException("blockMs must be between 1 and 999");
      return put("block_ms", String.valueOf(value));
    }

    public Builder reclaimOlderThanMs(long value) {
      if (value < 0) throw new ConfigurationException("Value must not be negative");
      return put("reclaim_older_than_ms", String.valueOf(value));
    }

    private Builder put(String key, String value) {
      if (value == null) throw new ConfigurationException("Query parameters cannot be null");
      values.put(key, value);
      return this;
    }

    public WorkPollParams build() {
      return new WorkPollParams(this);
    }
  }
}
