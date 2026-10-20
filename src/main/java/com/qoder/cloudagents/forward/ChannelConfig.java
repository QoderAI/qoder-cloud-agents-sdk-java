package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class ChannelConfig extends JsonObject {
  private ChannelConfig(JsonValue value) {
    super(value);
  }

  public static ChannelConfig fromJson(JsonValue value) {
    return new ChannelConfig(value);
  }

  public ChannelResponseOptions responseOptions() {
    return optional("response_options", ChannelResponseOptions::fromJson);
  }
}
