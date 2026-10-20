package com.qoder.cloudagents.core;

import com.qoder.cloudagents.errors.ConfigurationException;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable per-call overrides. A total timeout includes credential lookup, retries and body reads.
 */
public final class RequestOptions {
  public static final RequestOptions DEFAULT = builder().build();
  private final Duration timeout;
  private final Duration totalTimeout;
  private final Integer maxRetries;
  private final Map<String, String> headers;
  private final Map<String, String> query;

  private RequestOptions(Builder b) {
    timeout = b.timeout;
    totalTimeout = b.totalTimeout;
    maxRetries = b.maxRetries;
    headers = Collections.unmodifiableMap(new LinkedHashMap<>(b.headers));
    query = Collections.unmodifiableMap(new LinkedHashMap<>(b.query));
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.timeout = timeout;
    builder.totalTimeout = totalTimeout;
    builder.maxRetries = maxRetries;
    builder.headers.putAll(headers);
    builder.query.putAll(query);
    return builder;
  }

  public Duration timeout() {
    return timeout;
  }

  public Duration totalTimeout() {
    return totalTimeout;
  }

  public Integer maxRetries() {
    return maxRetries;
  }

  public Map<String, String> headers() {
    return headers;
  }

  public Map<String, String> query() {
    return query;
  }

  public static long positiveMillis(Duration value) {
    try {
      long ms = value.toMillis();
      if (ms <= 0 || ms > Integer.MAX_VALUE) throw new ArithmeticException();
      return ms;
    } catch (ArithmeticException | NullPointerException e) {
      throw new ConfigurationException("Timeout must be between 1 and 2147483647 milliseconds");
    }
  }

  public static final class Builder {
    private Duration timeout, totalTimeout;
    private Integer maxRetries;
    private final Map<String, String> headers = new LinkedHashMap<>(),
        query = new LinkedHashMap<>();

    public Builder timeout(Duration value) {
      positiveMillis(value);
      timeout = value;
      return this;
    }

    public Builder totalTimeout(Duration value) {
      positiveMillis(value);
      totalTimeout = value;
      return this;
    }

    public Builder maxRetries(int value) {
      if (value < 0) throw new ConfigurationException("maxRetries must not be negative");
      maxRetries = value;
      return this;
    }

    public Builder header(String name, String value) {
      if (name == null || value == null || !name.matches("[!#$%&'*+.^_`|~0-9A-Za-z-]+"))
        throw new ConfigurationException("Invalid request header");
      for (int i = 0; i < value.length(); i++) {
        char c = value.charAt(i);
        if ((c < 32 && c != '\t') || c >= 127)
          throw new ConfigurationException("Invalid request header");
      }
      if (name.equalsIgnoreCase("authorization"))
        throw new ConfigurationException(
            "Configure credentials instead of overriding Authorization");
      headers.keySet().removeIf(key -> key.equalsIgnoreCase(name));
      headers.put(name, value);
      return this;
    }

    public Builder idempotencyKey(String value) {
      return header("Idempotency-Key", value);
    }

    public Builder query(String name, String value) {
      if (name == null || value == null)
        throw new ConfigurationException("Query key and value must be non-null");
      query.put(name, value);
      return this;
    }

    public Builder clearQuery() {
      query.clear();
      return this;
    }

    public RequestOptions build() {
      return new RequestOptions(this);
    }
  }
}
