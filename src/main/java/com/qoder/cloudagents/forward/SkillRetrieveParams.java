package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

public final class SkillRetrieveParams {
  private final Map<String, String> query;

  private SkillRetrieveParams(Builder b) {
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

    /**
     * @deprecated Download a specific version instead.
     */
    @Deprecated
    public Builder includeContent(boolean value) {
      values.put("include_content", Boolean.toString(value));
      return this;
    }

    public Builder identityId(String value) {
      if (value == null) throw new ConfigurationException("identityId cannot be null");
      values.put("identity_id", value);
      return this;
    }

    public SkillRetrieveParams build() {
      return new SkillRetrieveParams(this);
    }
  }
}
