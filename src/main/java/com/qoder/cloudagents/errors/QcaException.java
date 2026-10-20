package com.qoder.cloudagents.errors;

/** Base class for SDK failures. */
public class QcaException extends RuntimeException {
  public QcaException(String message) {
    super(message);
  }

  public QcaException(String message, Throwable cause) {
    super(message, cause);
  }
}
