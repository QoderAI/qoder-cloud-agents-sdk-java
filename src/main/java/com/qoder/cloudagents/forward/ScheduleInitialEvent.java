package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ScheduleInitialEvent extends JsonObject {
  private ScheduleInitialEvent(JsonValue value) {
    super(value);
  }

  public static ScheduleInitialEvent fromJson(JsonValue value) {
    return new ScheduleInitialEvent(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String content() {
    return optional("content", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder content(String value) {
      return put("content", value);
    }

    public ScheduleInitialEvent build() {
      require("type", "content");
      return new ScheduleInitialEvent(json());
    }
  }
}
