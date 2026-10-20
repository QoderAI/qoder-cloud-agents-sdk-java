package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class IdentityMemoryStoreListResponse extends JsonModel {
  public IdentityMemoryStoreListResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public List<MemoryStoreMount> data() {
    return optionalList("data", item -> new MemoryStoreMount(item, requestId()));
  }

  public Boolean hasMore() {
    return optional("has_more", JsonValue::asBoolean);
  }

  public String firstId() {
    return optional("first_id", JsonValue::asString);
  }

  public String lastId() {
    return optional("last_id", JsonValue::asString);
  }
}
