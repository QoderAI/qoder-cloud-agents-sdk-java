package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable cursor filters; later pages retain the same selectors. */
public final class SessionListParams {
  private final Map<String, String> query;

  private SessionListParams(Builder b) {
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

    public Builder search(String value) {
      return put("search", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public Builder sourceType(String value) {
      return put("source_type", value);
    }

    public Builder order(String value) {
      return put("order", value);
    }

    public Builder includeArchived(boolean value) {
      return put("include_archived", Boolean.toString(value));
    }

    public Builder createdAtGt(String value) {
      return put("created_at[gt]", value);
    }

    public Builder createdAtGte(String value) {
      return put("created_at[gte]", value);
    }

    public Builder createdAtLt(String value) {
      return put("created_at[lt]", value);
    }

    public Builder createdAtLte(String value) {
      return put("created_at[lte]", value);
    }

    public Builder updatedAtGt(String value) {
      return put("updated_at[gt]", value);
    }

    public Builder updatedAtGte(String value) {
      return put("updated_at[gte]", value);
    }

    public Builder updatedAtLt(String value) {
      return put("updated_at[lt]", value);
    }

    public Builder updatedAtLte(String value) {
      return put("updated_at[lte]", value);
    }

    public Builder afterId(String value) {
      return put("after_id", value);
    }

    public Builder beforeId(String value) {
      return put("before_id", value);
    }

    /** Uses the documented comma-separated query form. */
    public Builder identityIds(List<String> values) {
      if (values == null || values.isEmpty())
        throw new ConfigurationException("identityIds must not be empty");
      for (String value : values)
        if (value == null || value.isEmpty() || value.contains(","))
          throw new ConfigurationException(
              "Each filter value must be nonempty and contain no comma");
      return put("identity_ids", String.join(",", values));
    }

    private Builder put(String key, String value) {
      if (value == null) throw new ConfigurationException("List parameters cannot be null");
      values.put(key, value);
      return this;
    }

    public SessionListParams build() {
      return new SessionListParams(this);
    }
  }
}
