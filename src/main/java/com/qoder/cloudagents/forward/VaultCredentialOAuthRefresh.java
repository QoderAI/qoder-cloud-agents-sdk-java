package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class VaultCredentialOAuthRefresh extends JsonObject {
  private VaultCredentialOAuthRefresh(JsonValue value) {
    super(value);
  }

  public static VaultCredentialOAuthRefresh fromJson(JsonValue value) {
    return new VaultCredentialOAuthRefresh(value);
  }

  public String refreshToken() {
    return optional("refresh_token", JsonValue::asString);
  }

  public String clientId() {
    return optional("client_id", JsonValue::asString);
  }

  public String tokenEndpoint() {
    return optional("token_endpoint", JsonValue::asString);
  }

  public String scope() {
    return optional("scope", JsonValue::asString);
  }

  public JsonValue tokenEndpointAuth() {
    return field("token_endpoint_auth");
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder refreshToken(String value) {
      return put("refresh_token", value);
    }

    public Builder clientId(String value) {
      return put("client_id", value);
    }

    public Builder tokenEndpoint(String value) {
      return put("token_endpoint", value);
    }

    public Builder scope(String value) {
      return put("scope", value);
    }

    public Builder tokenEndpointAuth(JsonValue value) {
      return put("token_endpoint_auth", value);
    }

    public VaultCredentialOAuthRefresh build() {
      return new VaultCredentialOAuthRefresh(json());
    }
  }
}
