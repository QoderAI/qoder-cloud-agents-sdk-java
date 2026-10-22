package com.qoder.cloudagents.core;

import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.Arrays;
import java.util.Locale;

/** A JSON request relative to the client's API base URL. Paths are unencoded segments. */
public final class ApiRequest {
  private final String method;
  private final String[] path;
  private final JsonValue body;

  private ApiRequest(Builder builder) {
    method = builder.method;
    path = builder.path.clone();
    body = builder.body;
  }

  /** For example, builder("POST", "sessions", sessionId, "new-action"). */
  public static Builder builder(String method, String... path) {
    return new Builder(method, path);
  }

  public String method() {
    return method;
  }

  public String[] path() {
    return path.clone();
  }

  public JsonValue body() {
    return body;
  }

  public static final class Builder {
    private final String method;
    private final String[] path;
    private JsonValue body;

    private Builder(String method, String[] path) {
      if (method == null
          || !Arrays.asList("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
              .contains(method.toUpperCase(Locale.ROOT)))
        throw new ConfigurationException("Unsupported HTTP method");
      this.method = method.toUpperCase(Locale.ROOT);
      if (path == null || path.length == 0)
        throw new ConfigurationException("Supply relative API path segments");
      this.path = path.clone();
      for (String part : this.path)
        if (part == null
            || part.isEmpty()
            || part.equals(".")
            || part.equals("..")
            || part.indexOf(':') >= 0
            || part.indexOf('/') >= 0
            || part.indexOf('\\') >= 0
            || part.indexOf('?') >= 0
            || part.indexOf('#') >= 0)
          throw new ConfigurationException("Supply unencoded relative path segments, not a URL");
    }

    public Builder body(JsonValue value) {
      if (method.equals("GET") || method.equals("HEAD"))
        throw new ConfigurationException("GET and HEAD requests cannot have a body");
      if (value == null || value.isMissing())
        throw new ConfigurationException("Use JsonValue.of(null) for an explicit JSON null");
      body = value;
      return this;
    }

    public ApiRequest build() {
      return new ApiRequest(this);
    }
  }
}
