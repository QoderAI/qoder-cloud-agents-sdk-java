package com.qoder.cloudagents.core;

import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Shared builder semantics: unset omits a field, a null setter writes JSON null. */
public abstract class JsonObjectBuilder<B extends JsonObjectBuilder<B>> {
  private final Map<String, JsonValue> fields = new LinkedHashMap<>();

  protected JsonObjectBuilder() {}

  @SuppressWarnings("unchecked")
  protected final B put(String name, Object value) {
    fields.put(name, JsonValue.of(value));
    return (B) this;
  }

  public final B additionalField(String name, JsonValue value) {
    if (name == null || name.isEmpty())
      throw new ConfigurationException("Field name must not be empty");
    return put(name, value);
  }

  @SuppressWarnings("unchecked")
  public final B omit(String name) {
    fields.remove(name);
    return (B) this;
  }

  protected final JsonValue json() {
    return JsonValue.of(fields);
  }

  protected final void require(String... names) {
    for (String name : names) {
      JsonValue value = fields.get(name);
      if (value == null
          || value.isNull()
          || value.isMissing()
          || (value.isString() && value.asString().isEmpty()))
        throw new ConfigurationException("Missing required field: " + name);
    }
  }

  @Override
  public final String toString() {
    return "JsonObjectBuilder([redacted])";
  }
}
