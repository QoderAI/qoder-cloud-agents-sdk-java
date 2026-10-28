package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class VaultCreateParams extends JsonObject {
  private VaultCreateParams(JsonValue value) {
    super(value);
  }

  public static VaultCreateParams fromJson(JsonValue value) {
    return new VaultCreateParams(value);
  }

  public String displayName() {
    return optional("display_name", JsonValue::asString);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder displayName(String value) {
      return put("display_name", value);
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public VaultCreateParams build() {
      require("display_name");
      return new VaultCreateParams(json());
    }
  }
}
