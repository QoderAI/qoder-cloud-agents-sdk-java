package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonValue;

/** Immutable Template update body. Header overrides use RequestOptions. */
public final class TemplateUpdateParams extends JsonObject {
  private TemplateUpdateParams(JsonValue value) {
    super(value);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends TemplateConfigBuilder<Builder> {
    private Builder() {}

    public TemplateUpdateParams build() {

      return new TemplateUpdateParams(json());
    }
  }
}
