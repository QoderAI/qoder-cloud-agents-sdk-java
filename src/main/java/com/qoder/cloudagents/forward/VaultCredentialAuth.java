package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class VaultCredentialAuth extends JsonObject {
  private VaultCredentialAuth(JsonValue value) {
    super(value);
  }

  public static VaultCredentialAuth fromJson(JsonValue value) {
    return new VaultCredentialAuth(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String mcpServerUrl() {
    return optional("mcp_server_url", JsonValue::asString);
  }

  public String secretName() {
    return optional("secret_name", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder mcpServerUrl(String value) {
      return put("mcp_server_url", value);
    }

    public Builder secretName(String value) {
      return put("secret_name", value);
    }

    public VaultCredentialAuth build() {
      return new VaultCredentialAuth(json());
    }
  }
}
