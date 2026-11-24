package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.List;

/** Cloud Environment bucket mount input; an empty buckets array removes the mounts on update. */
public final class EnvironmentStorageParams extends JsonObject {
  private EnvironmentStorageParams(JsonValue value) {
    super(value);
  }

  public static EnvironmentStorageParams fromJson(JsonValue value) {
    return new EnvironmentStorageParams(value);
  }

  public List<EnvironmentStorageBucketParams> buckets() {
    return optionalList("buckets", EnvironmentStorageBucketParams::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder buckets(List<EnvironmentStorageBucketParams> value) {
      return put("buckets", value);
    }

    public EnvironmentStorageParams build() {
      return new EnvironmentStorageParams(json());
    }
  }
}
