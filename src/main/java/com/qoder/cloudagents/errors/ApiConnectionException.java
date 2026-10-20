package com.qoder.cloudagents.errors;

public class ApiConnectionException extends ApiException {
  public ApiConnectionException(String message) {
    super(message);
  }

  public ApiConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
