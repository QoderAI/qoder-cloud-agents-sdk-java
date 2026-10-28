package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
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

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder buckets(List<EnvironmentStorageBucket> value) {
      return put("buckets", value);
    }

    public EnvironmentStorage build() {

      return new EnvironmentStorage(json());
    }
  }
}
