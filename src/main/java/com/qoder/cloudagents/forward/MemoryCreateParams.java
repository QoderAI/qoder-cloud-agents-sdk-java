package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.Map;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class MemoryCreateParams extends JsonObject {
  private MemoryCreateParams(JsonValue value) {
    super(value);
  }

  public static MemoryCreateParams fromJson(JsonValue value) {
    return new MemoryCreateParams(value);
  }

  public String path() {
    return optional("path", JsonValue::asString);
  }

  public String content() {
    return optional("content", JsonValue::asString);
  }

  public Map<String, String> metadata() {
    return optionalMap("metadata", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder path(String value) {
      return put("path", value);
    }

    public Builder content(String value) {
      return put("content", value);
    }

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public MemoryCreateParams build() {
      require("path");
      if (!json().get("content").isString())
        throw new ConfigurationException("content must be a non-null string (empty is allowed)");
      return new MemoryCreateParams(json());
    }
  }
}
