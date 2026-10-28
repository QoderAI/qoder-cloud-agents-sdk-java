package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class WorkStopParams extends JsonObject {
  private WorkStopParams(JsonValue value) {
    super(value);
  }

  public static WorkStopParams fromJson(JsonValue value) {
    return new WorkStopParams(value);
  }

  public Boolean force() {
    return optional("force", JsonValue::asBoolean);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder force(Boolean value) {
      return put("force", value);
    }

    public WorkStopParams build() {

      return new WorkStopParams(json());
    }
  }
}
