package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class OAuthStartResponse extends JsonModel {
  public OAuthStartResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static OAuthStartResponse fromJson(JsonValue value) {
    return new OAuthStartResponse(value, null);
  }

  public String authorizationUrl() {
    return optional("authorization_url", JsonValue::asString);
  }

  public String callbackOrigin() {
    return optional("callback_origin", JsonValue::asString);
  }

  public String state() {
    return optional("state", JsonValue::asString);
  }
}
