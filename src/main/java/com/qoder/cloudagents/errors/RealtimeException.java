package com.qoder.cloudagents.errors;

import com.qoder.cloudagents.core.JsonValue;

/**
 * Realtime failure. Server content and close reason are exposed only through explicit accessors.
 */
public final class RealtimeException extends ApiException {
  private final String conversationId, requestId, closeReason;
  private final Integer closeCode;
  private final JsonValue event;

  public RealtimeException(
      String message,
      String conversationId,
      String requestId,
      Integer closeCode,
      String closeReason,
      JsonValue event) {
    super(message);
    this.conversationId = conversationId;
    this.requestId = requestId;
    this.closeCode = closeCode;
    this.closeReason = closeReason;
    this.event = event;
  }

  public String conversationId() {
    return conversationId;
  }

  public String requestId() {
    return requestId;
  }

  public Integer closeCode() {
    return closeCode;
  }

  public String closeReason() {
    return closeReason;
  }

  public JsonValue event() {
    return event;
  }
}
