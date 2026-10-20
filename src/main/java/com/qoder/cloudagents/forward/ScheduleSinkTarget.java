package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ScheduleSinkTarget extends JsonObject {
  private ScheduleSinkTarget(JsonValue value) {
    super(value);
  }

  public static ScheduleSinkTarget fromJson(JsonValue value) {
    return new ScheduleSinkTarget(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String externalId() {
    return optional("external_id", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder externalId(String value) {
      return put("external_id", value);
    }

    public ScheduleSinkTarget build() {
      require("type", "external_id");
      return new ScheduleSinkTarget(json());
    }
  }
}
