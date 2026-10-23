package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable webhook response preserving unknown fields. */
public final class WebhookEndpointTestResponse extends JsonModel {
  public WebhookEndpointTestResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public Long eventId() {
    return optional("event_id", JsonValue::asLong);
  }

  public Long deliveryRows() {
    return optional("delivery_rows", JsonValue::asLong);
  }
}
