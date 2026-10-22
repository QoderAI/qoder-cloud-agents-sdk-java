package com.qoder.cloudagents.core.internal;

import com.qoder.cloudagents.core.http.HttpInterceptor;
import com.qoder.cloudagents.core.http.HttpRequest;
import com.qoder.cloudagents.core.http.HttpResponse;
import com.qoder.cloudagents.core.http.HttpTransport;
import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.*;
import okio.*;

/** Bridges public transport contracts to the existing SDK retry and streaming lifecycle. */
final class HttpExtensions implements Call {
  private final OkHttpClient client;
  private final HttpTransport transport;
  private final List<HttpInterceptor> interceptors;
  private final Request request;
  private final Call metadata;
  private final long timeout;
  private final boolean sse, replayable;
  private final List<CompletableFuture<?>> pending = new ArrayList<>();
  private final Set<HttpResponse> responses = Collections.newSetFromMap(new IdentityHashMap<>());
  private boolean executed, cancelled;

  static Call create(
      OkHttpClient client,
      HttpTransport transport,
      List<HttpInterceptor> interceptors,
      Request request,
      long timeout,
      boolean sse,
      boolean replayable) {
    OkHttpClient configured =
        client
            .newBuilder()
            .readTimeout(sse ? 0 : timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .build();
    if (transport == null && interceptors.isEmpty()) return configured.newCall(request);
    return new HttpExtensions(
        configured, transport, interceptors, request, timeout, sse, replayable);
  }

  private HttpExtensions(
      OkHttpClient client,
      HttpTransport transport,
      List<HttpInterceptor> interceptors,
      Request request,
      long timeout,
      boolean sse,
      boolean replayable) {
    this.client = client;
    this.transport = transport;
    this.interceptors = interceptors;
    this.request = request;
    this.metadata = client.newCall(request);
    this.timeout = timeout;
    this.sse = sse;
    this.replayable = replayable;
  }

  @Override
  public Request request() {
    return request;
  }

  @Override
  public synchronized boolean isExecuted() {
    return executed;
  }

  @Override
  public synchronized boolean isCanceled() {
    return cancelled;
  }

  @Override
  public Timeout timeout() {
    return Timeout.NONE;
  }

  @Override
  public void addEventListener(okhttp3.EventListener listener) {
    metadata.addEventListener(listener);
  }

  @Override
  public <T> T tag(Class<? extends T> type) {
    return metadata.tag(type);
  }

  @Override
  public <T> T tag(kotlin.reflect.KClass<T> type) {
    return metadata.tag(type);
  }

  @Override
  public <T> T tag(Class<T> type, kotlin.jvm.functions.Function0<? extends T> factory) {
    return metadata.tag(type, factory);
  }

  @Override
  public <T> T tag(
      kotlin.reflect.KClass<T> type, kotlin.jvm.functions.Function0<? extends T> factory) {
    return metadata.tag(type, factory);
  }

  @Override
  public Call clone() {
    return new HttpExtensions(client, transport, interceptors, request, timeout, sse, replayable);
  }

  @Override
  public Response execute() {
    throw new UnsupportedOperationException("SDK operations use asynchronous transport calls");
  }

  @Override
  public void enqueue(Callback callback) {
    synchronized (this) {
      if (executed) throw new IllegalStateException("Already executed");
      executed = true;
    }
    try {
      client
          .dispatcher()
          .executorService()
          .execute(
              () -> {
                try {
                  HttpRequest original = adaptRequest();
                  CompletableFuture<HttpResponse> result = invoke(0, original, original);
                  result.whenComplete(
                      (value, error) -> {
                        if (error != null) {
                          cancel();
                          callback.onFailure(this, ioFailure(error));
                          return;
                        }
                        if (isCanceled()) {
                          close(value);
                          callback.onFailure(this, new IOException("HTTP call cancelled"));
                          return;
                        }
                        Response response = null;
                        try {
                          response = adaptResponse(value);
                          callback.onResponse(this, response);
                        } catch (IOException | RuntimeException failure) {
                          if (response != null) response.close();
                          else close(value);
                          cancel();
                          callback.onFailure(this, ioFailure(failure));
                        }
                      });
                } catch (RuntimeException failure) {
                  cancel();
                  callback.onFailure(this, ioFailure(failure));
                }
              });
    } catch (RejectedExecutionException failure) {
      cancel();
      callback.onFailure(this, ioFailure(failure));
    }
  }

  private CompletableFuture<HttpResponse> invoke(
      int index, HttpRequest next, HttpRequest original) {
    if (isCanceled()) return failed(new CancellationException());
    validate(next, original);
    CompletableFuture<HttpResponse> result;
    if (index == interceptors.size())
      result = transport == null ? send(next) : transport.execute(next);
    else {
      AtomicBoolean proceeded = new AtomicBoolean();
      result =
          interceptors
              .get(index)
              .intercept(
                  next,
                  modified -> {
                    if (!proceeded.compareAndSet(false, true))
                      throw new IllegalStateException(
                          "An interceptor may proceed only once per attempt");
                    return invoke(index + 1, modified, original);
                  });
    }
    if (result == null) throw new IllegalStateException("HTTP extension returned no future");
    track(result);
    CompletableFuture<HttpResponse> checked =
        result.thenApply(
            response -> {
              if (response == null)
                throw new IllegalStateException("HTTP extension returned no response");
              return response;
            });
    track(checked);
    return checked;
  }

  private synchronized void track(CompletableFuture<HttpResponse> future) {
    pending.add(future);
    future.whenComplete(
        (response, error) -> {
          if (response != null) {
            boolean release;
            synchronized (HttpExtensions.this) {
              release = cancelled;
              if (!release) responses.add(response);
            }
            if (release) close(response);
          }
        });
    if (cancelled) future.cancel(true);
  }

  private void validate(HttpRequest value, HttpRequest original) {
    if (value == null
        || !value.uri().equals(original.uri())
        || !value.method().equals(original.method())
        || value.body() != original.body()
        || !value.readTimeout().equals(original.readTimeout())
        || !value.writeTimeout().equals(original.writeTimeout()))
      throw new IllegalArgumentException(
          "Interceptors may change headers, not routing, body or timeouts");
    for (String header :
        Arrays.asList(
            "Authorization", "Host", "Idempotency-Key", "Content-Length", "Transfer-Encoding"))
      if (!Objects.equals(
          value.headers().get(header.toLowerCase(Locale.ROOT)),
          original.headers().get(header.toLowerCase(Locale.ROOT))))
        throw new IllegalArgumentException("Interceptor changed a protected header");
  }

  private HttpRequest adaptRequest() {
    RequestBody source = request.body();
    HttpRequest.Body body =
        source == null
            ? null
            : new HttpRequest.Body() {
              public String contentType() {
                return source.contentType() == null ? null : source.contentType().toString();
              }

              public long contentLength() throws IOException {
                return source.contentLength();
              }

              public boolean replayable() {
                return replayable;
              }

              public void writeTo(OutputStream destination) throws IOException {
                BufferedSink sink = Okio.buffer(Okio.sink(destination));
                source.writeTo(sink);
                sink.flush();
              }
            };
    return new HttpRequest(
        request.method(),
        request.url().uri(),
        request.headers().toMultimap(),
        body,
        Duration.ofMillis(timeout),
        sse);
  }

  private CompletableFuture<HttpResponse> send(HttpRequest value) {
    Headers.Builder headers = new Headers.Builder();
    value.headers().forEach((name, values) -> values.forEach(v -> headers.add(name, v)));
    Call call = client.newCall(request.newBuilder().headers(headers.build()).build());
    CompletableFuture<HttpResponse> future = new CompletableFuture<>();
    future.whenComplete(
        (response, error) -> {
          if (future.isCancelled()) call.cancel();
        });
    call.enqueue(
        new Callback() {
          public void onFailure(Call ignored, IOException error) {
            future.completeExceptionally(error);
          }

          public void onResponse(Call ignored, Response response) {
            InputStream stream =
                response.body() == null
                    ? new ByteArrayInputStream(new byte[0])
                    : response.body().byteStream();
            HttpResponse converted =
                new HttpResponse(response.code(), response.headers().toMultimap(), stream);
            if (!future.complete(converted)) {
              close(converted);
              response.close();
            }
          }
        });
    return future;
  }

  private Response adaptResponse(HttpResponse value) {
    Headers.Builder headers = new Headers.Builder();
    value.headers().forEach((name, values) -> values.forEach(v -> headers.add(name, v)));
    InputStream stream =
        new FilterInputStream(value.body()) {
          @Override
          public void close() throws IOException {
            try {
              value.close();
            } finally {
              // Also release downstream responses replaced by an interceptor, including retries.
              HttpExtensions.this.cancel();
            }
          }
        };
    ResponseBody body =
        new ResponseBody() {
          private final BufferedSource source = Okio.buffer(Okio.source(stream));

          public MediaType contentType() {
            String type = value.header("Content-Type");
            return type == null ? null : MediaType.parse(type);
          }

          public long contentLength() {
            try {
              return Long.parseLong(value.header("Content-Length"));
            } catch (NumberFormatException ignored) {
              return -1;
            }
          }

          public BufferedSource source() {
            return source;
          }
        };
    return new Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(value.statusCode())
        .message("")
        .headers(headers.build())
        .body(body)
        .build();
  }

  @Override
  public void cancel() {
    List<CompletableFuture<?>> futures;
    List<HttpResponse> bodies;
    synchronized (this) {
      if (cancelled) return;
      cancelled = true;
      futures = new ArrayList<>(pending);
      bodies = new ArrayList<>(responses);
      responses.clear();
    }
    futures.forEach(future -> future.cancel(true));
    bodies.forEach(HttpExtensions::close);
  }

  private static IOException ioFailure(Throwable error) {
    while ((error instanceof CompletionException || error instanceof ExecutionException)
        && error.getCause() != null) error = error.getCause();
    return error instanceof IOException
        ? (IOException) error
        : new IOException("HTTP extension failed", error);
  }

  private static CompletableFuture<HttpResponse> failed(Throwable error) {
    CompletableFuture<HttpResponse> future = new CompletableFuture<>();
    future.completeExceptionally(error);
    return future;
  }

  private static void close(HttpResponse response) {
    try {
      response.close();
    } catch (IOException ignored) {
    }
  }
}
