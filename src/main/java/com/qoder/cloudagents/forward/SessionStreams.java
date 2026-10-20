package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.ClientEngine;
import com.qoder.cloudagents.errors.ConfigurationException;
import com.qoder.cloudagents.streaming.EventStream;
import java.util.*;

final class SessionStreams {
  private SessionStreams() {}

  static EventStream<SessionEvent> create(
      ClientEngine engine,
      String[] path,
      Map<String, List<String>> query,
      String initialId,
      RequestOptions options,
      boolean resumable,
      boolean thread) {
    if (options == null) throw new ConfigurationException("Request options must not be null");
    for (String segment : path)
      if (segment == null || segment.isEmpty() || segment.equals(".") || segment.equals(".."))
        throw new ConfigurationException("Invalid API path segment");
    if (thread && !options.query().isEmpty())
      throw new ConfigurationException("Thread streams do not accept query selectors");
    String cursor = initialId == null ? engine.defaultHeader("Last-Event-ID") : initialId;
    for (Map.Entry<String, String> header : options.headers().entrySet())
      if (header.getKey().equalsIgnoreCase("Last-Event-ID")) cursor = header.getValue();
    return new EventStream<>(
        engine,
        path,
        query,
        options,
        cursor,
        SessionEvent::new,
        event ->
            "session.status_terminated".equals(event.type())
                || "session.deleted".equals(event.type()),
        resumable);
  }
}
