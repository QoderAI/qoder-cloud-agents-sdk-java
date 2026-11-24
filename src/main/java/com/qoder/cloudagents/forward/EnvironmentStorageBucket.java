package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** OSS bucket mount read view; absent optional fields return null. */
public final class EnvironmentStorageBucket extends JsonObject {
  private EnvironmentStorageBucket(JsonValue value) {
    super(value);
  }

  public static EnvironmentStorageBucket fromJson(JsonValue value) {
    return new EnvironmentStorageBucket(value);
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

  public EnvironmentStorageCredential credential() {
    return optional("credential", EnvironmentStorageCredential::fromJson);
  }
}
