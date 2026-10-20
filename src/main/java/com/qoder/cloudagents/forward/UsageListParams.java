package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Hourly Asia/Shanghai interval for both CN and Global; legacy timestamps are unsupported. */
public final class UsageListParams {
  private final Map<String, String> query;

  private UsageListParams(Builder b) {
    query = Collections.unmodifiableMap(validate(b.values));
  }

  public static Builder builder() {
    return new Builder();
  }

  Map<String, String> query() {
    return query;
  }

  static Map<String, String> validate(Map<String, String> values) {
    Map<String, String> q = new LinkedHashMap<>(values);
    for (String legacy :
        Arrays.asList("start_time", "end_time", "start_date", "end_date", "timezone", "page"))
      if (q.containsKey(legacy))
        throw new ConfigurationException("Usage requires startAt/endAt and cursor pagination");
    java.time.LocalDateTime start = hour(q.get("start_at")), end = hour(q.get("end_at"));
    long hours = java.time.Duration.between(start, end).toHours();
    if (hours <= 0 || hours > 744)
      throw new ConfigurationException("Usage interval must be 1..744 hours");
    if (q.containsKey("after_id") && q.containsKey("before_id"))
      throw new ConfigurationException("afterId and beforeId are mutually exclusive");
    if (q.containsKey("limit")) {
      try {
        int n = Integer.parseInt(q.get("limit"));
        if (n < 1 || n > 100) throw new NumberFormatException();
      } catch (NumberFormatException e) {
        throw new ConfigurationException("limit must be 1..100");
      }
    }
    return q;
  }

  private static java.time.LocalDateTime hour(String value) {
    if (value == null || !value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:00:00"))
      throw new ConfigurationException("Usage times must use YYYY-MM-DDTHH:00:00 in Asia/Shanghai");
    try {
      return java.time.LocalDateTime.parse(value);
    } catch (java.time.DateTimeException e) {
      throw new ConfigurationException("Invalid Usage time");
    }
  }

  public static final class Builder {
    private final Map<String, String> values = new LinkedHashMap<>();

    public Builder startAt(String v) {
      return put("start_at", v);
    }

    public Builder endAt(String v) {
      return put("end_at", v);
    }

    public Builder identityId(String v) {
      return put("identity_id", v);
    }

    public Builder templateId(String v) {
      return put("template_id", v);
    }

    public Builder identityIds(List<String> v) {
      return ids("identity_ids", v);
    }

    public Builder templateIds(List<String> v) {
      return ids("template_ids", v);
    }

    public Builder afterId(String v) {
      return put("after_id", v);
    }

    public Builder beforeId(String v) {
      return put("before_id", v);
    }

    public Builder limit(int v) {
      return put("limit", Integer.toString(v));
    }

    private Builder ids(String name, List<String> v) {
      if (v == null || v.isEmpty() || v.stream().anyMatch(s -> s == null || s.isEmpty()))
        throw new ConfigurationException("Usage ID filters must contain nonempty values");
      return put(name, String.join(",", v));
    }

    private Builder put(String k, String v) {
      if (v == null) throw new ConfigurationException("Query values cannot be null");
      values.put(k, v);
      return this;
    }

    public UsageListParams build() {
      return new UsageListParams(this);
    }
  }
}
