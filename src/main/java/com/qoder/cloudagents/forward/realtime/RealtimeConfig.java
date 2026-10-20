package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeConfig extends JsonObject {
  private RealtimeConfig(JsonValue value) {
    super(value);
  }

  public static RealtimeConfig fromJson(JsonValue value) {
    return new RealtimeConfig(value);
  }

  public RealtimeAudioConfig audio() {
    return optional("audio", RealtimeAudioConfig::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder audio(RealtimeAudioConfig value) {
      return put("audio", value);
    }

    public RealtimeConfig build() {
      return new RealtimeConfig(json());
    }
  }
}
