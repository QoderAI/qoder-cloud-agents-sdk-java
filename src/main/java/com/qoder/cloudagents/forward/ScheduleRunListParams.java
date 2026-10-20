package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

public final class ScheduleRunListParams {
  private final Map<String, String> query;

  private ScheduleRunListParams(Builder b) {
    if (b.values.containsKey("after_id") && b.values.containsKey("before_id"))
      throw new ConfigurationException("afterId and beforeId are mutually exclusive");
    if (!b.values.containsKey("identity_id") || b.values.get("identity_id").trim().isEmpty())
      throw new ConfigurationException("identity_id is required");
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

    public Builder identityId(String value) {
      return put("identity_id", value);
    }

    public Builder scheduleId(String value) {
      return put("schedule_id", value);
    }

    public Builder status(String value) {
      return put("status", value);
    }

    public Builder triggerType(String value) {
      return put("trigger_type", value);
    }

    public Builder sortBy(String value) {
      return put("sort_by", value);
    }

    public Builder order(String value) {
      return put("order", value);
    }

    public Builder hasError(boolean value) {
      return put("has_error", String.valueOf(value));
    }

    public Builder limit(int value) {
      if (value < 1 || value > 100) throw new ConfigurationException("limit must be 1..100");
      return put("limit", String.valueOf(value));
    }

    public Builder afterId(String value) {
      return put("after_id", value);
    }

    public Builder beforeId(String value) {
      return put("before_id", value);
    }

    private Builder put(String k, String v) {
      if (v == null) throw new ConfigurationException("Query values cannot be null");
      values.put(k, v);
      return this;
    }

    public ScheduleRunListParams build() {
      return new ScheduleRunListParams(this);
    }
  }
}
