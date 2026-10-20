package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonValue;
import java.util.List;

/** Immutable ManagedToolConfig configuration. Unknown fields are retained through field(). */
public final class ManagedToolConfig extends JsonObject {
  private ManagedToolConfig(JsonValue value) {
    super(value);
  }

  public static ManagedToolConfig fromJson(JsonValue value) {
    return new ManagedToolConfig(value);
  }

  public List<String> enabledTools() {
    return optionalList("enabled_tools", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder enabledTools(List<String> value) {
      return put("enabled_tools", value);
    }

    public ManagedToolConfig build() {
      return new ManagedToolConfig(json());
    }
  }
}
