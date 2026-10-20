package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeAudioConfig extends JsonObject {
  private RealtimeAudioConfig(JsonValue value) {
    super(value);
  }

  public static RealtimeAudioConfig fromJson(JsonValue value) {
    return new RealtimeAudioConfig(value);
  }

  public RealtimeAudioOutputConfig output() {
    return optional("output", RealtimeAudioOutputConfig::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder output(RealtimeAudioOutputConfig value) {
      return put("output", value);
    }

    public RealtimeAudioConfig build() {
      return new RealtimeAudioConfig(json());
    }
  }
}
