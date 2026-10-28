package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonValue;

/** Immutable PermissionPolicy configuration. Unknown fields are retained through field(). */
public final class PermissionPolicy extends JsonObject {
  private PermissionPolicy(JsonValue value) {
    super(value);
  }

  public static PermissionPolicy fromJson(JsonValue value) {
    return new PermissionPolicy(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public PermissionPolicy build() {
      require("type");
      return new PermissionPolicy(json());
    }
  }
}
