package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class ContentBlock extends JsonObject {
  private ContentBlock(JsonValue value) {
    super(value);
  }

  public static ContentBlock fromJson(JsonValue value) {
    return new ContentBlock(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String text() {
    return optional("text", JsonValue::asString);
  }

  public String thinking() {
    return optional("thinking", JsonValue::asString);
  }

  public ImageSource source() {
    return optional("source", ImageSource::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder text(String value) {
      return put("text", value);
    }

    public Builder thinking(String value) {
      return put("thinking", value);
    }

    public Builder source(ImageSource value) {
      return put("source", value);
    }

    public ContentBlock build() {
      require("type");
      return new ContentBlock(json());
    }
  }
}
