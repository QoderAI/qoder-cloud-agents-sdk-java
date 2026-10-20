package com.qoder.cloudagents.core;

/** Typed view over an immutable response. Request metadata is not part of the business JSON. */
public abstract class JsonModel extends JsonObject {
  private final String requestId;

  protected JsonModel(JsonValue value, String requestId) {
    super(value);
    this.requestId = requestId;
  }

  public final String requestId() {
    return requestId;
  }
}
