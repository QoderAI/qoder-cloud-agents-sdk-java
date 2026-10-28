package com.qoder.cloudagents.core.internal;

import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reserved provenance fields for typed Session creation, without mutating caller data. */
public final class SessionMetadata {
  private SessionMetadata() {}

  public static JsonValue created(JsonValue body) {
    JsonValue supplied = body.get("metadata");
    if (!supplied.isMissing() && !supplied.isNull() && !supplied.isObject())
      throw new ConfigurationException("Session metadata must be an object or null");
    Map<String, JsonValue> metadata = new LinkedHashMap<>();
    if (supplied.isObject()) metadata.putAll(supplied.asObject());
    marker(metadata, "__qca_sdk_created", "true");
    marker(metadata, "__qca_sdk_language", "java");
    marker(metadata, "__qca_sdk_version", Version.CURRENT);
    Map<String, JsonValue> request = new LinkedHashMap<>(body.asObject());
    request.put("metadata", JsonValue.of(metadata));
    return JsonValue.of(request);
  }

  private static void marker(Map<String, JsonValue> metadata, String key, String expected) {
    JsonValue value = metadata.get(key);
    if (value != null && (!value.isString() || !expected.equals(value.asString())))
      throw new ConfigurationException("Conflicting reserved Session metadata field: " + key);
    metadata.put(key, JsonValue.of(expected));
  }
}
