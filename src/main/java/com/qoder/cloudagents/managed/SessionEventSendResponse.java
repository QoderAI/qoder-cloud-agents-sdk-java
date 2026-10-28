package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class SessionEventSendResponse extends JsonModel {
  public SessionEventSendResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public List<SessionEvent> data() {
    return optionalList("data", item -> new SessionEvent(item, requestId()));
  }
}
