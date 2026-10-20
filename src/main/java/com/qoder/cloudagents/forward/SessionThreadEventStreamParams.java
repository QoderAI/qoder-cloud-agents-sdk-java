package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.RequestOptions;
import java.util.*;

/** Immutable SSE subscription parameters. The cursor is sent as a header, never as a query. */
public final class SessionThreadEventStreamParams {
  private final String lastEventId;

  private SessionThreadEventStreamParams(Builder b) {
    lastEventId = b.lastEventId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String lastEventId() {
    return lastEventId;
  }

  Map<String, List<String>> query() {
    return Collections.emptyMap();
  }

  public static final class Builder {
    private String lastEventId;

    public Builder lastEventId(String value) {
      RequestOptions.builder().header("Last-Event-ID", value);
      lastEventId = value;
      return this;
    }

    public SessionThreadEventStreamParams build() {
      return new SessionThreadEventStreamParams(this);
    }
  }
}
