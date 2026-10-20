package com.qoder.cloudagents.errors;

import com.qoder.cloudagents.core.ApiResponse;
import com.qoder.cloudagents.core.JsonValue;
import java.util.List;
import java.util.Map;

/** HTTP failure. Inspect body explicitly; the exception message does not dump server content. */
public final class ApiStatusException extends ApiException {
  private final ApiResponse<JsonValue> response;

  public ApiStatusException(ApiResponse<JsonValue> response) {
    super("QCA request failed with HTTP " + response.statusCode());
    this.response = response;
  }

  public int statusCode() {
    return response.statusCode();
  }

  public String requestId() {
    if (response.requestId() != null) return response.requestId();
    String id = body().get("request_id").stringOrNull();
    return id != null ? id : body().get("error").get("request_id").stringOrNull();
  }

  public Map<String, List<String>> headers() {
    return response.headers();
  }

  public JsonValue body() {
    return response.data();
  }

  public String code() {
    JsonValue error = body().get("error");
    return (error.isObject() ? error.get("code") : body().get("code")).stringOrNull();
  }

  public String type() {
    JsonValue error = body().get("error");
    return (error.isObject() ? error.get("type") : body().get("type")).stringOrNull();
  }
}
