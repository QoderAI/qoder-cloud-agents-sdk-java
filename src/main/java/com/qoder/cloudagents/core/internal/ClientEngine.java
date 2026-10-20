package com.qoder.cloudagents.core.internal;

import com.qoder.cloudagents.auth.Credential;
import com.qoder.cloudagents.core.ApiResponse;
import com.qoder.cloudagents.core.FileDownload;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.errors.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import okhttp3.*;

/**
 * Internal shared asynchronous engine. Synchronous resources await the same cancellable operation.
 */
public final class ClientEngine implements AutoCloseable {
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  private final HttpUrl baseUrl;
  private final Credential credential;
  private final long timeoutMillis;
  private final int maxRetries;
  private final Map<String, String> defaultHeaders;
  private final OkHttpClient transport = Transports.create();
  private final ScheduledThreadPoolExecutor timer =
      new ScheduledThreadPoolExecutor(1, Transports.threads("qca-timer"));
  private final Set<Operation<?>> active = ConcurrentHashMap.newKeySet();
  private final Set<Consumer<RuntimeException>> streams = ConcurrentHashMap.newKeySet();
  private volatile boolean closed;
  private final FileDownloads downloads = new FileDownloads(this, transport, timer);

  public ClientEngine(
      HttpUrl baseUrl,
      Credential credential,
      Duration timeout,
      int maxRetries,
      Map<String, String> defaultHeaders) {
    this.baseUrl = baseUrl;
    this.credential = credential;
    timeoutMillis = RequestOptions.positiveMillis(timeout);
    this.maxRetries = maxRetries;
    this.defaultHeaders = Collections.unmodifiableMap(new LinkedHashMap<>(defaultHeaders));
    timer.setRemoveOnCancelPolicy(true);
  }

  public CompletableFuture<ApiResponse<JsonValue>> get(
      String[] path, Map<String, String> query, RequestOptions options) {
    return request("GET", path, query, null, true, options);
  }

  /**
   * Request bodies are explicit about replay safety; non-repeatable uploads must set
   * replayable=false.
   */
  public CompletableFuture<ApiResponse<JsonValue>> request(
      String method,
      String[] path,
      Map<String, String> query,
      JsonValue body,
      boolean replayable,
      RequestOptions options) {
    if (closed) return Futures.failed(new ConfigurationException("Client is closed"));
    return requestBody(
        method,
        path,
        query,
        body == null ? null : RequestBody.create(body.toJson(), JSON),
        replayable,
        options);
  }

  public CompletableFuture<ApiResponse<JsonValue>> requestBody(
      String method,
      String[] path,
      Map<String, String> query,
      RequestBody body,
      boolean replayable,
      RequestOptions options) {
    if (closed) return Futures.failed(new ConfigurationException("Client is closed"));
    return launch(
        new Operation<>(
            method,
            path.clone(),
            query,
            body,
            replayable,
            options,
            false,
            (op, response) -> decodeJson(response),
            null));
  }

  /** Per-call SA Key authentication, restricted to the three Forward Token management routes. */
  public CompletableFuture<ApiResponse<JsonValue>> serviceAccountToken(
      String[] path, String saKey, JsonValue body, RequestOptions options) {
    if (options == null) throw new ConfigurationException("Request options must not be null");
    Authentication.validateToken(saKey);
    boolean allowed =
        Arrays.equals(path, new String[] {"service_account_tokens"})
            || (path.length == 3
                && path[0].equals("identities")
                && path[2].equals("service_account_tokens"))
            || (path.length == 3
                && path[0].equals("service_account_tokens")
                && path[2].equals("revoke"));
    if (!allowed)
      throw new ConfigurationException("SA Key is restricted to Token management routes");
    if (closed) return Futures.failed(new ConfigurationException("Client is closed"));
    return launch(
        new Operation<>(
            "POST",
            path.clone(),
            Collections.emptyMap(),
            RequestBody.create(body.toJson(), JSON),
            false,
            options.toBuilder().maxRetries(0).header("Content-Type", "application/json").build(),
            false,
            (op, response) -> decodeJson(response),
            saKey));
  }

  /** Direct authenticated binary response, unlike the separate File storage grant. */
  public CompletableFuture<ApiResponse<FileDownload>> downloadContent(
      String[] path, RequestOptions options) {
    if (closed) return Futures.failed(new ConfigurationException("Client is closed"));
    return launch(
        new Operation<>(
            "GET",
            path.clone(),
            Collections.emptyMap(),
            null,
            true,
            options,
            true,
            (op, response) -> op.openDownload(response),
            null));
  }

  public String defaultHeader(String name) {
    String value = null;
    for (Map.Entry<String, String> entry : defaultHeaders.entrySet())
      if (entry.getKey().equalsIgnoreCase(name)) value = entry.getValue();
    return value;
  }

  /** One authenticated WebSocket upgrade, using the same credential and owned transport. */
  public CompletableFuture<WebSocket> webSocket(
      String conversationId,
      RequestOptions options,
      Duration handshakeTimeout,
      Duration sendTimeout,
      WebSocketListener listener) {
    if (closed) return Futures.failed(new ApiUserAbortException("Client closed"));
    return WebSockets.open(
        baseUrl,
        credential,
        defaultHeaders,
        transport,
        conversationId,
        options,
        handshakeTimeout,
        sendTimeout,
        listener);
  }

  public ScheduledFuture<?> schedule(Runnable action, long millis) {
    return timer.schedule(action, millis, TimeUnit.MILLISECONDS);
  }

  /**
   * Internal SSE transport: authentication and header timeout remain shared with ordinary calls.
   */
  public CompletableFuture<ApiResponse<FileDownload>> eventStream(
      String[] path, Map<String, List<String>> query, RequestOptions options) {
    if (closed) return Futures.failed(new ApiUserAbortException("Client closed"));
    Operation<FileDownload> operation =
        new Operation<>(
            "GET",
            path.clone(),
            Collections.emptyMap(),
            null,
            true,
            options,
            true,
            (op, response) -> {
              String contentType = response.header("Content-Type", "").split(";", 2)[0].trim();
              if (!contentType.equalsIgnoreCase("text/event-stream"))
                throw new ResponseValidationException("Expected text/event-stream response");
              return op.openDownload(response);
            },
            null);
    operation.sse = true;
    query.forEach((key, values) -> operation.repeatedQuery.put(key, new ArrayList<>(values)));
    return launch(operation);
  }

  /** Tracks the whole subscription, including reconnect waits and credential lookup. */
  public Runnable watchStream(Consumer<RuntimeException> stop, Duration totalTimeout) {
    streams.add(stop);
    ScheduledFuture<?> deadline = null;
    try {
      if (totalTimeout != null)
        deadline =
            timer.schedule(
                () -> stop.accept(new ApiTimeoutException("Total stream deadline exceeded")),
                RequestOptions.positiveMillis(totalTimeout),
                TimeUnit.MILLISECONDS);
      if (closed) stop.accept(new ApiUserAbortException("Client closed"));
    } catch (RejectedExecutionException e) {
      stop.accept(new ApiUserAbortException("Client closed"));
    }
    ScheduledFuture<?> task = deadline;
    return () -> {
      streams.remove(stop);
      if (task != null) task.cancel(false);
    };
  }

  private <T> CompletableFuture<ApiResponse<T>> launch(Operation<T> operation) {
    active.add(operation);
    operation.result.whenComplete(
        (value, error) -> {
          if (!operation.streaming || error != null) operation.dispose();
        });
    if (closed) operation.fail(new ApiUserAbortException("Client closed"));
    else operation.start();
    return operation.result;
  }

  private static JsonValue decodeJson(Response response) throws IOException {
    String text = response.body() == null ? "" : response.body().string();
    try {
      return response.code() == 204 || response.code() == 205
          ? JsonValue.of(null)
          : JsonValue.parse(text);
    } catch (ResponseValidationException error) {
      if (response.isSuccessful()) throw error;
      return JsonValue.of(text);
    }
  }

  private interface Decoder<T> {
    T read(Operation<T> operation, Response response) throws IOException;
  }

  public CompletableFuture<ApiResponse<com.qoder.cloudagents.core.FileDownload>> download(
      String[] path, RequestOptions options) {
    return downloads.open(path, options, timeoutMillis, maxRetries);
  }

  @Override
  public void close() {
    closed = true;
    downloads.close();
    streams.forEach(stop -> stop.accept(new ApiUserAbortException("Client closed")));
    active.forEach(operation -> operation.fail(new ApiUserAbortException("Client closed")));
    timer.shutdownNow();
    Transports.close(transport);
  }

  private final class Operation<T> {
    final CompletableFuture<ApiResponse<T>> result = new CompletableFuture<>();
    final String method;
    final String[] path;
    final Map<String, String> query;
    final RequestBody body;
    final boolean replayable;
    final RequestOptions options;
    final long timeout;
    final int retries;
    final boolean streaming;
    final Decoder<T> decoder;
    final String saKey;
    boolean sse;
    final Map<String, List<String>> repeatedQuery = new LinkedHashMap<>();
    volatile RuntimeException streamFailure;
    boolean disposed;
    Response ownedResponse;
    Call call;
    ScheduledFuture<?> deadline, retry, headerWait;

    Operation(
        String method,
        String[] path,
        Map<String, String> query,
        RequestBody body,
        boolean replayable,
        RequestOptions options,
        boolean streaming,
        Decoder<T> decoder,
        String saKey) {
      this.saKey = saKey;
      this.streaming = streaming;
      this.decoder = decoder;
      this.method = method;
      this.path = path;
      this.query = new LinkedHashMap<>(query);
      this.body = body;
      this.replayable = replayable;
      this.options = options;
      timeout =
          options.timeout() == null
              ? timeoutMillis
              : RequestOptions.positiveMillis(options.timeout());
      retries = options.maxRetries() == null ? maxRetries : options.maxRetries();
    }

    void start() {
      synchronized (this) {
        if (result.isDone()) return;
        try {
          if (options.totalTimeout() != null)
            deadline =
                timer.schedule(
                    () -> fail(new ApiTimeoutException("Total request deadline exceeded")),
                    RequestOptions.positiveMillis(options.totalTimeout()),
                    TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
          result.completeExceptionally(new ApiUserAbortException("Client closed"));
          return;
        }
      }
      attempt(0);
    }

    void attempt(int number) {
      if (result.isDone()) return;
      try {
        CompletableFuture<String> token =
            saKey == null ? credential.getToken() : CompletableFuture.completedFuture(saKey);
        if (token == null) throw new ConfigurationException("Credential returned no future");
        // Do not cancel a provider's future: it may represent a shared SA refresh.
        token.whenComplete(
            (value, error) -> {
              if (result.isDone()) return;
              if (error != null) result.completeExceptionally(Futures.unwrap(error));
              else {
                try {
                  send(Authentication.validateToken(value), number);
                } catch (RuntimeException failure) {
                  result.completeExceptionally(
                      failure instanceof QcaException
                          ? failure
                          : new ConfigurationException("Invalid request configuration"));
                }
              }
            });
      } catch (RuntimeException error) {
        result.completeExceptionally(error);
      }
    }

    void send(String token, int number) {
      HttpUrl.Builder url = baseUrl.newBuilder();
      for (String part : path) {
        if (part == null || part.isEmpty() || part.equals(".") || part.equals(".."))
          throw new ConfigurationException("Invalid API path segment");
        url.addPathSegment(part);
      }
      Map<String, String> parameters = new LinkedHashMap<>(query);
      parameters.putAll(options.query());
      parameters.forEach(
          (key, value) -> {
            if (method.equals("GET")
                && Arrays.equals(path, new String[] {"drives", "entries"})
                && key.equals("path")) {
              try {
                // Drive's protocol requires literal path separators after query overrides.
                url.addEncodedQueryParameter(
                    "path", URLEncoder.encode(value, "UTF-8").replace("%2F", "/"));
              } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
              }
            } else url.addQueryParameter(key, value);
          });
      repeatedQuery.forEach(
          (key, values) -> {
            if (!parameters.containsKey(key))
              values.forEach(value -> url.addQueryParameter(key, value));
          });
      Request.Builder builder =
          new Request.Builder()
              .url(url.build())
              .header(
                  "Accept",
                  sse ? "text/event-stream" : streaming ? "application/zip" : "application/json")
              .header("User-Agent", "qca-java/" + Version.CURRENT)
              .header("X-Qoder-Lang", "java")
              .header("X-Qoder-Package-Version", Version.CURRENT);
      defaultHeaders.forEach(builder::header);
      options.headers().forEach(builder::header);
      builder
          .header("Authorization", "Bearer " + token)
          .header("X-Qoder-Retry-Count", Integer.toString(number))
          .header("X-Qoder-Timeout", Long.toString(timeout / 1000));
      RequestBody requestBody = body;
      if (body instanceof MultipartBody) builder.removeHeader("Content-Type");
      if (requestBody == null
          && (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")))
        requestBody = RequestBody.create(new byte[0], null);
      if (sse) builder.header("Accept", "text/event-stream");
      builder.method(method, requestBody);
      Request request = builder.build();
      AtomicBoolean headersFinished = new AtomicBoolean();
      final Call attemptCall =
          transport
              .newBuilder()
              .readTimeout(sse ? 0 : timeout, TimeUnit.MILLISECONDS)
              .writeTimeout(timeout, TimeUnit.MILLISECONDS)
              .build()
              .newCall(request);
      synchronized (this) {
        if (result.isDone()) {
          attemptCall.cancel();
          return;
        }
        call = attemptCall;
        headerWait =
            timer.schedule(
                () -> {
                  if (headersFinished.compareAndSet(false, true)) {
                    attemptCall.cancel();
                    // Queued OkHttp calls may not deliver onFailure until a dispatcher slot opens.
                    retryOrFail(
                        number,
                        request,
                        null,
                        new Headers.Builder().build(),
                        new ApiTimeoutException("Request timed out waiting for response headers"));
                  }
                },
                timeout,
                TimeUnit.MILLISECONDS);
        attemptCall.enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException error) {
                if (!headersFinished.compareAndSet(false, true)) return;
                cancelHeaderWait();
                if (result.isDone()) return;
                ApiException failure =
                    error instanceof InterruptedIOException
                        ? new ApiTimeoutException("Request timed out")
                        : new ApiConnectionException("Connection failed", error);
                retryOrFail(number, request, null, new Headers.Builder().build(), failure);
              }

              @Override
              public void onResponse(Call call, Response response) {
                if (!headersFinished.compareAndSet(false, true)) {
                  response.close();
                  return;
                }
                cancelHeaderWait();
                boolean retained = false;
                try {
                  if (result.isDone()) return;
                  if (sse && !response.isSuccessful() && response.body() != null)
                    response.body().source().timeout().timeout(timeout, TimeUnit.MILLISECONDS);
                  if (!response.isSuccessful()
                      && !(sse && (response.code() == 401 || response.code() == 403))
                      && number < retries
                      && RetryPolicy.shouldRetry(
                          method,
                          hasIdempotency(request),
                          replayable,
                          response.code(),
                          response.headers())) {
                    scheduleRetry(number, response.headers());
                    return;
                  }
                  if (response.isSuccessful()) {
                    if (streaming) {
                      synchronized (Operation.this) {
                        if (disposed) return;
                        ownedResponse = response;
                      }
                    }
                    T data = decoder.read(Operation.this, response);
                    boolean delivered =
                        result.complete(
                            new ApiResponse<>(
                                data, response.code(), response.headers().toMultimap()));
                    retained = streaming && delivered;
                    if (streaming && !delivered) dispose();
                  } else {
                    fail(
                        new ApiStatusException(
                            new ApiResponse<>(
                                decodeJson(response),
                                response.code(),
                                response.headers().toMultimap())));
                  }
                } catch (IOException error) {
                  ApiException failure =
                      error instanceof InterruptedIOException
                          ? new ApiTimeoutException("Response read timed out")
                          : new ApiConnectionException("Response read failed", error);
                  retryOrFail(
                      number,
                      request,
                      response.isSuccessful() ? null : response.code(),
                      response.headers(),
                      failure);
                } catch (RuntimeException error) {
                  fail(error);
                } finally {
                  if (!retained) response.close();
                }
              }
            });
      }
    }

    FileDownload openDownload(Response response) {
      if (response.body() == null) throw new ResponseValidationException("Missing response body");
      InputStream source = response.body().byteStream();
      InputStream input =
          new InputStream() {
            private boolean eof;

            public int read() throws IOException {
              byte[] one = new byte[1];
              int n = read(one, 0, 1);
              return n < 0 ? -1 : one[0] & 255;
            }

            public int read(byte[] bytes, int offset, int length) throws IOException {
              if (eof) return length == 0 ? 0 : -1;
              if (streamFailure != null) throw streamFailure;
              synchronized (Operation.this) {
                if (disposed) throw new ApiUserAbortException("Response stream closed");
              }
              try {
                int n = source.read(bytes, offset, length);
                if (streamFailure != null) throw streamFailure;
                if (n < 0) {
                  eof = true;
                  dispose();
                }
                return n;
              } catch (IOException e) {
                RuntimeException mapped =
                    streamFailure != null
                        ? streamFailure
                        : e instanceof InterruptedIOException
                            ? new ApiTimeoutException("Response stream read timed out")
                            : new ApiConnectionException("Response stream read failed", e);
                fail(mapped);
                throw mapped;
              }
            }

            public void close() {
              dispose();
            }
          };
      ApiResponse<Void> metadata =
          new ApiResponse<>(null, response.code(), response.headers().toMultimap());
      return new FileDownload(input, metadata, metadata.requestId(), this::dispose);
    }

    void fail(RuntimeException error) {
      streamFailure = error;
      result.completeExceptionally(error);
      dispose();
    }

    boolean hasIdempotency(Request request) {
      String key = request.header("Idempotency-Key");
      return key != null && !key.isEmpty();
    }

    void retryOrFail(
        int number, Request request, Integer status, Headers headers, ApiException error) {
      if (result.isDone()) return;
      if (number < retries
          && RetryPolicy.shouldRetry(method, hasIdempotency(request), replayable, status, headers))
        scheduleRetry(number, headers);
      else result.completeExceptionally(error);
    }

    synchronized void scheduleRetry(int number, Headers headers) {
      if (result.isDone()) return;
      try {
        retry =
            timer.schedule(
                () -> attempt(number + 1),
                RetryPolicy.delayMillis(
                    headers, number, Clock.systemUTC(), ThreadLocalRandom.current().nextDouble()),
                TimeUnit.MILLISECONDS);
      } catch (RejectedExecutionException error) {
        result.completeExceptionally(new ApiUserAbortException("Client closed"));
      }
    }

    synchronized void cancelHeaderWait() {
      if (headerWait != null) headerWait.cancel(false);
    }

    synchronized void dispose() {
      if (disposed) return;
      disposed = true;
      active.remove(this);
      if (deadline != null) deadline.cancel(false);
      if (retry != null) retry.cancel(false);
      cancelHeaderWait();
      if (call != null) call.cancel();
      if (ownedResponse != null) ownedResponse.close();
    }
  }
}
