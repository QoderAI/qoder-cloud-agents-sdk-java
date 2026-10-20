package com.qoder.cloudagents.core.internal;

import com.qoder.cloudagents.errors.ApiUserAbortException;
import com.qoder.cloudagents.errors.QcaException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/** Internal future adapters that retain cancellation propagation. */
public final class Futures {
  private Futures() {}

  public static <T> CompletableFuture<T> failed(Throwable error) {
    CompletableFuture<T> result = new CompletableFuture<>();
    result.completeExceptionally(error);
    return result;
  }

  public static Throwable unwrap(Throwable error) {
    while ((error instanceof CompletionException || error instanceof ExecutionException)
        && error.getCause() != null) error = error.getCause();
    return error;
  }

  public static <T, R> CompletableFuture<R> map(
      CompletableFuture<T> source, Function<T, R> mapper) {
    return mapOwned(source, mapper, ignored -> {});
  }

  /** Releases a resource when cancellation wins the race with delivery. */
  public static <T, R> CompletableFuture<R> mapOwned(
      CompletableFuture<T> source, Function<T, R> mapper, java.util.function.Consumer<T> discard) {
    CompletableFuture<R> result = new CompletableFuture<>();
    result.whenComplete(
        (value, error) -> {
          if (result.isCancelled()) source.cancel(true);
        });
    source.whenComplete(
        (value, error) -> {
          if (result.isDone()) {
            if (error == null) discard.accept(value);
            return;
          }
          if (error != null) result.completeExceptionally(unwrap(error));
          else {
            try {
              if (!result.complete(mapper.apply(value))) discard.accept(value);
            } catch (Throwable failure) {
              discard.accept(value);
              result.completeExceptionally(failure);
            }
          }
        });
    return result;
  }

  public static <T> T await(CompletableFuture<T> future) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      future.cancel(true);
      Thread.currentThread().interrupt();
      throw new ApiUserAbortException("Request interrupted");
    } catch (CancellationException e) {
      throw new ApiUserAbortException("Request cancelled");
    } catch (ExecutionException e) {
      Throwable cause = unwrap(e);
      if (cause instanceof RuntimeException) throw (RuntimeException) cause;
      throw new QcaException("Request failed", cause);
    }
  }
}
