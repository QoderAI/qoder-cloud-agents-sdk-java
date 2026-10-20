package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionResourceSpec extends JsonObject {
  private SessionResourceSpec(JsonValue value) {
    super(value);
  }

  public static SessionResourceSpec fromJson(JsonValue value) {
    return new SessionResourceSpec(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String fileId() {
    return optional("file_id", JsonValue::asString);
  }

  public String mountPath() {
    return optional("mount_path", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder fileId(String value) {
      return put("file_id", value);
    }

    public Builder mountPath(String value) {
      return put("mount_path", value);
    }

    public SessionResourceSpec build() {
      require("type");
      require("file_id");
      return new SessionResourceSpec(json());
    }
  }
}
