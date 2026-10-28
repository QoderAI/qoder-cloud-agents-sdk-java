package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionResourceCreateParams extends JsonObject {
  private SessionResourceCreateParams(JsonValue value) {
    super(value);
  }

  public static SessionResourceCreateParams fromJson(JsonValue value) {
    return new SessionResourceCreateParams(value);
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

    public SessionResourceCreateParams build() {
      require("type", "file_id");
      if (!"file".equals(json().get("type").asString()))
        throw new ConfigurationException("Only file resources can be added to an existing session");
      return new SessionResourceCreateParams(json());
    }
  }
}
