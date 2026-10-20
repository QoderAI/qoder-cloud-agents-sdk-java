package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ChannelUpdateParams extends JsonObject {
  private ChannelUpdateParams(JsonValue value) {
    super(value);
  }

  public static ChannelUpdateParams fromJson(JsonValue value) {
    return new ChannelUpdateParams(value);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public ChannelConfigParams channelConfig() {
    return optional("channel_config", ChannelConfigParams::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder identityId(String value) {
      return put("identity_id", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public Builder channelConfig(ChannelConfigParams value) {
      return put("channel_config", value);
    }

    public ChannelUpdateParams build() {
      return new ChannelUpdateParams(json());
    }
  }
}
