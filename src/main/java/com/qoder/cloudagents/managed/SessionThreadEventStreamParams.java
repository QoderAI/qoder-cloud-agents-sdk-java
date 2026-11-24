package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Thread SSE selectors preserving repeated deltas; the resume cursor is header-only. */
public final class SessionThreadEventStreamParams {
  private final String lastEventId;
  private final List<String> eventDeltas;

  private SessionThreadEventStreamParams(Builder b) {
    lastEventId = b.lastEventId;
    eventDeltas = Collections.unmodifiableList(new ArrayList<>(b.eventDeltas));
  }

  public static Builder builder() {
    return new Builder();
  }

  public String lastEventId() {
    return lastEventId;
  }

  public List<String> eventDeltas() {
    return eventDeltas;
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

    public SessionThreadEventStreamParams build() {
      return new SessionThreadEventStreamParams(this);
    }
  }
}
