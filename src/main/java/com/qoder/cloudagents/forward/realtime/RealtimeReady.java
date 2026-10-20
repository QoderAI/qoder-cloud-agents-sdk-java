package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeReady extends JsonObject {
  private RealtimeReady(JsonValue value) {
    super(value);
  }

  public static RealtimeReady fromJson(JsonValue value) {
    return new RealtimeReady(value);
  }

  public String provider() {
    return optional("provider", JsonValue::asString);
  }

  public RealtimeConfig config() {
    return optional("config", RealtimeConfig::fromJson);
  }

  public RealtimeAudioFormat inputAudio() {
    return optional("input_audio", RealtimeAudioFormat::fromJson);
  }

  public RealtimeAudioFormat outputAudio() {
    return optional("output_audio", RealtimeAudioFormat::fromJson);
  }

  public RealtimeCapabilities capabilities() {
    return optional("capabilities", RealtimeCapabilities::fromJson);
  }
}
