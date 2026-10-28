package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionEventParam extends JsonObject {
  private SessionEventParam(JsonValue value) {
    super(value);
  }

  public static SessionEventParam fromJson(JsonValue value) {
    return new SessionEventParam(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public EventContent content() {
    return optional("content", EventContent::fromJson);
  }

  public String toolUseId() {
    return optional("tool_use_id", JsonValue::asString);
  }

  public String customToolUseId() {
    return optional("custom_tool_use_id", JsonValue::asString);
  }

  public String result() {
    return optional("result", JsonValue::asString);
  }

  public String denyMessage() {
    return optional("deny_message", JsonValue::asString);
  }

  public Boolean isError() {
    return optional("is_error", JsonValue::asBoolean);
  }

  public String reason() {
    return optional("reason", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public OutcomeRubric rubric() {
    return optional("rubric", OutcomeRubric::fromJson);
  }

  public String outcomeId() {
    return optional("outcome_id", JsonValue::asString);
  }

  public Long maxIterations() {
    return optional("max_iterations", JsonValue::asLong);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder content(EventContent value) {
      return put("content", value);
    }

    public Builder toolUseId(String value) {
      return put("tool_use_id", value);
    }

    public Builder customToolUseId(String value) {
      return put("custom_tool_use_id", value);
    }

    public Builder result(String value) {
      return put("result", value);
    }

    public Builder denyMessage(String value) {
      return put("deny_message", value);
    }

    public Builder isError(Boolean value) {
      return put("is_error", value);
    }

    public Builder reason(String value) {
      return put("reason", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public Builder rubric(OutcomeRubric value) {
      return put("rubric", value);
    }

    public Builder outcomeId(String value) {
      return put("outcome_id", value);
    }

    public Builder maxIterations(Long value) {
      return put("max_iterations", value);
    }

    public SessionEventParam build() {
      require("type");
      return new SessionEventParam(json());
    }
  }
}
