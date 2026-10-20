package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class OutcomeEvaluation extends JsonObject {
  private OutcomeEvaluation(JsonValue value) {
    super(value);
  }

  public static OutcomeEvaluation fromJson(JsonValue value) {
    return new OutcomeEvaluation(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String outcomeId() {
    return optional("outcome_id", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public Long iteration() {
    return optional("iteration", JsonValue::asLong);
  }

  public String result() {
    return optional("result", JsonValue::asString);
  }

  public String explanation() {
    return optional("explanation", JsonValue::asString);
  }

  public String completedAt() {
    return optional("completed_at", JsonValue::asString);
  }
}
