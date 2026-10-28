package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.math.BigDecimal;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionBudget extends JsonObject {
  private SessionBudget(JsonValue value) {
    super(value);
  }

  public static SessionBudget fromJson(JsonValue value) {
    return new SessionBudget(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String maxCreditCost() {
    return optional("max_credit_cost", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder maxCreditCost(String value) {
      return put("max_credit_cost", value);
    }

    public SessionBudget build() {
      require("type", "max_credit_cost");
      if (!"limit".equals(json().get("type").asString()))
        throw new ConfigurationException("Budget type must be limit");
      String cost = json().get("max_credit_cost").asString();
      if (!cost.matches("[0-9]{1,12}(\\.[0-9]{1,2})?") || new BigDecimal(cost).signum() <= 0)
        throw new ConfigurationException(
            "maxCreditCost must be a positive decimal string with at most 12 integer and 2"
                + " fractional digits");
      return new SessionBudget(json());
    }
  }
}
