package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonValue;
import java.util.List;

/** Immutable MultiagentConfig configuration. Unknown fields are retained through field(). */
public final class MultiagentConfig extends JsonObject {
  private MultiagentConfig(JsonValue value) {
    super(value);
  }

  public static MultiagentConfig fromJson(JsonValue value) {
    return new MultiagentConfig(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public List<MultiagentEntry> agents() {
    return optionalList("agents", MultiagentEntry::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder agents(List<MultiagentEntry> value) {
      return put("agents", value);
    }

    public MultiagentConfig build() {
      require("type", "agents");
      return new MultiagentConfig(json());
    }
  }
}
