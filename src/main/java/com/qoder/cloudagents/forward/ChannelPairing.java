package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class ChannelPairing extends JsonModel {
  public ChannelPairing(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String channelId() {
    return optional("channel_id", JsonValue::asString);
  }

  public String scopeType() {
    return optional("scope_type", JsonValue::asString);
  }

  public String scopeExternalId() {
    return optional("scope_external_id", JsonValue::asString);
  }

  public String scopeDisplayName() {
    return optional("scope_display_name", JsonValue::asString);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String pairedAt() {
    return optional("paired_at", JsonValue::asString);
  }
}
