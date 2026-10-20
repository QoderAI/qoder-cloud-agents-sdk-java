package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.math.BigDecimal;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionUsage extends JsonObject {
  private SessionUsage(JsonValue value) {
    super(value);
  }

  public static SessionUsage fromJson(JsonValue value) {
    return new SessionUsage(value);
  }

  public BigDecimal totalCredits() {
    return optional("total_credits", JsonValue::asDecimal);
  }

  public BigDecimal modelCredits() {
    return optional("model_credits", JsonValue::asDecimal);
  }

  public BigDecimal sandboxRuntimeCredits() {
    return optional("sandbox_runtime_credits", JsonValue::asDecimal);
  }
}
