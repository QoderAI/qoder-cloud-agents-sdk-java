package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class ChannelConfigParams extends JsonObject {
  private ChannelConfigParams(JsonValue value) {
    super(value);
  }

  public static ChannelConfigParams fromJson(JsonValue value) {
    return new ChannelConfigParams(value);
  }

  public ChannelCredentials credentials() {
    return optional("credentials", ChannelCredentials::fromJson);
  }

  public ChannelResponseOptions responseOptions() {
    return optional("response_options", ChannelResponseOptions::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder credentials(ChannelCredentials value) {
      return put("credentials", value);
    }

    public Builder responseOptions(ChannelResponseOptions value) {
      return put("response_options", value);
    }

    public ChannelConfigParams build() {
      return new ChannelConfigParams(json());
    }
  }
}
