package com.qoder.cloudagents.forward;

/** Missing, invalid, expired or mismatched delivery authentication. */
public final class WebhookVerificationException extends IllegalArgumentException {
  public WebhookVerificationException() {
    super("webhook signature verification failed");
  }
}
