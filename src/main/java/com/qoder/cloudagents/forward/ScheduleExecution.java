package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ScheduleExecution extends JsonObject {
  private ScheduleExecution(JsonValue value) {
    super(value);
  }

  public static ScheduleExecution fromJson(JsonValue value) {
    return new ScheduleExecution(value);
  }

  public String sessionMode() {
    return optional("session_mode", JsonValue::asString);
  }

  public Long maxConcurrentRuns() {
    return optional("max_concurrent_runs", JsonValue::asLong);
  }

  public Long maxAttempts() {
    return optional("max_attempts", JsonValue::asLong);
  }

  public Long timeoutMs() {
    return optional("timeout_ms", JsonValue::asLong);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder sessionMode(String value) {
      return put("session_mode", value);
    }

    public Builder maxConcurrentRuns(Long value) {
      return put("max_concurrent_runs", value);
    }

    public Builder maxAttempts(Long value) {
      return put("max_attempts", value);
    }

    public Builder timeoutMs(Long value) {
      return put("timeout_ms", value);
    }

    public ScheduleExecution build() {
      if (!json().get("max_attempts").isMissing()
          && (json().get("max_attempts").isNull()
              || json().get("max_attempts").asLong() < 1
              || json().get("max_attempts").asLong() > 2))
        throw new ConfigurationException("maxAttempts must be 1 or 2");
      return new ScheduleExecution(json());
    }
  }
}
