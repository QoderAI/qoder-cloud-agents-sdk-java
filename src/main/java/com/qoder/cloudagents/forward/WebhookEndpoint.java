package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable webhook response preserving unknown fields. */
public final class WebhookEndpoint extends JsonModel {
  public WebhookEndpoint(JsonValue value, String requestId) {
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

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }

  public String lastSuccessAt() {
    return optional("last_success_at", JsonValue::asString);
  }

  public String lastFailureAt() {
    return optional("last_failure_at", JsonValue::asString);
  }

  public String disabledReason() {
    return optional("disabled_reason", JsonValue::asString);
  }

  public Long consecutiveFail() {
    return optional("consecutive_fail", JsonValue::asLong);
  }
}
