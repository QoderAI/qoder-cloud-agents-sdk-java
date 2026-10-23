package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable webhook response preserving unknown fields. */
public final class WebhookEndpointCreateResponse extends JsonModel {
  public WebhookEndpointCreateResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String url() {
    return optional("url", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public List<String> events() {
    return optionalList("events", JsonValue::asString);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public Boolean active() {
    return optional("active", JsonValue::asBoolean);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  /** Returned only at creation. Store securely; do not log. */
  public String signingSecret() {
    return optional("signing_secret", JsonValue::asString);
  }
}
