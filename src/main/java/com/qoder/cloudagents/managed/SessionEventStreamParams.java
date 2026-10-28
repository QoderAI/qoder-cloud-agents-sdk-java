package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable SSE subscription parameters. The cursor is sent as a header, never as a query. */
public final class SessionEventStreamParams {
  private final String lastEventId;
  private final List<String> eventDeltas;

  private SessionEventStreamParams(Builder b) {
    lastEventId = b.lastEventId;
    eventDeltas = Collections.unmodifiableList(new ArrayList<>(b.eventDeltas));
  }

  public static Builder builder() {
    return new Builder();
  }

  public String lastEventId() {
    return lastEventId;
  }

  Map<String, List<String>> query() {
    Map<String, List<String>> values = new LinkedHashMap<>();
    if (!eventDeltas.isEmpty()) values.put("event_deltas[]", eventDeltas);
    return values;
  }

  public static final class Builder {
    private String lastEventId;
    private List<String> eventDeltas = Collections.emptyList();

    public Builder eventDeltas(List<String> values) {
      if (values == null) throw new ConfigurationException("eventDeltas must not be null");
      for (String value : values)
        if (value == null || value.isEmpty())
          throw new ConfigurationException("eventDeltas values must not be empty");
      eventDeltas = new ArrayList<>(values);
      return this;
    }

    public Builder lastEventId(String value) {
      RequestOptions.builder().header("Last-Event-ID", value);
      lastEventId = value;
      return this;
    }

    public SessionEventStreamParams build() {
      return new SessionEventStreamParams(this);
    }
  }
}
