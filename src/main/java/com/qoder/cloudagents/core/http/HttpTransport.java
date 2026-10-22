package com.qoder.cloudagents.core.http;

import java.util.concurrent.CompletableFuture;

/**
 * Caller-owned HTTP transport for API requests and isolated storage downloads. Implementations must
 * be thread-safe, return promptly, honor request timeouts, and abort I/O when the returned future
 * is cancelled. Perform exactly one attempt without redirects, authentication or retries. The SDK
 * owns response bodies, but never closes this transport. WebSockets and SA Key exchange use their
 * own transports.
 */
@FunctionalInterface
public interface HttpTransport {
  CompletableFuture<HttpResponse> execute(HttpRequest request);
}
