package com.qoder.cloudagents.streaming;

import java.util.concurrent.CompletableFuture;

/** Cancellation handle and terminal outcome for an asynchronous event consumer. */
public final class EventSubscription implements AutoCloseable {
  private final CompletableFuture<Void> completion = new CompletableFuture<>();
  private final Runnable close;

  EventSubscription(Runnable close) {
    this.close = close;
    completion.whenComplete(
        (value, error) -> {
          if (completion.isCancelled()) close.run();
        });
  }

  public CompletableFuture<Void> completion() {
    return completion;
  }

  /** In-flight user callbacks may finish; no further callback is scheduled. */
  @Override
  public void close() {
    close.run();
    completion.cancel(false);
  }
}
