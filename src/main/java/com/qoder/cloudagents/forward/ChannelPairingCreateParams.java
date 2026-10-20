package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ChannelPairingCreateParams extends JsonObject {
  private ChannelPairingCreateParams(JsonValue value) {
    super(value);
  }

  public static ChannelPairingCreateParams fromJson(JsonValue value) {
    return new ChannelPairingCreateParams(value);
  }

  public String code() {
    return optional("code", JsonValue::asString);
  }

  public String channelPairingId() {
    return optional("channel_pairing_id", JsonValue::asString);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder code(String value) {
      return put("code", value);
    }

    public Builder channelPairingId(String value) {
      return put("channel_pairing_id", value);
    }

    public Builder identityId(String value) {
      return put("identity_id", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public ChannelPairingCreateParams build() {
      require("identity_id", "template_id");
      boolean code = !json().get("code").isMissing();
      boolean id = !json().get("channel_pairing_id").isMissing();
      if (code == id)
        throw new ConfigurationException("Supply exactly one of code or channelPairingId");
      String selector = code ? "code" : "channel_pairing_id";
      require(selector);
      if (json().get(selector).asString().trim().isEmpty())
        throw new ConfigurationException("Pairing selector must not be blank");
      return new ChannelPairingCreateParams(json());
    }
  }
}
