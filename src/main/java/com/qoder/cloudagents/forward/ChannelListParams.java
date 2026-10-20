package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

public final class ChannelListParams {
  private final Map<String, String> query;

  private ChannelListParams(Builder b) {
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

    public Builder identityId(String value) {
      return put("identity_id", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public Builder channelType(String value) {
      return put("channel_type", value);
    }

    public Builder enabled(boolean value) {
      return put("enabled", Boolean.toString(value));
    }

    public Builder bindingStatus(String value) {
      return put("binding_status", value);
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

    public ChannelListParams build() {
      return new ChannelListParams(this);
    }
  }
}
