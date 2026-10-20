package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ChannelCreateParams extends JsonObject {
  private ChannelCreateParams(JsonValue value) {
    super(value);
  }

  public static ChannelCreateParams fromJson(JsonValue value) {
    return new ChannelCreateParams(value);
  }

  public String channelType() {
    return optional("channel_type", JsonValue::asString);
  }

  public ChannelIdentityResolution identityResolution() {
    return optional("identity_resolution", ChannelIdentityResolution::fromJson);
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

    public Builder channelType(String value) {
      return put("channel_type", value);
    }

    public Builder identityResolution(ChannelIdentityResolution value) {
      return put("identity_resolution", value);
    }

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

    public ChannelCreateParams build() {
      require("channel_type");
      JsonValue resolution = json().get("identity_resolution");
      String mode =
          resolution.isMissing() || resolution.isNull()
              ? "fixed"
              : resolution.get("mode").asString();
      if ("fixed".equals(mode)) require("identity_id", "template_id");
      if ("pairing".equals(mode)
          && (!json().get("identity_id").isMissing() || !json().get("template_id").isMissing()))
        throw new ConfigurationException("Pairing channels must omit identityId and templateId");
      return new ChannelCreateParams(json());
    }
  }
}
