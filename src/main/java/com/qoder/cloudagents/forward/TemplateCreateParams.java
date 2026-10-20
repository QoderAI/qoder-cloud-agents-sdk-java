package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonValue;

/** Immutable Template create body. Header overrides use RequestOptions. */
public final class TemplateCreateParams extends JsonObject {
  private TemplateCreateParams(JsonValue value) {
    super(value);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends TemplateConfigBuilder<Builder> {
    private Builder() {}

    public TemplateCreateParams build() {
      require("name", "model", "environment_id");
      return new TemplateCreateParams(json());
    }
  }
}
