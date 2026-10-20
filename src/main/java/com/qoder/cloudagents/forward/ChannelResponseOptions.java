package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ChannelResponseOptions extends JsonObject {
  private ChannelResponseOptions(JsonValue value) {
    super(value);
  }

  public static ChannelResponseOptions fromJson(JsonValue value) {
    return new ChannelResponseOptions(value);
  }

  public Boolean includeToolCalls() {
    return optional("include_tool_calls", JsonValue::asBoolean);
  }

  public Boolean includeThinking() {
    return optional("include_thinking", JsonValue::asBoolean);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder includeToolCalls(Boolean value) {
      return put("include_tool_calls", value);
    }

    public Builder includeThinking(Boolean value) {
      return put("include_thinking", value);
    }

    public ChannelResponseOptions build() {
      return new ChannelResponseOptions(json());
    }
  }
}
