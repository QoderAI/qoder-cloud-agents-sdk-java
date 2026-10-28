package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DreamUsage extends JsonObject {
  private DreamUsage(JsonValue value) {
    super(value);
  }

  public static DreamUsage fromJson(JsonValue value) {
    return new DreamUsage(value);
  }

  public Long inputTokens() {
    return optional("input_tokens", JsonValue::asLong);
  }

  public Long outputTokens() {
    return optional("output_tokens", JsonValue::asLong);
  }

  public Long cacheCreationInputTokens() {
    return optional("cache_creation_input_tokens", JsonValue::asLong);
  }

  public Long cacheReadInputTokens() {
    return optional("cache_read_input_tokens", JsonValue::asLong);
  }
}
