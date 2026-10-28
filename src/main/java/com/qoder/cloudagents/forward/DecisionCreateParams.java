package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DecisionCreateParams extends JsonObject {
  private DecisionCreateParams(JsonValue value) {
    super(value);
  }

  public static DecisionCreateParams fromJson(JsonValue value) {
    return new DecisionCreateParams(value);
  }

  public Map<String, JsonValue> state() {
    return optionalMap("state", value -> value);
  }

  public String sessionId() {
    return optional("session_id", JsonValue::asString);
  }

  public List<DecisionQuestion> questions() {
    return optionalList("questions", DecisionQuestion::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder state(Map<String, JsonValue> value) {
      return put("state", value);
    }

    public Builder sessionId(String value) {
      return put("session_id", value);
    }

    public Builder questions(List<DecisionQuestion> value) {
      return put("questions", value);
    }

    public DecisionCreateParams build() {
      require("questions");
      boolean state = !json().get("state").isMissing(),
          session = !json().get("session_id").isMissing();
      if (state == session)
        throw new ConfigurationException("Supply exactly one of state or sessionId");
      require(state ? "state" : "session_id");
      if (state
          && (json().get("state").asObject().isEmpty()
              || !json().get("state").get("session_context").isMissing()))
        throw new ConfigurationException(
            "Decision state must be nonempty and cannot contain session_context");
      if (json().get("questions").asArray().isEmpty())
        throw new ConfigurationException("Questions must not be empty");
      return new DecisionCreateParams(json());
    }
  }
}
