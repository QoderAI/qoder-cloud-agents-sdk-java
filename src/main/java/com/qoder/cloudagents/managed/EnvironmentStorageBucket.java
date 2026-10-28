package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class EnvironmentStorageBucket extends JsonObject {
  private EnvironmentStorageBucket(JsonValue value) {
    super(value);
  }

  public static EnvironmentStorageBucket fromJson(JsonValue value) {
    return new EnvironmentStorageBucket(value);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String provider() {
    return optional("provider", JsonValue::asString);
  }

  public String region() {
    return optional("region", JsonValue::asString);
  }

  public String bucket() {
    return optional("bucket", JsonValue::asString);
  }

  public String prefix() {
    return optional("prefix", JsonValue::asString);
  }

  public String access() {
    return optional("access", JsonValue::asString);
  }

  public String acceleratorEndpoint() {
    return optional("accelerator_endpoint", JsonValue::asString);
  }

  public EnvironmentStorageCredential credential() {
    return optional("credential", EnvironmentStorageCredential::fromJson);
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

    public Builder credential(EnvironmentStorageCredential value) {
      return put("credential", value);
    }

    public EnvironmentStorageBucket build() {
      require("name", "provider", "region", "bucket");

      return new EnvironmentStorageBucket(json());
    }
  }
}
