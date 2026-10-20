package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class SystemOverride extends JsonObject {
  private SystemOverride(JsonValue value) {
    super(value);
  }

  public static SystemOverride fromJson(JsonValue value) {
    return new SystemOverride(value);
  }

  public String mode() {
    return optional("mode", JsonValue::asString);
  }

  public String content() {
    return optional("content", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder mode(String value) {
      return put("mode", value);
    }

    public Builder content(String value) {
      return put("content", value);
    }

    public SystemOverride build() {

      return new SystemOverride(json());
    }
  }
}
