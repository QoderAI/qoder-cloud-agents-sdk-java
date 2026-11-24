package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.List;

/** Environment storage read view containing bucket mounts with redacted credentials. */
public final class EnvironmentStorage extends JsonObject {
  private EnvironmentStorage(JsonValue value) {
    super(value);
  }

  public static EnvironmentStorage fromJson(JsonValue value) {
    return new EnvironmentStorage(value);
  }

  public List<EnvironmentStorageBucket> buckets() {
    return optionalList("buckets", EnvironmentStorageBucket::fromJson);
  }
}
