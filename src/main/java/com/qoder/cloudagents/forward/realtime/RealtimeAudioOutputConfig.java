package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeAudioOutputConfig extends JsonObject {
  private RealtimeAudioOutputConfig(JsonValue value) {
    super(value);
  }

  public static RealtimeAudioOutputConfig fromJson(JsonValue value) {
    return new RealtimeAudioOutputConfig(value);
  }

  public String voice() {
    return optional("voice", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder voice(String value) {
      return put("voice", value);
    }

    public RealtimeAudioOutputConfig build() {
      return new RealtimeAudioOutputConfig(json());
    }
  }
}
