package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonValue;

/** Immutable ToolConfig configuration. Unknown fields are retained through field(). */
public final class ToolConfig extends JsonObject {
  private ToolConfig(JsonValue value) {
    super(value);
  }

  public static ToolConfig fromJson(JsonValue value) {
    return new ToolConfig(value);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public PermissionPolicy permissionPolicy() {
    return optional("permission_policy", PermissionPolicy::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public Builder permissionPolicy(PermissionPolicy value) {
      return put("permission_policy", value);
    }

    public ToolConfig build() {
      require("name");
      return new ToolConfig(json());
    }
  }
}
