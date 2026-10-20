package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeCloseResult extends JsonObject {
  private RealtimeCloseResult(JsonValue value) {
    super(value);
  }

  public static RealtimeCloseResult fromJson(JsonValue value) {
    return new RealtimeCloseResult(value);
  }

  public String requestId() {
    return optional("request_id", JsonValue::asString);
  }

  public String outcome() {
    return optional("outcome", JsonValue::asString);
  }
}
