package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class VaultCredential extends JsonModel {
  public VaultCredential(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String vaultId() {
    return optional("vault_id", JsonValue::asString);
  }

  public String displayName() {
    return optional("display_name", JsonValue::asString);
  }

  public String archivedAt() {
    return optional("archived_at", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }

  public VaultCredentialAuth auth() {
    return optional("auth", VaultCredentialAuth::fromJson);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }
}
