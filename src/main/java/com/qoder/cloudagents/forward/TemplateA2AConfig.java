package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.List;

/** A2A publishing settings; updates preserve omission and replace supplied skill arrays. */
public final class TemplateA2AConfig extends JsonObject {
  private TemplateA2AConfig(JsonValue value) {
    super(value);
  }

  public static TemplateA2AConfig fromJson(JsonValue value) {
    return new TemplateA2AConfig(value);
  }

  public Boolean enabled() {
    return optionalBoolean("enabled");
  }

  public String description() {
    return optionalString("description");
  }

  public List<TemplateA2ASkill> skills() {
    return optionalList("skills", TemplateA2ASkill::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public Builder skills(List<TemplateA2ASkill> value) {
      return put("skills", value);
    }

    public TemplateA2AConfig build() {
      return new TemplateA2AConfig(json());
    }
  }
}
