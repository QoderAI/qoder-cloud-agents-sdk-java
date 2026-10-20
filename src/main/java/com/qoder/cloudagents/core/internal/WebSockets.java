package com.qoder.cloudagents.core.internal;

import com.qoder.cloudagents.auth.Credential;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.*;
import okio.ByteString;

/** Authentication is resolved once per explicit connection; never replay an upgrade. */
final class WebSockets {
  private WebSockets() {}

  static CompletableFuture<WebSocket> open(
      HttpUrl baseUrl,
      Credential credential,
      Map<String, String> defaults,
      OkHttpClient transport,
      String id,
      RequestOptions options,
      Duration handshakeTimeout,
      Duration sendTimeout,
      WebSocketListener listener) {
    CompletableFuture<WebSocket> result = new CompletableFuture<>();
    CompletableFuture<String> token;
    try {
      token = credential.getToken();
      if (token == null) throw new ConfigurationException("Credential returned no future");
    } catch (Throwable e) {
      return Futures.failed(e);
    }
    token.whenComplete(
        (value, error) -> {
          if (result.isDone()) return;
          if (error != null) {
            result.completeExceptionally(Futures.unwrap(error));
            return;
          }
          try {
            HttpUrl url =
                baseUrl
                    .newBuilder()
                    .addPathSegment("realtime")
                    .addQueryParameter("conversation_id", id)
                    .build();
            Request.Builder request =
                new Request.Builder()
                    .url(url)
                    .header("User-Agent", "qca-java/" + Version.CURRENT)
                    .header("X-Qoder-Lang", "java")
                    .header("X-Qoder-Package-Version", Version.CURRENT);
            defaults.forEach((k, v) -> header(request, k, v));
            options.headers().forEach((k, v) -> header(request, k, v));
            request.header("Authorization", "Bearer " + Authentication.validateToken(value));
            AtomicReference<ApiStatusException> firstStatus = new AtomicReference<>();
            OkHttpClient.Builder builder =
                transport
                    .newBuilder()
                    .connectTimeout(
                        RequestOptions.positiveMillis(handshakeTimeout), TimeUnit.MILLISECONDS)
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .writeTimeout(
                        RequestOptions.positiveMillis(sendTimeout), TimeUnit.MILLISECONDS);
            // WebSockets skip network interceptors. Cancel any library follow-up before it is sent.
            builder.addInterceptor(
                chain -> {
                  chain
                      .call()
                      .addEventListener(
                          new okhttp3.EventListener() {
                            @Override
                            public void followUpDecision(
                                Call call, Response response, Request followUp) {
                              if (followUp != null) {
                                firstStatus.compareAndSet(
                                    null,
                                    new ApiStatusException(
                                        new ApiResponse<>(
                                            JsonValue.of(null),
                                            response.code(),
                                            response.headers().toMultimap())));
                                call.cancel();
                              }
                            }
                          });
                  return chain.proceed(chain.request());
                });
            WebSocket socket =
                builder
                    .build()
                    .newWebSocket(
                        request.build(),
                        new WebSocketListener() {
                          @Override
                          public void onOpen(WebSocket ws, Response r) {
                            listener.onOpen(ws, r);
                          }

                          @Override
                          public void onMessage(WebSocket ws, String text) {
                            listener.onMessage(ws, text);
                          }

                          @Override
                          public void onMessage(WebSocket ws, ByteString data) {
                            listener.onMessage(ws, data);
                          }

                          @Override
                          public void onClosing(WebSocket ws, int code, String reason) {
                            listener.onClosing(ws, code, reason);
                          }

                          @Override
                          public void onClosed(WebSocket ws, int code, String reason) {
                            listener.onClosed(ws, code, reason);
                          }

                          @Override
                          public void onFailure(WebSocket ws, Throwable failure, Response r) {
                            Throwable translated = firstStatus.get();
                            if (translated == null && r != null && r.code() != 101)
                              translated = status(r);
                            listener.onFailure(ws, translated == null ? failure : translated, r);
                          }
                        });
            if (!result.complete(socket)) socket.cancel();
          } catch (Throwable failure) {
            result.completeExceptionally(failure);
          }
        });
    // Cancelling this wait deliberately does not cancel a shared SA token refresh.
    return result;
  }

  private static void header(Request.Builder request, String key, String value) {
    String k = key.toLowerCase(Locale.ROOT);
    if (k.startsWith("sec-websocket-")
        || Arrays.asList("host", "connection", "upgrade", "content-length").contains(k))
      throw new ConfigurationException("WebSocket handshake headers are transport-owned");
    request.header(key, value);
  }

  private static ApiStatusException status(Response response) {
    JsonValue body = JsonValue.of(null);
    try {
      String text = response.peekBody(65536).string();
      try {
        body = JsonValue.parse(text);
      } catch (ResponseValidationException e) {
        body = JsonValue.of(text);
      }
    } catch (IOException | IllegalStateException ignored) {
    }
    return new ApiStatusException(
        new ApiResponse<>(body, response.code(), response.headers().toMultimap()));
  }
}
