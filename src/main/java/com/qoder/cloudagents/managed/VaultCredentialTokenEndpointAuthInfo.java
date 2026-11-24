package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;

/** Token endpoint authentication read view exposing the method type, not the client secret. */
public final class VaultCredentialTokenEndpointAuthInfo extends JsonObject {
  private VaultCredentialTokenEndpointAuthInfo(JsonValue value) {
    super(value);
  }

  public static VaultCredentialTokenEndpointAuthInfo fromJson(JsonValue value) {
    return new VaultCredentialTokenEndpointAuthInfo(value);
  }

  public String type() {
    return optionalString("type");
  }
}
