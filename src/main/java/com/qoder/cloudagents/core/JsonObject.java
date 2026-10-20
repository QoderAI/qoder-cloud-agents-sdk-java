package com.qoder.cloudagents.core;

import com.qoder.cloudagents.errors.ResponseValidationException;
import java.util.*;
import java.util.function.Function;

/** Immutable JSON object. Missing and explicitly null fields remain distinguishable via field(). */
public abstract class JsonObject implements JsonSerializable {
  private final JsonValue value;

  protected JsonObject(JsonValue value) {
    if (value == null || !value.isObject())
      throw new ResponseValidationException("Expected a JSON object");
    this.value = value;
  }

  public final JsonValue field(String name) {
    return value.get(name);
  }

  public final String rawJson() {
    return value.toJson();
  }

  @Override
  public final JsonValue toJsonValue() {
    return value;
  }

  protected final String optionalString(String name) {
    return optional(name, JsonValue::asString);
  }

  protected final Boolean optionalBoolean(String name) {
    return optional(name, JsonValue::asBoolean);
  }

  protected final Long optionalLong(String name) {
    return optional(name, JsonValue::asLong);
  }

  protected final <T> T optional(String name, Function<JsonValue, T> parse) {
    JsonValue field = field(name);
    return field.isMissing() || field.isNull() ? null : parse.apply(field);
  }

  protected final <T> List<T> optionalList(String name, Function<JsonValue, T> parse) {
    return optional(
        name,
        value -> {
          List<T> result = new ArrayList<>();
          value.asArray().forEach(item -> result.add(parse.apply(item)));
          return Collections.unmodifiableList(result);
        });
  }

  protected final <T> Map<String, T> optionalMap(String name, Function<JsonValue, T> parse) {
    return optional(
        name,
        value -> {
          Map<String, T> result = new LinkedHashMap<>();
          value.asObject().forEach((key, item) -> result.put(key, parse.apply(item)));
          return Collections.unmodifiableMap(result);
        });
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  /** Overlay map entries can be null to restore an inherited value. */
  protected final <T> Map<String, T> optionalNullableMap(
      String name, Function<JsonValue, T> parse) {
    return optionalMap(name, value -> value.isNull() ? null : parse.apply(value));
  }
}
