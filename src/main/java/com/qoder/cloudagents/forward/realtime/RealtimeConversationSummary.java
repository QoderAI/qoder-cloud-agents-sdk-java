package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeConversationSummary extends JsonObject {
  private RealtimeConversationSummary(JsonValue value) {
    super(value);
  }

  public static RealtimeConversationSummary fromJson(JsonValue value) {
    return new RealtimeConversationSummary(value);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String title() {
    return optional("title", JsonValue::asString);
  }

  public String initializationStatus() {
    return optional("initialization_status", JsonValue::asString);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }
}
