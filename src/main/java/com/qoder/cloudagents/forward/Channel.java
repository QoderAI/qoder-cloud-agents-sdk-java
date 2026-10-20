package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class Channel extends JsonModel {
  public Channel(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String channelType() {
    return optional("channel_type", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String bindingStatus() {
    return optional("binding_status", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public ChannelIdentityResolution identityResolution() {
    return optional("identity_resolution", ChannelIdentityResolution::fromJson);
  }

  public ChannelConfig channelConfig() {
    return optional("channel_config", ChannelConfig::fromJson);
  }
}
