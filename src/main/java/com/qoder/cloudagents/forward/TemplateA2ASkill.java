package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.List;

/** Agent Card capability metadata, independent of the Template's Skill resource bindings. */
public final class TemplateA2ASkill extends JsonObject {
  private TemplateA2ASkill(JsonValue value) {
    super(value);
  }

  public static TemplateA2ASkill fromJson(JsonValue value) {
    return new TemplateA2ASkill(value);
  }

  public String id() {
    return optionalString("id");
  }

  public String name() {
    return optionalString("name");
  }

  public String description() {
    return optionalString("description");
  }

  public List<String> tags() {
    return optionalList("tags", JsonValue::asString);
  }

  public List<String> examples() {
    return optionalList("examples", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder id(String value) {
      return put("id", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public Builder tags(List<String> value) {
      return put("tags", value);
    }

    public Builder examples(List<String> value) {
      return put("examples", value);
    }

    public TemplateA2ASkill build() {
      require("id", "name", "description");
      return new TemplateA2ASkill(json());
    }
  }
}
