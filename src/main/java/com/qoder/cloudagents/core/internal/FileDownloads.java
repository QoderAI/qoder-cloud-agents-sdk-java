package com.qoder.cloudagents.core.internal;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import java.io.*;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.*;

/** Two-hop download lifecycle; the storage hop never resolves credentials or copies API headers. */
public final class FileDownloads implements AutoCloseable {
  private final ClientEngine engine;
  private final ScheduledThreadPoolExecutor timer;
  private final Set<Operation> active = ConcurrentHashMap.newKeySet();
  private volatile boolean closed;

  FileDownloads(ClientEngine engine, ScheduledThreadPoolExecutor timer) {
    this.engine = engine;
    this.timer = timer;
  }

  public CompletableFuture<ApiResponse<FileDownload>> open(
      String[] path, RequestOptions options, long timeout, int retries) {
    Operation op = new Operation(options, timeout, retries);
    active.add(op);
    if (closed) op.fail(new ApiUserAbortException("Client closed"));
    else op.start(path);
    return op.result;
  }

  public void close() {
    closed = true;
    active.forEach(op -> op.fail(new ApiUserAbortException("Client closed")));
  }

  private final class Operation {
    final CompletableFuture<ApiResponse<FileDownload>> result = new CompletableFuture<>();
    final RequestOptions options;
    final long timeout;
    final int retries;
    CompletableFuture<?> grant;
    Call call;
    Response response;
    ScheduledFuture<?> deadline, retry, headerWait;
    boolean stopped;
    volatile RuntimeException failure;
    String apiRequestId;

    Operation(RequestOptions options, long timeout, int retries) {
      this.options = options;
      this.timeout =
          options.timeout() == null ? timeout : RequestOptions.positiveMillis(options.timeout());
      this.retries = options.maxRetries() == null ? retries : options.maxRetries();
      result.whenComplete(
          (v, e) -> {
            if (e != null) stop();
          });
    }

    synchronized void start(String[] path) {
      if (stopped) return;
      try {
        if (options.totalTimeout() != null)
          deadline =
              timer.schedule(
                  () -> fail(new ApiTimeoutException("Download total deadline exceeded")),
                  RequestOptions.positiveMillis(options.totalTimeout()),
                  TimeUnit.MILLISECONDS);
        CompletableFuture<ApiResponse<JsonValue>> pending =
            engine.get(path, Collections.emptyMap(), options);
        grant = pending;
        pending.whenComplete(
            (value, error) -> {
              if (error != null) {
                fail(Futures.unwrap(error));
                return;
              }
              try {
                apiRequestId = value.requestId();
                HttpUrl url = validate(value.data().get("url").asString());
                send(url, 0, 0);
              } catch (RuntimeException e) {
                fail(e);
              }
            });
      } catch (RuntimeException e) {
        fail(e);
      }
    }

    HttpUrl validate(String raw) {
      try {
        HttpUrl url = HttpUrl.get(raw);
        if (!url.username().isEmpty() || !url.password().isEmpty() || url.fragment() != null)
          throw new IllegalArgumentException();
        return url;
      } catch (IllegalArgumentException e) {
        throw new ResponseValidationException("Invalid file download URL");
      }
    }

    synchronized void send(HttpUrl url, int attempt, int redirects) {
      if (stopped) return;
      try {
        Request request = new Request.Builder().url(url).get().build();
        AtomicBoolean headersFinished = new AtomicBoolean();
        Call sending = engine.storageCall(request, timeout);
        call = sending;
        headerWait =
            timer.schedule(
                () -> {
                  if (headersFinished.compareAndSet(false, true)) {
                    sending.cancel();
                    retryOrFail(
                        url,
                        attempt,
                        redirects,
                        null,
                        new Headers.Builder().build(),
                        new ApiTimeoutException("Download timed out waiting for response headers"));
                  }
                },
                timeout,
                TimeUnit.MILLISECONDS);
        sending.enqueue(
            new Callback() {
              public void onFailure(Call c, IOException e) {
                if (!headersFinished.compareAndSet(false, true)) return;
                cancelHeaderWait();
                retryOrFail(
                    url, attempt, redirects, null, new Headers.Builder().build(), ioError(e));
              }

              public void onResponse(Call c, Response received) {
                if (!headersFinished.compareAndSet(false, true)) {
                  received.close();
                  return;
                }
                cancelHeaderWait();
                synchronized (Operation.this) {
                  if (stopped) {
                    received.close();
                    return;
                  }
                  response = received;
                }
                try {
                  int status = received.code();
                  if (status == 301
                      || status == 302
                      || status == 303
                      || status == 307
                      || status == 308) {
                    String location = received.header("Location");
                    HttpUrl target = location == null ? null : url.resolve(location);
                    if (target == null || redirects >= 5)
                      throw new ResponseValidationException(
                          "Invalid or excessive file download redirects");
                    target = validate(target.toString());
                    if (url.isHttps() && !target.isHttps())
                      throw new ResponseValidationException("Refusing HTTPS download downgrade");
                    received.close();
                    send(target, attempt, redirects + 1);
                    return;
                  }
                  if (!received.isSuccessful()) {
                    if (attempt < retries
                        && RetryPolicy.shouldRetry(
                            "GET", false, true, status, received.headers())) {
                      Headers headers = received.headers();
                      received.close();
                      schedule(url, attempt, redirects, headers);
                      return;
                    }
                    String text = received.body() == null ? "" : received.body().string();
                    JsonValue json;
                    try {
                      json = JsonValue.parse(text);
                    } catch (ResponseValidationException ignored) {
                      json = JsonValue.of(text);
                    }
                    throw new ApiStatusException(
                        new ApiResponse<>(json, status, received.headers().toMultimap()));
                  }
                  if (received.body() == null)
                    throw new ResponseValidationException("Missing download body");
                  InputStream source = received.body().byteStream();
                  InputStream input =
                      new InputStream() {
                        private boolean eof;

                        private void check() {
                          if (failure != null) throw failure;
                          synchronized (Operation.this) {
                            if (stopped) throw new ApiUserAbortException("Download closed");
                          }
                        }

                        public int read() throws IOException {
                          byte[] one = new byte[1];
                          int count = read(one, 0, 1);
                          return count < 0 ? -1 : one[0] & 255;
                        }

                        public int read(byte[] bytes, int offset, int length) throws IOException {
                          if (eof) return length == 0 ? 0 : -1;
                          check();
                          try {
                            int count = source.read(bytes, offset, length);
                            if (failure != null) throw failure;
                            if (count < 0) {
                              eof = true;
                              stop();
                            }
                            return count;
                          } catch (IOException e) {
                            RuntimeException mapped = failure != null ? failure : ioError(e);
                            fail(mapped);
                            throw mapped;
                          }
                        }

                        public void close() {
                          stop();
                        }
                      };
                  ApiResponse<Void> meta =
                      new ApiResponse<>(null, status, received.headers().toMultimap());
                  FileDownload download =
                      new FileDownload(input, meta, apiRequestId, Operation.this::stop);
                  if (!result.complete(meta.map(ignored -> download))) stop();
                } catch (IOException e) {
                  fail(ioError(e));
                } catch (RuntimeException e) {
                  fail(e);
                }
              }
            });
      } catch (RuntimeException e) {
        fail(e);
      }
    }

    ApiException ioError(IOException e) {
      return e instanceof InterruptedIOException
          ? new ApiTimeoutException("Download read timed out")
          : new ApiConnectionException("Download failed", e);
    }

    void retryOrFail(
        HttpUrl url,
        int attempt,
        int redirects,
        Integer status,
        Headers headers,
        ApiException error) {
      if (attempt < retries && RetryPolicy.shouldRetry("GET", false, true, status, headers))
        schedule(url, attempt, redirects, headers);
      else fail(error);
    }

    synchronized void schedule(HttpUrl url, int attempt, int redirects, Headers headers) {
      if (stopped) return;
      try {
        retry =
            timer.schedule(
                () -> send(url, attempt + 1, redirects),
                RetryPolicy.delayMillis(
                    headers, attempt, Clock.systemUTC(), ThreadLocalRandom.current().nextDouble()),
                TimeUnit.MILLISECONDS);
      } catch (RejectedExecutionException e) {
        fail(new ApiUserAbortException("Client closed"));
      }
    }

    synchronized void cancelHeaderWait() {
      if (headerWait != null) headerWait.cancel(false);
    }

    void fail(Throwable e) {
      failure =
          e instanceof RuntimeException
              ? (RuntimeException) e
              : new ApiConnectionException("Download failed", e);
      result.completeExceptionally(failure);
      stop();
    }

    synchronized void stop() {
      if (stopped) return;
      stopped = true;
      if (deadline != null) deadline.cancel(false);
      if (retry != null) retry.cancel(false);
      cancelHeaderWait();
      if (grant != null && !grant.isDone()) grant.cancel(true);
      if (call != null) call.cancel();
      if (response != null) response.close();
      active.remove(this);
    }
  }
}
