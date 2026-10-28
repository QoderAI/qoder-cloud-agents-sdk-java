package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.math.BigDecimal;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class Model extends JsonModel {
  public Model(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static Model fromJson(JsonValue value) {
    return new Model(value, null);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String displayName() {
    return optional("display_name", JsonValue::asString);
  }

  public String source() {
    return optional("source", JsonValue::asString);
  }

  public String defaultEffort() {
    return optional("default_effort", JsonValue::asString);
  }

  public Boolean isEnabled() {
    return optional("is_enabled", JsonValue::asBoolean);
  }

  public Boolean isNew() {
    return optional("is_new", JsonValue::asBoolean);
  }

  public BigDecimal priceFactor() {
    return optional("price_factor", JsonValue::asDecimal);
  }

  public List<String> efforts() {
    return optionalList("efforts", JsonValue::asString);
  }

  public Long maxInputTokens() {
    return optional("max_input_tokens", JsonValue::asLong);
  }

  public Long defaultContextWindow() {
    return optional("default_context_window", JsonValue::asLong);
  }

  public List<Long> availableContextWindows() {
    return optionalList("available_context_windows", JsonValue::asLong);
  }
}
