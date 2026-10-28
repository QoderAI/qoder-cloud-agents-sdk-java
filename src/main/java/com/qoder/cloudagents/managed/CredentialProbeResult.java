package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class CredentialProbeResult extends JsonObject {
  private CredentialProbeResult(JsonValue value) {
    super(value);
  }

  public static CredentialProbeResult fromJson(JsonValue value) {
    return new CredentialProbeResult(value);
  }

  public String method() {
    return optional("method", JsonValue::asString);
  }

  public CredentialHttpResponse httpResponse() {
    return optional("http_response", CredentialHttpResponse::fromJson);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder method(String value) {
      return put("method", value);
    }

    public Builder httpResponse(CredentialHttpResponse value) {
      return put("http_response", value);
    }

    public CredentialProbeResult build() {

      return new CredentialProbeResult(json());
    }
  }
}
