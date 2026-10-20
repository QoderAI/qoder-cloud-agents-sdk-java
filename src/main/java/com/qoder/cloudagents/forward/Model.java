package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonModel;
import com.qoder.cloudagents.core.JsonValue;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public final class Model extends JsonModel {
  public Model(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return field("id").asString();
  }

  public String displayName() {
    return field("display_name").asString();
  }

  public boolean isEnabled() {
    return field("is_enabled").asBoolean();
  }

  public boolean isNew() {
    return field("is_new").asBoolean();
  }

  public boolean isVl() {
    return field("is_vl").asBoolean();
  }

  public boolean supportDisableReasoning() {
    return field("support_disable_reasoning").asBoolean();
  }

  public BigDecimal priceFactor() {
    return field("price_factor").asDecimal();
  }

  public List<String> efforts() {
    return field("efforts").asArray().stream()
        .map(JsonValue::asString)
        .collect(Collectors.toList());
  }

  public String defaultEffort() {
    return field("default_effort").asString();
  }

  public List<String> speed() {
    return field("speed").asArray().stream().map(JsonValue::asString).collect(Collectors.toList());
  }

  public long maxInputTokens() {
    return field("max_input_tokens").asLong();
  }

  public long defaultContextWindow() {
    return field("default_context_window").asLong();
  }

  public List<Long> availableContextWindows() {
    return field("available_context_windows").asArray().stream()
        .map(JsonValue::asLong)
        .collect(Collectors.toList());
  }
}
