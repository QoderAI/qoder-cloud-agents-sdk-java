package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable Realtime JSON object; unknown fields are preserved. */
public final class RealtimeConversationCreateParams extends JsonObject {
  private RealtimeConversationCreateParams(JsonValue value) {
    super(value);
  }

  public static RealtimeConversationCreateParams fromJson(JsonValue value) {
    return new RealtimeConversationCreateParams(value);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String title() {
    return optional("title", JsonValue::asString);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", v -> v);
  }

  public RealtimeConfig config() {
    return optional("config", RealtimeConfig::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder identityId(String value) {
      return put("identity_id", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public Builder title(String value) {
      return put("title", value);
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public Builder config(RealtimeConfig value) {
      return put("config", value);
    }

    public RealtimeConversationCreateParams build() {
      require("identity_id");
      require("template_id");
      return new RealtimeConversationCreateParams(json());
    }
  }
}
