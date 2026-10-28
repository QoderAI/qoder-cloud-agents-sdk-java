package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionResourceUpdateParams extends JsonObject {
  private SessionResourceUpdateParams(JsonValue value) {
    super(value);
  }

  public static SessionResourceUpdateParams fromJson(JsonValue value) {
    return new SessionResourceUpdateParams(value);
  }

  public String authorizationToken() {
    return optional("authorization_token", JsonValue::asString);
  }

  public String password() {
    return optional("password", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder authorizationToken(String value) {
      return put("authorization_token", value);
    }

    public Builder password(String value) {
      return put("password", value);
    }

    public SessionResourceUpdateParams build() {

      return new SessionResourceUpdateParams(json());
    }
  }
}
