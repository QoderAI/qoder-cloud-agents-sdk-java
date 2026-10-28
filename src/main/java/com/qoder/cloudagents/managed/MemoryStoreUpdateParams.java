package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class MemoryStoreUpdateParams extends JsonObject {
  private MemoryStoreUpdateParams(JsonValue value) {
    super(value);
  }

  public static MemoryStoreUpdateParams fromJson(JsonValue value) {
    return new MemoryStoreUpdateParams(value);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public Map<String, String> metadata() {
    return optionalNullableMap("metadata", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public MemoryStoreUpdateParams build() {

      return new MemoryStoreUpdateParams(json());
    }
  }
}
