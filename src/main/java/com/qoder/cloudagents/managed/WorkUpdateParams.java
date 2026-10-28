package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class WorkUpdateParams extends JsonObject {
  private WorkUpdateParams(JsonValue value) {
    super(value);
  }

  public static WorkUpdateParams fromJson(JsonValue value) {
    return new WorkUpdateParams(value);
  }

  public Map<String, String> metadata() {
    return optionalNullableMap("metadata", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public WorkUpdateParams build() {

      return new WorkUpdateParams(json());
    }
  }
}
