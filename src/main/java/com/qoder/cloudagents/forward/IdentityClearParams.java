package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class IdentityClearParams extends JsonObject {
  private IdentityClearParams(JsonValue value) {
    super(value);
  }

  public static IdentityClearParams fromJson(JsonValue value) {
    return new IdentityClearParams(value);
  }

  public String reason() {
    return optional("reason", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder reason(String value) {
      return put("reason", value);
    }

    public IdentityClearParams build() {

      return new IdentityClearParams(json());
    }
  }
}
