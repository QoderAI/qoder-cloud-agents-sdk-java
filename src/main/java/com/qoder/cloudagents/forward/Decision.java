package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class Decision extends JsonModel {
  public Decision(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String sessionId() {
    return optional("session_id", JsonValue::asString);
  }

  public String source() {
    return optional("source", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public List<DecisionQuestion> questions() {
    return optionalList("questions", DecisionQuestion::fromJson);
  }

  public List<DecisionAnswer> answers() {
    return optionalList("answers", DecisionAnswer::fromJson);
  }

  public DecisionContext context() {
    return optional("context", DecisionContext::fromJson);
  }

  public DecisionError error() {
    return optional("error", DecisionError::fromJson);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String completedAt() {
    return optional("completed_at", JsonValue::asString);
  }
}
