package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.math.BigDecimal;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DecisionAnswer extends JsonObject {
  private DecisionAnswer(JsonValue value) {
    super(value);
  }

  public static DecisionAnswer fromJson(JsonValue value) {
    return new DecisionAnswer(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public JsonValue value() {
    return field("value");
  }

  public BigDecimal score() {
    return optional("score", JsonValue::asDecimal);
  }
}
