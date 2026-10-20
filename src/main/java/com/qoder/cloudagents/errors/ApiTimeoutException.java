package com.qoder.cloudagents.errors;

public final class ApiTimeoutException extends ApiConnectionException {
  public ApiTimeoutException(String message) {
    super(message);
  }
}
