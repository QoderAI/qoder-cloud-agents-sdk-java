package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionUpdateParams extends JsonObject {
  private SessionUpdateParams(JsonValue value) {
    super(value);
  }

  public static SessionUpdateParams fromJson(JsonValue value) {
    return new SessionUpdateParams(value);
  }

  public String title() {
    return optional("title", JsonValue::asString);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", item -> item);
  }

  public SessionConfig config() {
    return optional("config", SessionConfig::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder title(String value) {
      return put("title", value);
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public Builder config(SessionConfig value) {
      return put("config", value);
    }

    public SessionUpdateParams build() {
      return new SessionUpdateParams(json());
    }
  }
}
