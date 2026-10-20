package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionEventSendParams extends JsonObject {
  private SessionEventSendParams(JsonValue value) {
    super(value);
  }

  public static SessionEventSendParams fromJson(JsonValue value) {
    return new SessionEventSendParams(value);
  }

  public List<SessionEventParam> events() {
    return optionalList("events", SessionEventParam::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder events(List<SessionEventParam> value) {
      return put("events", value);
    }

    public SessionEventSendParams build() {
      require("events");
      return new SessionEventSendParams(json());
    }
  }
}
