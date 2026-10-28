package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class VaultCredentialValidation extends JsonModel {
  public VaultCredentialValidation(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static VaultCredentialValidation fromJson(JsonValue value) {
    return new VaultCredentialValidation(value, null);
  }

  public String credentialId() {
    return optional("credential_id", JsonValue::asString);
  }

  public String vaultId() {
    return optional("vault_id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String validatedAt() {
    return optional("validated_at", JsonValue::asString);
  }

  public Boolean hasRefreshToken() {
    return optional("has_refresh_token", JsonValue::asBoolean);
  }

  public CredentialRefreshResult refresh() {
    return optional("refresh", CredentialRefreshResult::fromJson);
  }

  public CredentialProbeResult mcpProbe() {
    return optional("mcp_probe", CredentialProbeResult::fromJson);
  }
}
