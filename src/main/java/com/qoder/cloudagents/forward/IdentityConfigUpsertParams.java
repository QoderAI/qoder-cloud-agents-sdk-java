package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.Map;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class IdentityConfigUpsertParams extends JsonObject {
  private IdentityConfigUpsertParams(JsonValue value) {
    super(value);
  }

  public static IdentityConfigUpsertParams fromJson(JsonValue value) {
    return new IdentityConfigUpsertParams(value);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public IdentityConfigSpec identityConfig() {
    return optional("identity_config", IdentityConfigSpec::fromJson);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", value -> value);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder identityConfig(IdentityConfigSpec value) {
      return put("identity_config", value);
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public IdentityConfigUpsertParams build() {
      require("identity_config");
      return new IdentityConfigUpsertParams(json());
    }
  }
}
