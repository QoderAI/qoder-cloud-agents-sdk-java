package com.qoder.cloudagents.core.http;

import com.qoder.cloudagents.core.ApiResponse;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable single-attempt request. Headers and URL may contain secrets; toString is redacted. */
public final class HttpRequest {
  private final String method;
  private final URI uri;
  private final Map<String, List<String>> headers;
  private final Body body;
  private final Duration timeout;
  private final boolean eventStream;

  public HttpRequest(
      String method,
      URI uri,
      Map<String, List<String>> headers,
      Body body,
      Duration timeout,
      boolean eventStream) {
    if (method == null || uri == null || headers == null)
      throw new ConfigurationException("HTTP request metadata must not be null");
    RequestOptions.positiveMillis(timeout);
    this.method = method;
    this.uri = uri;
    this.headers = new ApiResponse<Void>(null, 0, headers).headers();
    this.body = body;
    this.timeout = timeout;
    this.eventStream = eventStream;
  }

  public String method() {
    return method;
  }

  public URI uri() {
    return uri;
  }

  public Map<String, List<String>> headers() {
    return headers;
  }

  public String header(String name) {
    List<String> values = headers.get(name.toLowerCase(java.util.Locale.ROOT));
    return values == null || values.isEmpty() ? null : values.get(0);
  }

  public Body body() {
    return body;
  }

  public Duration connectTimeout() {
    return Duration.ofSeconds(5);
  }

  /** Zero disables read-idle timeout for SSE; header and total deadlines remain SDK-owned. */
  public Duration readTimeout() {
    return eventStream ? Duration.ZERO : timeout;
  }

  public Duration writeTimeout() {
    return timeout;
  }

  /** Adds or replaces an observability header without modifying routing or authentication. */
  public HttpRequest withHeader(String name, String value) {
    RequestOptions.builder().header(name, value);
    if (name.equalsIgnoreCase("Host")
        || name.equalsIgnoreCase("Idempotency-Key")
        || name.equalsIgnoreCase("Content-Length")
        || name.equalsIgnoreCase("Transfer-Encoding"))
      throw new ConfigurationException("This header is controlled by the request or transport");
    Map<String, List<String>> copy = new LinkedHashMap<>(headers);
    copy.put(name.toLowerCase(java.util.Locale.ROOT), Collections.singletonList(value));
    return new HttpRequest(method, uri, copy, body, timeout, eventStream);
  }

  @Override
  public String toString() {
    return "HttpRequest([redacted])";
  }

  /** Stream a body without buffering uploads; do not close the supplied destination. */
  public interface Body {
    String contentType();

    long contentLength() throws IOException;

    boolean replayable();

    void writeTo(OutputStream destination) throws IOException;
  }
}
