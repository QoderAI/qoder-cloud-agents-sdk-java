package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeTranscript extends JsonObject {
  private RealtimeTranscript(JsonValue value) {
    super(value);
  }

  public static RealtimeTranscript fromJson(JsonValue value) {
    return new RealtimeTranscript(value);
  }

  public String role() {
    return optional("role", JsonValue::asString);
  }

  public String text() {
    return optional("text", JsonValue::asString);
  }

  public String itemId() {
    return optional("item_id", JsonValue::asString);
  }

  public String responseId() {
    return optional("response_id", JsonValue::asString);
  }
}
