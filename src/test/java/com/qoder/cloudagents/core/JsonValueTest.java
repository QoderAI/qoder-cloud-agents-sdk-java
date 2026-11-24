package com.qoder.cloudagents.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qoder.cloudagents.errors.ResponseValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class JsonValueTest {
  @Test
  void asObjectPreservesFieldOrderAndReturnsUnmodifiableMap() {
    JsonValue value = JsonValue.parse("{\"second\":2,\"first\":\"one\",\"nested\":{\"ok\":true}}");
    Map<String, JsonValue> object = value.asObject();

    assertEquals(Arrays.asList("second", "first", "nested"), new ArrayList<>(object.keySet()));
    assertEquals(2L, object.get("second").asLong());
    assertEquals("one", object.get("first").asString());
    assertEquals(true, object.get("nested").asObject().get("ok").asBoolean());
    assertThrows(
        UnsupportedOperationException.class, () -> object.put("extra", JsonValue.of("nope")));
  }

  @Test
  void asObjectRejectsNonObjectValues() {
    assertThrows(ResponseValidationException.class, () -> JsonValue.parse("[1,2]").asObject());
    assertThrows(ResponseValidationException.class, () -> JsonValue.of("text").asObject());
  }
}
