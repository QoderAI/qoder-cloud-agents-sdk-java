package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

public final class ScheduleRunRetrieveParams {
  private final Map<String, String> query;

  private ScheduleRunRetrieveParams(Builder b) {
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

    private Builder put(String k, String v) {
      if (v == null) throw new ConfigurationException("Query values cannot be null");
      values.put(k, v);
      return this;
    }

    public ScheduleRunRetrieveParams build() {
      return new ScheduleRunRetrieveParams(this);
    }
  }
}
