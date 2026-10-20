package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeAudioFormat extends JsonObject {
  private RealtimeAudioFormat(JsonValue value) {
    super(value);
  }

  public static RealtimeAudioFormat fromJson(JsonValue value) {
    return new RealtimeAudioFormat(value);
  }

  public String format() {
    return optional("format", JsonValue::asString);
  }

  public Long sampleRate() {
    return optional("sample_rate", JsonValue::asLong);
  }

  public Long channels() {
    return optional("channels", JsonValue::asLong);
  }
}
