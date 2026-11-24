package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Write-only RAM role or AccessKey credential input, separate from the redacted read view. */
public final class EnvironmentStorageCredentialParams extends JsonObject {
  private EnvironmentStorageCredentialParams(JsonValue value) {
    super(value);
  }

  public static EnvironmentStorageCredentialParams fromJson(JsonValue value) {
    return new EnvironmentStorageCredentialParams(value);
  }

  public String type() {
    return optionalString("type");
  }

  public String roleArn() {
    return optionalString("role_arn");
  }

  public String accessKeyId() {
    return optionalString("access_key_id");
  }

  public String accessKeySecret() {
    return optionalString("access_key_secret");
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

    public EnvironmentStorageCredentialParams build() {
      require("type");
      return new EnvironmentStorageCredentialParams(json());
    }
  }
}
