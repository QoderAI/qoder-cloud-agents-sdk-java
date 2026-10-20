package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class BatchCreateParams extends JsonObject {
  private BatchCreateParams(JsonValue value) {
    super(value);
  }

  public static BatchCreateParams fromJson(JsonValue value) {
    return new BatchCreateParams(value);
  }

  public String inputFileId() {
    return optional("input_file_id", JsonValue::asString);
  }

  public String completionWindow() {
    return optional("completion_window", JsonValue::asString);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public Boolean ignoreIdleWindow() {
    return optional("ignore_idle_window", JsonValue::asBoolean);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder inputFileId(String value) {
      return put("input_file_id", value);
    }

    public Builder completionWindow(String value) {
      return put("completion_window", value);
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public Builder ignoreIdleWindow(Boolean value) {
      return put("ignore_idle_window", value);
    }

    public BatchCreateParams build() {
      require("input_file_id", "completion_window");
      if (json().get("ignore_idle_window").isNull())
        throw new ConfigurationException("ignoreIdleWindow cannot be null");
      return new BatchCreateParams(json());
    }
  }
}
