package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class OAuthStartParams extends JsonObject {
  private OAuthStartParams(JsonValue value) {
    super(value);
  }

  public static OAuthStartParams fromJson(JsonValue value) {
    return new OAuthStartParams(value);
  }

  public String vaultId() {
    return optional("vault_id", JsonValue::asString);
  }

  public String mcpServerUrl() {
    return optional("mcp_server_url", JsonValue::asString);
  }

  public String clientId() {
    return optional("client_id", JsonValue::asString);
  }

  public String clientSecret() {
    return optional("client_secret", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder vaultId(String value) {
      return put("vault_id", value);
    }

    public Builder mcpServerUrl(String value) {
      return put("mcp_server_url", value);
    }

    public Builder clientId(String value) {
      return put("client_id", value);
    }

    public Builder clientSecret(String value) {
      return put("client_secret", value);
    }

    public OAuthStartParams build() {
      require("vault_id", "mcp_server_url");

      return new OAuthStartParams(json());
    }
  }
}
