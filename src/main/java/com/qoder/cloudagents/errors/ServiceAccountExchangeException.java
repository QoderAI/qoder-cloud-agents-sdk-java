package com.qoder.cloudagents.errors;

/** Safe issuer diagnostics. The issuer body and credentials are deliberately not retained. */
public final class ServiceAccountExchangeException extends ApiException {
  private final int statusCode;
  private final String requestId;

  public ServiceAccountExchangeException(int statusCode, String requestId) {
    super("SA exchange failed with HTTP " + statusCode);
    this.statusCode = statusCode;
    this.requestId = requestId;
  }

  public int statusCode() {
    return statusCode;
  }

  public String requestId() {
    return requestId;
  }
}
