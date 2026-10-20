package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ChannelIdentityResolution extends JsonObject {
  private ChannelIdentityResolution(JsonValue value) {
    super(value);
  }

  public static ChannelIdentityResolution fromJson(JsonValue value) {
    return new ChannelIdentityResolution(value);
  }

  public String mode() {
    return optional("mode", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder mode(String value) {
      return put("mode", value);
    }

    public ChannelIdentityResolution build() {
      require("mode");
      return new ChannelIdentityResolution(json());
    }
  }
}
