package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DecisionQuestion extends JsonObject {
  private DecisionQuestion(JsonValue value) {
    super(value);
  }

  public static DecisionQuestion fromJson(JsonValue value) {
    return new DecisionQuestion(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String instructions() {
    return optional("instructions", JsonValue::asString);
  }

  public List<DecisionChoice> choices() {
    return optionalList("choices", DecisionChoice::fromJson);
  }

  public List<DecisionLevel> levels() {
    return optionalList("levels", DecisionLevel::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder instructions(String value) {
      return put("instructions", value);
    }

    public Builder choices(List<DecisionChoice> value) {
      return put("choices", value);
    }

    public Builder levels(List<DecisionLevel> value) {
      return put("levels", value);
    }

    public DecisionQuestion build() {
      require("type", "instructions");
      String type = json().get("type").asString();
      if (!type.equals("choice") && !type.equals("score"))
        throw new ConfigurationException("Question type must be choice or score");
      String key = type.equals("choice") ? "choices" : "levels",
          other = type.equals("choice") ? "levels" : "choices";
      require(key);
      int count = json().get(key).asArray().size();
      if (count < 2 || count > 32 || !json().get(other).isMissing())
        throw new ConfigurationException("Questions require 2-32 entries of their own type");
      return new DecisionQuestion(json());
    }
  }
}
