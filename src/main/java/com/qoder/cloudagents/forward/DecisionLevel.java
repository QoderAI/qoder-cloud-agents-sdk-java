package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.math.BigDecimal;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DecisionLevel extends JsonObject {
  private DecisionLevel(JsonValue value) {
    super(value);
  }

  public static DecisionLevel fromJson(JsonValue value) {
    return new DecisionLevel(value);
  }

  public String label() {
    return optional("label", JsonValue::asString);
  }

  public BigDecimal score() {
    return optional("score", JsonValue::asDecimal);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder label(String value) {
      return put("label", value);
    }

    public Builder score(BigDecimal value) {
      return put("score", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public DecisionLevel build() {
      require("label", "score");
      return new DecisionLevel(json());
    }
  }
}
