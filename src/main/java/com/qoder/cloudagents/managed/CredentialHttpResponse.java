package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class CredentialHttpResponse extends JsonObject {
  private CredentialHttpResponse(JsonValue value) {
    super(value);
  }

  public static CredentialHttpResponse fromJson(JsonValue value) {
    return new CredentialHttpResponse(value);
  }

  public Long statusCode() {
    return optional("status_code", JsonValue::asLong);
  }

  public String contentType() {
    return optional("content_type", JsonValue::asString);
  }

  public String body() {
    return optional("body", JsonValue::asString);
  }

  public Boolean bodyTruncated() {
    return optional("body_truncated", JsonValue::asBoolean);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder statusCode(Long value) {
      return put("status_code", value);
    }

    public Builder statusCode(long value) {
      return statusCode(Long.valueOf(value));
    }

    public Builder contentType(String value) {
      return put("content_type", value);
    }

    public Builder body(String value) {
      return put("body", value);
    }

    public Builder bodyTruncated(Boolean value) {
      return put("body_truncated", value);
    }

    public CredentialHttpResponse build() {

      return new CredentialHttpResponse(json());
    }
  }
}
