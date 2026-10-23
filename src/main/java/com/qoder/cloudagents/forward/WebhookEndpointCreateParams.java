package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable request. Unset fields are omitted; null metadata entries remove keys on update. */
public final class WebhookEndpointCreateParams extends JsonObject {
  private WebhookEndpointCreateParams(JsonValue value) {
    super(value);
  }

  public static WebhookEndpointCreateParams fromJson(JsonValue value) {
    return new WebhookEndpointCreateParams(value);
  }

  public String url() {
    return optional("url", JsonValue::asString);
  }

  public List<String> events() {
    return optionalList("events", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public Map<String, String> metadata() {
    return optionalMap("metadata", v -> v.isNull() ? null : v.asString());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder url(String value) {
      return put("url", value);
    }

    public Builder events(List<String> value) {
      return put("events", value);
    }

    public Builder description(String value) {
      return put("description", value);
    }

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public WebhookEndpointCreateParams build() {
      require("url", "events");
      return new WebhookEndpointCreateParams(json());
    }
  }
}
