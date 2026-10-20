package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeCapabilities extends JsonObject {
  private RealtimeCapabilities(JsonValue value) {
    super(value);
  }

  public static RealtimeCapabilities fromJson(JsonValue value) {
    return new RealtimeCapabilities(value);
  }

  public Boolean gracefulClose() {
    return optional("graceful_close", JsonValue::asBoolean);
  }
}
