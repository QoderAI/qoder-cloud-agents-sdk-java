package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class ScheduleTriggerPolicy extends JsonObject {
  private ScheduleTriggerPolicy(JsonValue value) {
    super(value);
  }

  public static ScheduleTriggerPolicy fromJson(JsonValue value) {
    return new ScheduleTriggerPolicy(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String expression() {
    return optional("expression", JsonValue::asString);
  }

  public String timezone() {
    return optional("timezone", JsonValue::asString);
  }

  public String startAt() {
    return optional("start_at", JsonValue::asString);
  }

  public String stopAt() {
    return optional("stop_at", JsonValue::asString);
  }

  public String lastRunAt() {
    return optional("last_run_at", JsonValue::asString);
  }

  public List<String> upcomingRunsAt() {
    return optionalList("upcoming_runs_at", JsonValue::asString);
  }
}
