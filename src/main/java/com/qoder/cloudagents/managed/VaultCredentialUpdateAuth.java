package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class VaultCredentialUpdateAuth extends JsonObject {
  private VaultCredentialUpdateAuth(JsonValue value) {
    super(value);
  }

  public static VaultCredentialUpdateAuth fromJson(JsonValue value) {
    return new VaultCredentialUpdateAuth(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
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

  public String secretValue() {
    return optional("secret_value", JsonValue::asString);
  }

  public VaultCredentialOAuthRefreshUpdate refresh() {
    return optional("refresh", VaultCredentialOAuthRefreshUpdate::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
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

    public Builder secretValue(String value) {
      return put("secret_value", value);
    }

    public Builder refresh(VaultCredentialOAuthRefreshUpdate value) {
      return put("refresh", value);
    }

    public VaultCredentialUpdateAuth build() {
      require("type");
      return new VaultCredentialUpdateAuth(json());
    }
  }
}
