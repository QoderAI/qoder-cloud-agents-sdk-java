package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** OSS credential read view exposing only its type and, for AccessKey, a masked ID. */
public final class EnvironmentStorageCredential extends JsonObject {
  private EnvironmentStorageCredential(JsonValue value) {
    super(value);
  }

  public static EnvironmentStorageCredential fromJson(JsonValue value) {
    return new EnvironmentStorageCredential(value);
  }

  public String type() {
    return optionalString("type");
  }

  public String accessKeyId() {
    return optionalString("access_key_id");
  }
}
