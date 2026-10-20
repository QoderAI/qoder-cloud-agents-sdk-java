package com.qoder.cloudagents.errors;

/** Base class for HTTP failures, connection errors, timeouts and synchronous cancellation. */
public class ApiException extends QcaException {
  public ApiException(String message) {
    super(message);
  }

  public ApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
