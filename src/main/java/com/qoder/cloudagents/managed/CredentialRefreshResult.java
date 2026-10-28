package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class CredentialRefreshResult extends JsonObject {
  private CredentialRefreshResult(JsonValue value) {
    super(value);
  }

  public static CredentialRefreshResult fromJson(JsonValue value) {
    return new CredentialRefreshResult(value);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public CredentialHttpResponse httpResponse() {
    return optional("http_response", CredentialHttpResponse::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder status(String value) {
      return put("status", value);
    }

    public Builder httpResponse(CredentialHttpResponse value) {
      return put("http_response", value);
    }

    public CredentialRefreshResult build() {

      return new CredentialRefreshResult(json());
    }
  }
}
