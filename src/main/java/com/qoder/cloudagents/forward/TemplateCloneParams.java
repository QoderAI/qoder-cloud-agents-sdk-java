package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Optional overrides for the cloned Template. */
public final class TemplateCloneParams extends JsonObject {
  private TemplateCloneParams(JsonValue value) {
    super(value);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public TemplateCloneParams build() {
      return new TemplateCloneParams(json());
    }
  }
}
