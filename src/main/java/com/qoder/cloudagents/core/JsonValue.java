package com.qoder.cloudagents.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoder.cloudagents.errors.ResponseValidationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable JSON value; preserves unknown fields, explicit null and numeric precision. */
public final class JsonValue {
  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
          .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
  private final JsonNode node;

  private JsonValue(JsonNode node) {
    this.node = node;
  }

  public static JsonValue parse(String json) {
    try {
      JsonNode value = MAPPER.readTree(json);
      if (value == null) throw new ResponseValidationException("Empty JSON response");
      return new JsonValue(value);
    } catch (IOException e) {
      throw new ResponseValidationException("Invalid JSON response");
    }
  }

  public static JsonValue of(Object value) {
    if (value instanceof JsonValue) return (JsonValue) value;
    if (value instanceof JsonSerializable) return ((JsonSerializable) value).toJsonValue();
    if (value instanceof Map) {
      com.fasterxml.jackson.databind.node.ObjectNode object = MAPPER.createObjectNode();
      ((Map<?, ?>) value)
          .forEach(
              (key, item) -> {
                if (!(key instanceof String))
                  throw new com.qoder.cloudagents.errors.ConfigurationException(
                      "JSON object keys must be strings");
                JsonValue child = of(item);
                if (child.isMissing())
                  throw new com.qoder.cloudagents.errors.ConfigurationException(
                      "Cannot serialize a missing JSON value");
                object.set((String) key, child.node);
              });
      return new JsonValue(object);
    }
    if (value instanceof Iterable) {
      com.fasterxml.jackson.databind.node.ArrayNode array = MAPPER.createArrayNode();
      for (Object item : (Iterable<?>) value) {
        JsonValue child = of(item);
        if (child.isMissing())
          throw new com.qoder.cloudagents.errors.ConfigurationException(
              "Cannot serialize a missing JSON value");
        array.add(child.node);
      }
      return new JsonValue(array);
    }
    return new JsonValue(MAPPER.valueToTree(value));
  }

  public boolean isBoolean() {
    return node.isBoolean();
  }

  public boolean isString() {
    return node.isTextual();
  }

  public JsonValue get(String field) {
    return new JsonValue(node.path(field));
  }

  public boolean isMissing() {
    return node.isMissingNode();
  }

  public boolean isNull() {
    return node.isNull();
  }

  public boolean isObject() {
    return node.isObject();
  }

  public boolean isArray() {
    return node.isArray();
  }

  public String stringOrNull() {
    return node.isTextual() ? node.textValue() : null;
  }

  public String asString() {
    if (!node.isTextual()) throw invalid("string");
    return node.textValue();
  }

  public boolean asBoolean() {
    if (!node.isBoolean()) throw invalid("boolean");
    return node.booleanValue();
  }

  public long asLong() {
    if (!node.isIntegralNumber() || !node.canConvertToLong()) throw invalid("64-bit integer");
    return node.longValue();
  }

  public BigDecimal asDecimal() {
    if (!node.isNumber()) throw invalid("number");
    return node.decimalValue();
  }

  public List<JsonValue> asArray() {
    if (!node.isArray()) throw invalid("array");
    List<JsonValue> result = new ArrayList<>();
    node.forEach(value -> result.add(new JsonValue(value)));
    return Collections.unmodifiableList(result);
  }

  public Map<String, JsonValue> asObject() {
    if (!node.isObject()) throw invalid("object");
    Map<String, JsonValue> result = new LinkedHashMap<>();
    node.properties().forEach(entry -> result.put(entry.getKey(), new JsonValue(entry.getValue())));
    return Collections.unmodifiableMap(result);
  }

  public String toJson() {
    if (isMissing()) throw invalid("present JSON value");
    return node.toString();
  }

  private ResponseValidationException invalid(String expected) {
    return new ResponseValidationException("Expected JSON " + expected);
  }

  @Override
  public String toString() {
    return "JsonValue(" + node.getNodeType() + ")";
  }
}
