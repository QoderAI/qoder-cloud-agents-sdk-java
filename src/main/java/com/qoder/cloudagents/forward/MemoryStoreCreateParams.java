package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.Map;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class MemoryStoreCreateParams extends JsonObject {
  private MemoryStoreCreateParams(JsonValue value) {
    super(value);
  }

  public static MemoryStoreCreateParams fromJson(JsonValue value) {
    return new MemoryStoreCreateParams(value);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public Map<String, String> metadata() {
    return optionalMap("metadata", JsonValue::asString);
  }

  public MemoryStoreCreateMount mount() {
    return optional("mount", MemoryStoreCreateMount::fromJson);
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

    public Builder mount(MemoryStoreCreateMount value) {
      return put("mount", value);
    }

    public MemoryStoreCreateParams build() {
      require("name");

      return new MemoryStoreCreateParams(json());
    }
  }
}
