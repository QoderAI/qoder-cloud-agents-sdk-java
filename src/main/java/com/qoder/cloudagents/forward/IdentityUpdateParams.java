package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.Map;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class IdentityUpdateParams extends JsonObject {
  private IdentityUpdateParams(JsonValue value) {
    super(value);
  }

  public static IdentityUpdateParams fromJson(JsonValue value) {
    return new IdentityUpdateParams(value);
  }

  public Boolean autoMemoryStore() {
    return optional("auto_memory_store", JsonValue::asBoolean);
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

    public Builder autoMemoryStore(Boolean value) {
      return put("auto_memory_store", value);
    }

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

    public IdentityUpdateParams build() {

      return new IdentityUpdateParams(json());
    }
  }
}
