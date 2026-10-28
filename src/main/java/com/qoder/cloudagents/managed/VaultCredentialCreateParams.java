package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class VaultCredentialCreateParams extends JsonObject {
  private VaultCredentialCreateParams(JsonValue value) {
    super(value);
  }

  public static VaultCredentialCreateParams fromJson(JsonValue value) {
    return new VaultCredentialCreateParams(value);
  }

  public VaultCredentialCreateAuth auth() {
    return optional("auth", VaultCredentialCreateAuth::fromJson);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public String displayName() {
    return optional("display_name", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder auth(VaultCredentialCreateAuth value) {
      return put("auth", value);
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public Builder displayName(String value) {
      return put("display_name", value);
    }

    public VaultCredentialCreateParams build() {
      require("auth");
      return new VaultCredentialCreateParams(json());
    }
  }
}
