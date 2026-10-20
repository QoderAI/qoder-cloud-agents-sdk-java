package com.qoder.cloudagents.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/** Data with response metadata. Close resource-valued data such as FileDownload after use. */
public final class ApiResponse<T> {
  private final T data;
  private final int statusCode;
  private final Map<String, List<String>> headers;

  public ApiResponse(T data, int statusCode, Map<String, List<String>> headers) {
    this.data = data;
    this.statusCode = statusCode;
    Map<String, List<String>> copy = new LinkedHashMap<>();
    headers.forEach(
        (key, values) ->
            copy.put(
                key.toLowerCase(Locale.ROOT),
                Collections.unmodifiableList(new ArrayList<>(values))));
    this.headers = Collections.unmodifiableMap(copy);
  }

  public T data() {
    return data;
  }

  public int statusCode() {
    return statusCode;
  }

  public Map<String, List<String>> headers() {
    return headers;
  }

  public String requestId() {
    String id = header("x-request-id");
    return id != null ? id : header("request-id");
  }

  public String header(String name) {
    List<String> values = headers.get(name.toLowerCase(Locale.ROOT));
    return values == null || values.isEmpty() ? null : values.get(0);
  }

  public <R> ApiResponse<R> map(Function<T, R> mapper) {
    return new ApiResponse<>(mapper.apply(data), statusCode, headers);
  }
}
