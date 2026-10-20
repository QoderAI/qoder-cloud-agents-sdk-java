package com.qoder.cloudagents.streaming;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.*;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;

/**
 * Single-consumer, closeable SSE stream. Reading is pull-based; callbacks run sequentially on the
 * chosen executor with no event queue. Always close when stopping before exhaustion.
 */
public final class EventStream<T> implements Iterable<T>, AutoCloseable {
  private final ClientEngine engine;
  private final String[] path;
  private final Map<String, List<String>> query;
  private final RequestOptions options;
  private final BiFunction<JsonValue, String, T> decode;
  private final Predicate<T> terminal;
  private final boolean resumable;
  private final AtomicBoolean consumed = new AtomicBoolean();
  private CompletableFuture<ApiResponse<FileDownload>> pending;
  private ApiResponse<FileDownload> response;
  private SseDecoder decoder;
  private String cursor;
  private boolean checkpoint, stopped;
  private RuntimeException failure;
  private long backoff = 500, connectedAt;
  private Runnable unwatch = () -> {};

  /** Internal construction; applications obtain streams from resource methods. */
  public EventStream(
      ClientEngine engine,
      String[] path,
      Map<String, List<String>> query,
      RequestOptions options,
      String initialCursor,
      BiFunction<JsonValue, String, T> decode,
      Predicate<T> terminal,
      boolean resumable) {
    this.engine = engine;
    this.path = path.clone();
    Map<String, List<String>> copy = new LinkedHashMap<>();
    query.forEach(
        (key, values) -> copy.put(key, Collections.unmodifiableList(new ArrayList<>(values))));
    this.query = Collections.unmodifiableMap(copy);
    this.options = options;
    this.decode = decode;
    this.terminal = terminal;
    this.resumable = resumable;
    this.cursor = initialCursor;
    Runnable cleanup = engine.watchStream(this::fail, options.totalTimeout());
    synchronized (this) {
      unwatch = cleanup;
      if (stopped) cleanup.run();
    }
  }

  /** Completes once response headers have been accepted. Cancelling it closes the subscription. */
  public CompletableFuture<EventStream<T>> openAsync() {
    CompletableFuture<EventStream<T>> result =
        Futures.mapOwned(open(), ignored -> this, ignored -> close());
    result.whenComplete(
        (value, error) -> {
          if (result.isCancelled()) close();
          else if (error != null) {
            Throwable cause = Futures.unwrap(error);
            fail(
                cause instanceof RuntimeException
                    ? (RuntimeException) cause
                    : new ApiConnectionException("Cannot open event stream", cause));
          }
        });
    return result;
  }

  private synchronized CompletableFuture<ApiResponse<FileDownload>> open() {
    if (failure != null) return Futures.failed(failure);
    if (stopped) return Futures.failed(new ApiUserAbortException("Event stream closed"));
    if (pending != null) return pending;
    RequestOptions.Builder request = options.toBuilder();
    if (checkpoint || cursor != null) request.header("Last-Event-ID", cursor == null ? "" : cursor);
    CompletableFuture<ApiResponse<FileDownload>> raw =
        engine.eventStream(path, query, request.build());
    pending =
        Futures.mapOwned(
            raw,
            value -> {
              synchronized (EventStream.this) {
                if (stopped) throw new ApiUserAbortException("Event stream closed");
                response = value;
                decoder = new SseDecoder(value.data().body(), cursor);
                connectedAt = System.nanoTime();
              }
              return value;
            },
            value -> value.data().close());
    return pending;
  }

  /**
   * Last fully yielded SSE frame ID, independent of the JSON payload ID. Empty clears the cursor.
   */
  public synchronized String lastEventId() {
    return cursor;
  }

  /** Metadata from the current/most recent successful connection; null before connection. */
  public synchronized ApiResponse<Void> response() {
    return response == null ? null : response.map(ignored -> null);
  }

  @Override
  public Iterator<T> iterator() {
    if (!consumed.compareAndSet(false, true))
      throw new IllegalStateException("EventStream supports one consumer");
    return new Iterator<T>() {
      private Item<T> next;

      public boolean hasNext() {
        synchronized (EventStream.this) {
          if (failure != null) throw failure;
          if (stopped) return false;
        }
        if (next == null) next = readNext();
        return next != null;
      }

      public T next() {
        if (!hasNext()) throw new NoSuchElementException();
        Item<T> item = next;
        next = null;
        synchronized (EventStream.this) {
          if (failure != null) throw failure;
          if (stopped) throw new NoSuchElementException();
          cursor = item.id;
          checkpoint = true;
          if (System.nanoTime() - connectedAt >= TimeUnit.SECONDS.toNanos(5)) backoff = 500;
          if (item.terminal) finish();
        }
        return item.value;
      }
    };
  }

  private Item<T> readNext() {
    for (; ; ) {
      synchronized (this) {
        if (failure != null) throw failure;
        if (stopped) return null;
      }
      try {
        Futures.await(open());
        SseDecoder current;
        ApiResponse<FileDownload> metadata;
        synchronized (this) {
          current = decoder;
          metadata = response;
        }
        SseDecoder.Frame frame = current.next();
        if (frame == null) {
          if (!resumable) {
            finish();
            return null;
          }
          reconnect();
          continue;
        }
        if (frame.type.equals("ping")) continue;
        if (frame.data.equals("[DONE]")) {
          finish();
          return null;
        }
        JsonValue json = JsonValue.parse(frame.data);
        if (frame.type.equals("error")) throw new StreamException(json, metadata.requestId());
        T value = decode.apply(json, metadata.requestId());
        return new Item<>(value, frame.id, terminal.test(value));
      } catch (CharacterCodingException error) {
        fail(new ResponseValidationException("Invalid UTF-8 in SSE response"));
      } catch (IOException error) {
        if (!recover(new ApiConnectionException("Event stream read failed", error))) return null;
      } catch (RuntimeException error) {
        if (!recover(error)) return null;
      }
    }
  }

  private boolean recover(RuntimeException error) {
    synchronized (this) {
      if (failure != null) throw failure;
      if (stopped) return false;
    }
    if (!resumable || !retryable(error)) {
      fail(error);
      throw error;
    }
    reconnect();
    return true;
  }

  private static boolean retryable(RuntimeException error) {
    if (error instanceof ApiStatusException) {
      ApiStatusException status = (ApiStatusException) error;
      int code = status.statusCode();
      if (code == 401 || code == 403 || code == 409) return false;
      List<String> hint = status.headers().get("x-should-retry");
      if (hint != null && hint.contains("false")) return false;
      return code == 408 || code == 429 || code >= 500;
    }
    return error instanceof ApiConnectionException;
  }

  private synchronized void reconnect() {
    releaseConnection();
    long delay = ThreadLocalRandom.current().nextLong(Math.max(1, backoff / 2), backoff + 1);
    backoff = Math.min(backoff * 2, 10000);
    long until = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delay);
    try {
      while (!stopped) {
        long remaining = until - System.nanoTime();
        if (remaining <= 0) break;
        TimeUnit.NANOSECONDS.timedWait(this, remaining);
      }
    } catch (InterruptedException error) {
      Thread.currentThread().interrupt();
      fail(new ApiUserAbortException("Event stream interrupted"));
    }
    if (failure != null) throw failure;
  }

  /** Uses a dedicated daemon reader which is released on completion. */
  public EventSubscription subscribe(Consumer<T> onEvent) {
    ExecutorService executor =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread thread = new Thread(r, "qca-sse-reader");
              thread.setDaemon(true);
              return thread;
            });
    try {
      return subscribe(onEvent, executor, executor);
    } catch (RuntimeException error) {
      executor.shutdownNow();
      throw error;
    }
  }

  /** Runs blocking reads and ordered callbacks on the supplied executor; SDK does not close it. */
  public EventSubscription subscribe(Consumer<T> onEvent, Executor executor) {
    return subscribe(onEvent, executor, null);
  }

  private EventSubscription subscribe(
      Consumer<T> onEvent, Executor executor, ExecutorService owned) {
    Objects.requireNonNull(onEvent, "onEvent");
    Objects.requireNonNull(executor, "executor");
    Iterator<T> iterator = iterator();
    EventSubscription subscription = new EventSubscription(this::close);
    try {
      executor.execute(
          () -> {
            try {
              while (!subscription.completion().isDone() && iterator.hasNext())
                onEvent.accept(iterator.next());
              subscription.completion().complete(null);
            } catch (Throwable error) {
              subscription.completion().completeExceptionally(error);
            } finally {
              close();
              if (owned != null) owned.shutdown();
            }
          });
    } catch (RuntimeException error) {
      close();
      subscription.completion().completeExceptionally(error);
      if (owned != null) owned.shutdown();
    }
    return subscription;
  }

  private synchronized void releaseConnection() {
    if (pending != null) pending.cancel(true);
    if (response != null) response.data().close();
    pending = null;
  }

  private synchronized void fail(RuntimeException error) {
    if (stopped) return;
    failure = error;
    finish();
  }

  private synchronized void finish() {
    if (stopped) return;
    stopped = true;
    releaseConnection();
    unwatch.run();
    notifyAll();
  }

  /**
   * Cancels pending authentication, header waits, reads and reconnect waits, without cancelling SA
   * refresh.
   */
  @Override
  public void close() {
    finish();
  }

  private static final class Item<T> {
    final T value;
    final String id;
    final boolean terminal;

    Item(T value, String id, boolean terminal) {
      this.value = value;
      this.id = id;
      this.terminal = terminal;
    }
  }
}
