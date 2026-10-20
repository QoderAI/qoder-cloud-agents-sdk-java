package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ResponseValidationException;
import java.util.*;
import java.util.stream.Collectors;

/** Tools keyed by name for sparse overrides, or an array replacing the full tool list. */
public final class IdentityTools implements JsonSerializable {
  private final JsonValue value;

  private IdentityTools(JsonValue value) {
    if (value == null || (!value.isObject() && !value.isArray()))
      throw new ResponseValidationException("Expected tool overrides object or replacement array");
    this.value = value;
  }

  public static IdentityTools overrides(Map<String, ToolOverride> values) {
    return fromJson(JsonValue.of(values));
  }

  public static IdentityTools replacement(List<Tool> values) {
    return fromJson(JsonValue.of(values));
  }

  public static IdentityTools fromJson(JsonValue value) {
    return new IdentityTools(value);
  }

  public boolean isReplacement() {
    return value.isArray();
  }

  public Map<String, ToolOverride> asOverrides() {
    Map<String, ToolOverride> result = new LinkedHashMap<>();
    value
        .asObject()
        .forEach(
            (name, item) -> result.put(name, item.isNull() ? null : ToolOverride.fromJson(item)));
    return Collections.unmodifiableMap(result);
  }

  public List<Tool> asReplacement() {
    return Collections.unmodifiableList(
        value.asArray().stream().map(Tool::fromJson).collect(Collectors.toList()));
  }

  @Override
  public JsonValue toJsonValue() {
    return value;
  }

  @Override
  public String toString() {
    return "IdentityTools";
  }
}
