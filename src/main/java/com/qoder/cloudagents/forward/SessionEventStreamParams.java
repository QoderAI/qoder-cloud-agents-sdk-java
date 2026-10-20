package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable SSE subscription parameters. The cursor is sent as a header, never as a query. */
public final class SessionEventStreamParams {
  private final String lastEventId;
  private final List<String> eventDeltas;
  private final Boolean includeToolCalls, includeThinking;

  private SessionEventStreamParams(Builder b) {
    lastEventId = b.lastEventId;
    eventDeltas = Collections.unmodifiableList(new ArrayList<>(b.eventDeltas));
    includeToolCalls = b.includeToolCalls;
    includeThinking = b.includeThinking;
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
    if (includeToolCalls != null)
      values.put("include_tool_calls", Collections.singletonList(includeToolCalls.toString()));
    if (includeThinking != null)
      values.put("include_thinking", Collections.singletonList(includeThinking.toString()));
    return values;
  }

  public static final class Builder {
    private String lastEventId;
    private List<String> eventDeltas = Collections.emptyList();
    private Boolean includeToolCalls, includeThinking;

    public Builder eventDeltas(List<String> values) {
      if (values == null) throw new ConfigurationException("eventDeltas must not be null");
      for (String value : values)
        if (value == null || value.isEmpty())
          throw new ConfigurationException("eventDeltas values must not be empty");
      eventDeltas = new ArrayList<>(values);
      return this;
    }

    public Builder includeToolCalls(boolean value) {
      includeToolCalls = value;
      return this;
    }

    public Builder includeThinking(boolean value) {
      includeThinking = value;
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
