package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class BatchRequestCounts extends JsonObject {
  private BatchRequestCounts(JsonValue value) {
    super(value);
  }

  public static BatchRequestCounts fromJson(JsonValue value) {
    return new BatchRequestCounts(value);
  }

  public Long total() {
    return optional("total", JsonValue::asLong);
  }

  public Long pending() {
    return optional("pending", JsonValue::asLong);
  }

  public Long running() {
    return optional("running", JsonValue::asLong);
  }

  public Long completed() {
    return optional("completed", JsonValue::asLong);
  }

  public Long failed() {
    return optional("failed", JsonValue::asLong);
  }

  public Long cancelled() {
    return optional("cancelled", JsonValue::asLong);
  }

  public Long expired() {
    return optional("expired", JsonValue::asLong);
  }
}
