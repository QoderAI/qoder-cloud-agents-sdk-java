package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class MemoryStoreCreateMount extends JsonObject {
  private MemoryStoreCreateMount(JsonValue value) {
    super(value);
  }

  public String access() {
    return optional("access", JsonValue::asString);
  }

  public static MemoryStoreCreateMount fromJson(JsonValue value) {
    return new MemoryStoreCreateMount(value);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder identityId(String value) {
      return put("identity_id", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public Builder access(String value) {
      return put("access", value);
    }

    public MemoryStoreCreateMount build() {
      require("identity_id");
      require("template_id");

      return new MemoryStoreCreateMount(json());
    }
  }
}
