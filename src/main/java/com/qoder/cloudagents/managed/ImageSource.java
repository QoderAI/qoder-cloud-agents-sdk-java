package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class ImageSource extends JsonObject {
  private ImageSource(JsonValue value) {
    super(value);
  }

  public String fileId() {
    return optional("file_id", JsonValue::asString);
  }

  public static ImageSource fromJson(JsonValue value) {
    return new ImageSource(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String mediaType() {
    return optional("media_type", JsonValue::asString);
  }

  public String data() {
    return optional("data", JsonValue::asString);
  }

  public String url() {
    return optional("url", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder mediaType(String value) {
      return put("media_type", value);
    }

    public Builder data(String value) {
      return put("data", value);
    }

    public Builder url(String value) {
      return put("url", value);
    }

    public Builder fileId(String value) {
      return put("file_id", value);
    }

    public ImageSource build() {
      require("type");
      return new ImageSource(json());
    }
  }
}
