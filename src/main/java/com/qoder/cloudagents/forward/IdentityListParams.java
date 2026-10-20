package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable query parameters; pagination retains filters across pages. */
public final class IdentityListParams {
  private final Map<String, String> query;

  private IdentityListParams(Builder builder) {
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

    public Builder externalId(String value) {
      return put("external_id", value);
    }

    public Builder search(String value) {
      return put("search", value);
    }

    public Builder enabled(boolean value) {
      return put("enabled", Boolean.toString(value));
    }

    public Builder limit(int value) {
      if (value < 1) throw new ConfigurationException("limit must be positive");
      return put("limit", Integer.toString(value));
    }

    public Builder afterId(String value) {
      return put("after_id", value);
    }

    public Builder beforeId(String value) {
      return put("before_id", value);
    }

    /** Uses the endpoint's comma-separated form; the server validates its ID/count rules. */
    public Builder identityIds(List<String> values) {
      if (values == null || values.isEmpty())
        throw new ConfigurationException("identityIds must not be empty");
      for (String value : values)
        if (value == null || value.isEmpty() || value.contains(","))
          throw new ConfigurationException(
              "Each identity ID must be nonempty and cannot contain a comma");
      return put("identity_ids", String.join(",", values));
    }

    private Builder put(String key, String value) {
      if (value == null) throw new ConfigurationException("List parameters cannot be null");
      values.put(key, value);
      return this;
    }

    public IdentityListParams build() {
      return new IdentityListParams(this);
    }
  }
}
