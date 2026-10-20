package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class BatchFile extends JsonModel {
  public BatchFile(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String url() {
    return optional("url", JsonValue::asString);
  }

  public String expiresAt() {
    return optional("expires_at", JsonValue::asString);
  }
}
