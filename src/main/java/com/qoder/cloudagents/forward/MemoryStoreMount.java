package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class MemoryStoreMount extends JsonModel {
  public MemoryStoreMount(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String memoryStoreId() {
    return optional("memory_store_id", JsonValue::asString);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String access() {
    return optional("access", JsonValue::asString);
  }

  public Boolean systemManaged() {
    return optional("system_managed", JsonValue::asBoolean);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public Long entryCount() {
    return optional("entry_count", JsonValue::asLong);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }
}
