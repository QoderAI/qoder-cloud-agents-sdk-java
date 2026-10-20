package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class VaultCredentialOAuthRefreshUpdate extends JsonObject {
  private VaultCredentialOAuthRefreshUpdate(JsonValue value) {
    super(value);
  }

  public static VaultCredentialOAuthRefreshUpdate fromJson(JsonValue value) {
    return new VaultCredentialOAuthRefreshUpdate(value);
  }

  public String refreshToken() {
    return optional("refresh_token", JsonValue::asString);
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

    public Builder scope(String value) {
      return put("scope", value);
    }

    public Builder tokenEndpointAuth(JsonValue value) {
      return put("token_endpoint_auth", value);
    }

    public VaultCredentialOAuthRefreshUpdate build() {
      return new VaultCredentialOAuthRefreshUpdate(json());
    }
  }
}
