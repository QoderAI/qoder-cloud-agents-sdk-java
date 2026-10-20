package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class ToolOverride extends JsonObject {
  private ToolOverride(JsonValue value) {
    super(value);
  }

  public static ToolOverride fromJson(JsonValue value) {
    return new ToolOverride(value);
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

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public Builder permissionPolicy(PermissionPolicy value) {
      return put("permission_policy", value);
    }

    public ToolOverride build() {

      return new ToolOverride(json());
    }
  }
}
