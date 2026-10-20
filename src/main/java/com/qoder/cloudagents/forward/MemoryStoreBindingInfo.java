package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class MemoryStoreBindingInfo extends JsonObject {
  private MemoryStoreBindingInfo(JsonValue value) {
    super(value);
  }

  public static MemoryStoreBindingInfo fromJson(JsonValue value) {
    return new MemoryStoreBindingInfo(value);
  }

  public Long identityTemplateCount() {
    return optional("identity_template_count", JsonValue::asLong);
  }
}
