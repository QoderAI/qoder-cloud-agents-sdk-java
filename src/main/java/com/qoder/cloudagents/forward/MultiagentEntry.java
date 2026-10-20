package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonValue;

/** Immutable MultiagentEntry configuration. Unknown fields are retained through field(). */
public final class MultiagentEntry extends JsonObject {
  private MultiagentEntry(JsonValue value) {
    super(value);
  }

  public static MultiagentEntry fromJson(JsonValue value) {
    return new MultiagentEntry(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String model() {
    return optional("model", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder model(String value) {
      return put("model", value);
    }

    public MultiagentEntry build() {
      require("type");
      return new MultiagentEntry(json());
    }
  }
}
