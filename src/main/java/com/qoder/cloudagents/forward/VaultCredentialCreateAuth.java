package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class VaultCredentialCreateAuth extends JsonObject {
  private VaultCredentialCreateAuth(JsonValue value) {
    super(value);
  }

  public static VaultCredentialCreateAuth fromJson(JsonValue value) {
    return new VaultCredentialCreateAuth(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String mcpServerUrl() {
    return optional("mcp_server_url", JsonValue::asString);
  }

  public String token() {
    return optional("token", JsonValue::asString);
  }

  public String accessToken() {
    return optional("access_token", JsonValue::asString);
  }

  public String expiresAt() {
    return optional("expires_at", JsonValue::asString);
  }

  public String secretName() {
    return optional("secret_name", JsonValue::asString);
  }

  public String secretValue() {
    return optional("secret_value", JsonValue::asString);
  }

  public VaultCredentialOAuthRefresh refresh() {
    return optional("refresh", VaultCredentialOAuthRefresh::fromJson);
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

    public Builder token(String value) {
      return put("token", value);
    }

    public Builder accessToken(String value) {
      return put("access_token", value);
    }

    public Builder expiresAt(String value) {
      return put("expires_at", value);
    }

    public Builder secretName(String value) {
      return put("secret_name", value);
    }

    public Builder secretValue(String value) {
      return put("secret_value", value);
    }

    public Builder refresh(VaultCredentialOAuthRefresh value) {
      return put("refresh", value);
    }

    public VaultCredentialCreateAuth build() {
      require("type");
      return new VaultCredentialCreateAuth(json());
    }
  }
}
