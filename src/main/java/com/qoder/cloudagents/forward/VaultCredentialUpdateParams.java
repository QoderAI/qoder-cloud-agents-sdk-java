package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class VaultCredentialUpdateParams extends JsonObject {
  private VaultCredentialUpdateParams(JsonValue value) {
    super(value);
  }

  public static VaultCredentialUpdateParams fromJson(JsonValue value) {
    return new VaultCredentialUpdateParams(value);
  }

  public VaultCredentialUpdateAuth auth() {
    return optional("auth", VaultCredentialUpdateAuth::fromJson);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder auth(VaultCredentialUpdateAuth value) {
      return put("auth", value);
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public VaultCredentialUpdateParams build() {
      if (json().get("auth").isMissing() && json().get("metadata").isMissing())
        throw new ConfigurationException("Supply auth or metadata");
      if (json().get("auth").isNull()) throw new ConfigurationException("auth cannot be null");
      return new VaultCredentialUpdateParams(json());
    }
  }
}
