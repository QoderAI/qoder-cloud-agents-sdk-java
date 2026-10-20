package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonSerializable;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.errors.ResponseValidationException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Environment variables as an object or the server's textual configuration format. */
public final class EnvironmentVariables implements JsonSerializable {
  private final JsonValue value;

  private EnvironmentVariables(JsonValue value) {
    if (value == null || (!value.isObject() && !value.isString()))
      throw new ResponseValidationException("Expected environment variables object or string");
    this.value = value;
  }

  public static EnvironmentVariables of(Map<String, String> values) {
    return fromJson(JsonValue.of(values));
  }

  public static EnvironmentVariables fromText(String text) {
    return fromJson(JsonValue.of(text));
  }

  public static EnvironmentVariables fromJson(JsonValue value) {
    return new EnvironmentVariables(value);
  }

  public boolean isString() {
    return value.isString();
  }

  public String asString() {
    return value.asString();
  }

  public Map<String, String> asMap() {
    Map<String, String> result = new LinkedHashMap<>();
    value.asObject().forEach((key, item) -> result.put(key, item.asString()));
    return Collections.unmodifiableMap(result);
  }

  @Override
  public JsonValue toJsonValue() {
    return value;
  }

  @Override
  public String toString() {
    return "EnvironmentVariables([redacted])";
  }
}
