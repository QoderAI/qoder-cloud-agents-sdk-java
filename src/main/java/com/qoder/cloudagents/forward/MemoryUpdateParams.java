package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.Map;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class MemoryUpdateParams extends JsonObject {
  private MemoryUpdateParams(JsonValue value) {
    super(value);
  }

  public static MemoryUpdateParams fromJson(JsonValue value) {
    return new MemoryUpdateParams(value);
  }

  public String content() {
    return optional("content", JsonValue::asString);
  }

  public String contentSha256() {
    return optional("content_sha256", JsonValue::asString);
  }

  public Map<String, String> metadata() {
    return optionalMap("metadata", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder content(String value) {
      return put("content", value);
    }

    public Builder contentSha256(String value) {
      return put("content_sha256", value);
    }

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public MemoryUpdateParams build() {

      if (!json().get("content").isString())
        throw new ConfigurationException("content must be a non-null string (empty is allowed)");
      return new MemoryUpdateParams(json());
    }
  }
}
