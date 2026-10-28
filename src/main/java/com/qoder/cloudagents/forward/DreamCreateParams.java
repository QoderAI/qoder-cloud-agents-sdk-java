package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DreamCreateParams extends JsonObject {
  private DreamCreateParams(JsonValue value) {
    super(value);
  }

  public static DreamCreateParams fromJson(JsonValue value) {
    return new DreamCreateParams(value);
  }

  public List<DreamInput> inputs() {
    return optionalList("inputs", DreamInput::fromJson);
  }

  public DreamModelConfig model() {
    return optional("model", DreamModelConfig::fromJson);
  }

  public String instructions() {
    return optional("instructions", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder inputs(List<DreamInput> value) {
      return put("inputs", value);
    }

    public Builder model(DreamModelConfig value) {
      return put("model", value);
    }

    public Builder instructions(String value) {
      return put("instructions", value);
    }

    public DreamCreateParams build() {
      require("inputs");
      long primary =
          json().get("inputs").asArray().stream()
              .filter(
                  v ->
                      "memory_store".equals(v.get("type").stringOrNull())
                          || "identity_template".equals(v.get("type").stringOrNull()))
              .count();
      if (primary != 1)
        throw new ConfigurationException(
            "Dream inputs require exactly one primary memory_store or identity_template");
      return new DreamCreateParams(json());
    }
  }
}
