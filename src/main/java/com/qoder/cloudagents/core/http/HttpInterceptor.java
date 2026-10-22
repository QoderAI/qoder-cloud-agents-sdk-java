package com.qoder.cloudagents.core.http;

import java.util.concurrent.CompletableFuture;

/**
 * Wraps each authenticated API attempt, including SSE handshakes and SAT management. Does not
 * intercept storage downloads, WebSockets or SA Key exchange. Return promptly; never log secrets or
 * consume a streaming body for logging. Interceptors run in registration order.
 */
@FunctionalInterface
public interface HttpInterceptor {
  CompletableFuture<HttpResponse> intercept(HttpRequest request, Chain chain);

  interface Chain {
    /** Proceed at most once, optionally adding headers with HttpRequest.withHeader. */
    CompletableFuture<HttpResponse> proceed(HttpRequest request);
  }
}
