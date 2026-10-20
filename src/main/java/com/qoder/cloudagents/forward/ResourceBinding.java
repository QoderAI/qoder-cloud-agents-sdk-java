package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonValue;

/** Immutable ResourceBinding configuration. Unknown fields are retained through field(). */
public final class ResourceBinding extends JsonObject {
  private ResourceBinding(JsonValue value) {
    super(value);
  }

  public static ResourceBinding fromJson(JsonValue value) {
    return new ResourceBinding(value);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public ResourceBinding build() {
      return new ResourceBinding(json());
    }
  }
}
