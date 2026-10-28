package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class WorkHeartbeat extends JsonModel {
  public WorkHeartbeat(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static WorkHeartbeat fromJson(JsonValue value) {
    return new WorkHeartbeat(value, null);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String lastHeartbeat() {
    return optional("last_heartbeat", JsonValue::asString);
  }

  public Boolean leaseExtended() {
    return optional("lease_extended", JsonValue::asBoolean);
  }

  public String state() {
    return optional("state", JsonValue::asString);
  }

  public Long ttlSeconds() {
    return optional("ttl_seconds", JsonValue::asLong);
  }
}
