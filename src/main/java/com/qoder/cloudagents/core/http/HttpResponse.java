package com.qoder.cloudagents.core.http;

import com.qoder.cloudagents.core.ApiResponse;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/** A response whose streaming body is transferred to the SDK. Closing is idempotent. */
public final class HttpResponse implements Closeable {
  private final ApiResponse<Void> metadata;
  private final InputStream body;
  private final AtomicBoolean closed = new AtomicBoolean();

  public HttpResponse(int statusCode, Map<String, List<String>> headers, InputStream body) {
    if (statusCode < 100 || statusCode > 599 || headers == null || body == null)
      throw new ConfigurationException("Invalid HTTP response");
    metadata = new ApiResponse<>(null, statusCode, headers);
    this.body = body;
  }

  public int statusCode() {
    return metadata.statusCode();
  }

  public Map<String, List<String>> headers() {
    return metadata.headers();
  }

  public String header(String name) {
    return metadata.header(name);
  }

  public InputStream body() {
    return body;
  }

  @Override
  public void close() throws IOException {
    if (closed.compareAndSet(false, true)) body.close();
  }

  @Override
  public String toString() {
    return "HttpResponse(" + statusCode() + ", [redacted])";
  }
}
