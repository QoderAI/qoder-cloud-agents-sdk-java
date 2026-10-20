package com.qoder.cloudagents.forward.realtime;

import java.util.*;

/** History uses before/next_before, independently of Session event cursors. */
public final class RealtimeHistoryParams {
  private final Map<String, String> query;

  private RealtimeHistoryParams(Builder builder) {
    query = RealtimeValidation.history(builder.query);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Map<String, String> query() {
    return query;
  }

  public static final class Builder {
    private final Map<String, String> query = new LinkedHashMap<>();

    private Builder() {}

    public Builder limit(int value) {
      query.put("limit", String.valueOf(value));
      return this;
    }

    public Builder before(String value) {
      query.put("before", value);
      return this;
    }

    public Builder types(String value) {
      query.put("types", value);
      return this;
    }

    public RealtimeHistoryParams build() {
      return new RealtimeHistoryParams(this);
    }
  }
}
