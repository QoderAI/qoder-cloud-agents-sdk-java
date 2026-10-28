package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class WorkData extends JsonObject {
  private WorkData(JsonValue value) {
    super(value);
  }

  public static WorkData fromJson(JsonValue value) {
    return new WorkData(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder id(String value) {
      return put("id", value);
    }

    public WorkData build() {

      return new WorkData(json());
    }
  }
}
