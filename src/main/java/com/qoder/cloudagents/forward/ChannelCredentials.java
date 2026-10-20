package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ChannelCredentials extends JsonObject {
  private ChannelCredentials(JsonValue value) {
    super(value);
  }

  public static ChannelCredentials fromJson(JsonValue value) {
    return new ChannelCredentials(value);
  }

  public String appId() {
    return optional("app_id", JsonValue::asString);
  }

  public String appSecret() {
    return optional("app_secret", JsonValue::asString);
  }

  public String clientId() {
    return optional("client_id", JsonValue::asString);
  }

  public String clientSecret() {
    return optional("client_secret", JsonValue::asString);
  }

  public String botId() {
    return optional("bot_id", JsonValue::asString);
  }

  public String secret() {
    return optional("secret", JsonValue::asString);
  }

  public String tenantId() {
    return optional("tenant_id", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder appId(String value) {
      return put("app_id", value);
    }

    public Builder appSecret(String value) {
      return put("app_secret", value);
    }

    public Builder clientId(String value) {
      return put("client_id", value);
    }

    public Builder clientSecret(String value) {
      return put("client_secret", value);
    }

    public Builder botId(String value) {
      return put("bot_id", value);
    }

    public Builder secret(String value) {
      return put("secret", value);
    }

    public Builder tenantId(String value) {
      return put("tenant_id", value);
    }

    public ChannelCredentials build() {
      return new ChannelCredentials(json());
    }
  }
}
