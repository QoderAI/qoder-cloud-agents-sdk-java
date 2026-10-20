package com.qoder.cloudagents.errors;

import com.qoder.cloudagents.core.JsonValue;

/** An SSE error frame. Server payload is available explicitly, never in the exception message. */
public final class StreamException extends ApiException {
  private final JsonValue body;
  private final String requestId;

  public StreamException(JsonValue body, String requestId) {
    super("QCA event stream returned an error");
    this.body = body;
    this.requestId = requestId;
  }

  public JsonValue body() {
    return body;
  }

  public String requestId() {
    return requestId;
  }
}
