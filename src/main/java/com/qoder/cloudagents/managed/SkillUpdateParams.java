package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SkillUpdateParams extends JsonObject {
  private SkillUpdateParams(JsonValue value) {
    super(value);
  }

  public static SkillUpdateParams fromJson(JsonValue value) {
    return new SkillUpdateParams(value);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public String content() {
    return optional("content", JsonValue::asString);
  }

  public String contentEncoding() {
    return optional("content_encoding", JsonValue::asString);
  }

  public Map<String, String> metadata() {
    return optionalNullableMap("metadata", JsonValue::asString);
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

    public Builder content(String value) {
      return put("content", value);
    }

    public Builder contentEncoding(String value) {
      return put("content_encoding", value);
    }

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public SkillUpdateParams build() {

      return new SkillUpdateParams(json());
    }
  }
}
