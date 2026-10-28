package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class IdentityMemoryStoreMountParams extends JsonObject {
  private IdentityMemoryStoreMountParams(JsonValue value) {
    super(value);
  }

  public static IdentityMemoryStoreMountParams fromJson(JsonValue value) {
    return new IdentityMemoryStoreMountParams(value);
  }

  public String access() {
    return optional("access", JsonValue::asString);
  }

  public String memoryStoreId() {
    return optional("memory_store_id", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder access(String value) {
      return put("access", value);
    }

    public Builder memoryStoreId(String value) {
      return put("memory_store_id", value);
    }

    public IdentityMemoryStoreMountParams build() {
      require("memory_store_id");
      return new IdentityMemoryStoreMountParams(json());
    }
  }
}
