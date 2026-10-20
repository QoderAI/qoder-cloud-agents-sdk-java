package com.qoder.cloudagents.errors;

/** No next page is available, or a repeated server cursor would cause a pagination loop. */
public final class PaginationException extends QcaException {
  public PaginationException(String message) {
    super(message);
  }
}
