package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable query parameters; filters are retained when paging. */
public final class ChannelPairingListParams {
  private final Map<String, String> query;

  private ChannelPairingListParams(Builder builder) {
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

    public Builder channelId(String value) {
      return put("channel_id", value);
    }

    public Builder status(String value) {
      return put("status", value);
    }

    public Builder targetType(String value) {
      return put("target_type", value);
    }

    public Builder targetExternalId(String value) {
      return put("target_external_id", value);
    }

    public Builder identityId(String value) {
      return put("identity_id", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
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

    public ChannelPairingListParams build() {
      return new ChannelPairingListParams(this);
    }
  }
}
