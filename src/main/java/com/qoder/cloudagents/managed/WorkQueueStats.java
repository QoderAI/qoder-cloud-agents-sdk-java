package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class WorkQueueStats extends JsonModel {
  public WorkQueueStats(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static WorkQueueStats fromJson(JsonValue value) {
    return new WorkQueueStats(value, null);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public Long depth() {
    return optional("depth", JsonValue::asLong);
  }

  public Long pending() {
    return optional("pending", JsonValue::asLong);
  }

  public String oldestQueuedAt() {
    return optional("oldest_queued_at", JsonValue::asString);
  }

  public Long workersPolling() {
    return optional("workers_polling", JsonValue::asLong);
  }
}
