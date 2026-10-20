package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** One directory page; pass nextPageToken unchanged with the same identity and path. */
public final class DriveListEntriesParams {
  private final Map<String, String> query;

  private DriveListEntriesParams(Builder b) {
    if (!b.values.containsKey("identity_id") || b.values.get("identity_id").isEmpty())
      throw new ConfigurationException("identityId is required");
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

    public Builder identityId(String v) {
      return put("identity_id", v);
    }

    public Builder path(String v) {
      return put("path", v);
    }

    public Builder pageToken(String v) {
      return put("page_token", v);
    }

    public Builder limit(int v) {
      if (v < 1 || v > 100) throw new ConfigurationException("limit must be 1..100");
      return put("limit", Integer.toString(v));
    }

    private Builder put(String k, String v) {
      if (v == null) throw new ConfigurationException("Query values cannot be null");
      values.put(k, v);
      return this;
    }

    public DriveListEntriesParams build() {
      return new DriveListEntriesParams(this);
    }
  }
}
