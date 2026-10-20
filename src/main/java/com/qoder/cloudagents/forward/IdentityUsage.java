package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class IdentityUsage extends JsonModel {
  public IdentityUsage(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String type() {
    return optionalString("type");
  }

  public Long sessionCount() {
    return optionalLong("session_count");
  }

  public java.math.BigDecimal activeSeconds() {
    return optional("active_seconds", JsonValue::asDecimal);
  }

  public java.math.BigDecimal credits() {
    return optional("credits", JsonValue::asDecimal);
  }

  public String identityId() {
    return optionalString("identity_id");
  }
}
