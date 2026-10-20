package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.Map;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class IdentityCreateParams extends JsonObject {
  private IdentityCreateParams(JsonValue value) {
    super(value);
  }

  public static IdentityCreateParams fromJson(JsonValue value) {
    return new IdentityCreateParams(value);
  }

  public String externalId() {
    return optional("external_id", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", value -> value);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder externalId(String value) {
      return put("external_id", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public IdentityCreateParams build() {
      require("external_id");
      return new IdentityCreateParams(json());
    }
  }
}
