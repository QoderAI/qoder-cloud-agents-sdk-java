package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;

/** OAuth refresh read view with resource and endpoint metadata, never refresh tokens or secrets. */
public final class VaultCredentialOAuthRefreshInfo extends JsonObject {
  private VaultCredentialOAuthRefreshInfo(JsonValue value) {
    super(value);
  }

  public static VaultCredentialOAuthRefreshInfo fromJson(JsonValue value) {
    return new VaultCredentialOAuthRefreshInfo(value);
  }

  public String clientId() {
    return optionalString("client_id");
  }

  public String tokenEndpoint() {
    return optionalString("token_endpoint");
  }

  public String resource() {
    return optionalString("resource");
  }

  public String scope() {
    return optionalString("scope");
  }

  public VaultCredentialTokenEndpointAuthInfo tokenEndpointAuth() {
    return optional("token_endpoint_auth", VaultCredentialTokenEndpointAuthInfo::fromJson);
  }
}
