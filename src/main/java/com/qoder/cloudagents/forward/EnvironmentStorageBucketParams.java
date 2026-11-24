package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** OSS bucket mount request; omitted credentials on update keep the stored credential. */
public final class EnvironmentStorageBucketParams extends JsonObject {
  private EnvironmentStorageBucketParams(JsonValue value) {
    super(value);
  }

  public static EnvironmentStorageBucketParams fromJson(JsonValue value) {
    return new EnvironmentStorageBucketParams(value);
  }

  public String name() {
    return optionalString("name");
  }

  public String provider() {
    return optionalString("provider");
  }

  public String region() {
    return optionalString("region");
  }

  public String bucket() {
    return optionalString("bucket");
  }

  public String prefix() {
    return optionalString("prefix");
  }

  public String access() {
    return optionalString("access");
  }

  public String acceleratorEndpoint() {
    return optionalString("accelerator_endpoint");
  }

  public EnvironmentStorageCredentialParams credential() {
    return optional("credential", EnvironmentStorageCredentialParams::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder provider(String value) {
      return put("provider", value);
    }

    public Builder region(String value) {
      return put("region", value);
    }

    public Builder bucket(String value) {
      return put("bucket", value);
    }

    public Builder prefix(String value) {
      return put("prefix", value);
    }

    public Builder access(String value) {
      return put("access", value);
    }

    public Builder acceleratorEndpoint(String value) {
      return put("accelerator_endpoint", value);
    }

    public Builder credential(EnvironmentStorageCredentialParams value) {
      return put("credential", value);
    }

    public EnvironmentStorageBucketParams build() {
      require("name", "provider", "region", "bucket");
      return new EnvironmentStorageBucketParams(json());
    }
  }
}
