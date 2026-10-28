package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class EnvironmentStorageCredential extends JsonObject {
  private EnvironmentStorageCredential(JsonValue value) {
    super(value);
  }

  public static EnvironmentStorageCredential fromJson(JsonValue value) {
    return new EnvironmentStorageCredential(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String roleArn() {
    return optional("role_arn", JsonValue::asString);
  }

  public String accessKeyId() {
    return optional("access_key_id", JsonValue::asString);
  }

  public String accessKeySecret() {
    return optional("access_key_secret", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder roleArn(String value) {
      return put("role_arn", value);
    }

    public Builder accessKeyId(String value) {
      return put("access_key_id", value);
    }

    public Builder accessKeySecret(String value) {
      return put("access_key_secret", value);
    }

    public EnvironmentStorageCredential build() {
      require("type");

      return new EnvironmentStorageCredential(json());
    }
  }
}
