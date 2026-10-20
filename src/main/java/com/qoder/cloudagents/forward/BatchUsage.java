package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.math.BigDecimal;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class BatchUsage extends JsonObject {
  private BatchUsage(JsonValue value) {
    super(value);
  }

  public static BatchUsage fromJson(JsonValue value) {
    return new BatchUsage(value);
  }

  public BigDecimal totalCredits() {
    return optional("total_credits", JsonValue::asDecimal);
  }
}
