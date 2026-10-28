package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DecisionContext extends JsonObject {
  private DecisionContext(JsonValue value) {
    super(value);
  }

  public static DecisionContext fromJson(JsonValue value) {
    return new DecisionContext(value);
  }

  public String mode() {
    return optional("mode", JsonValue::asString);
  }

  public String anchorEventId() {
    return optional("anchor_event_id", JsonValue::asString);
  }

  public String firstEventId() {
    return optional("first_event_id", JsonValue::asString);
  }

  public String lastEventId() {
    return optional("last_event_id", JsonValue::asString);
  }

  public Long includedSegmentCount() {
    return optional("included_segment_count", JsonValue::asLong);
  }

  public Long includedEventCount() {
    return optional("included_event_count", JsonValue::asLong);
  }
}
