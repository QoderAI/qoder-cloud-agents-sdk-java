package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ScheduleSink extends JsonObject {
  private ScheduleSink(JsonValue value) {
    super(value);
  }

  public static ScheduleSink fromJson(JsonValue value) {
    return new ScheduleSink(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String channelId() {
    return optional("channel_id", JsonValue::asString);
  }

  public String channelPairingId() {
    return optional("channel_pairing_id", JsonValue::asString);
  }

  public ScheduleSinkTarget target() {
    return optional("target", ScheduleSinkTarget::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder channelId(String value) {
      return put("channel_id", value);
    }

    public Builder channelPairingId(String value) {
      return put("channel_pairing_id", value);
    }

    public Builder target(ScheduleSinkTarget value) {
      return put("target", value);
    }

    public ScheduleSink build() {
      require("type");
      return new ScheduleSink(json());
    }
  }
}
