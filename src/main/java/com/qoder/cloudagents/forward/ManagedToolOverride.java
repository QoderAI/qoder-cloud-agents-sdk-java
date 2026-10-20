package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class ManagedToolOverride extends JsonObject {
  private ManagedToolOverride(JsonValue value) {
    super(value);
  }

  public static ManagedToolOverride fromJson(JsonValue value) {
    return new ManagedToolOverride(value);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public ManagedToolOverride build() {
      require("enabled");
      return new ManagedToolOverride(json());
    }
  }
}
