package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

class ManagedDocumentationTest {
  static Stream<Arguments> responses() throws Exception {
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                ManagedDocumentationTest.class.getResourceAsStream(
                    "/managed-documentation-responses.json"),
                StandardCharsets.UTF_8))) {
      return JsonValue.parse(reader.lines().collect(Collectors.joining("\n"))).asArray().stream()
          .map(f -> Arguments.of(f.get("source").asString(), f));
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("responses")
  void officialResponseExamplesDecodeThroughTypedAccessors(String source, JsonValue fixture)
      throws Exception {
    Class<?> type =
        Class.forName("com.qoder.cloudagents.managed." + fixture.get("model").asString());
    JsonValue original = fixture.get("value");
    Object model =
        type.getConstructor(JsonValue.class, String.class).newInstance(original, "req_docs");
    assertEquals(original.toJson(), ((JsonSerializable) model).toJsonValue().toJson());
    walk(model, 0);
  }

  private void walk(Object value, int depth) throws Exception {
    if (value == null || depth > 5) return;
    if (value instanceof CursorPage) {
      for (Object item : ((CursorPage<?, ?>) value).data()) walk(item, depth + 1);
      return;
    }
    if (value instanceof com.qoder.cloudagents.managed.EventContent) {
      com.qoder.cloudagents.managed.EventContent content =
          (com.qoder.cloudagents.managed.EventContent) value;
      if (content.isString()) assertNotNull(content.asString());
      else walk(content.asBlocks(), depth + 1);
      return;
    }
    if (value instanceof Iterable && !(value instanceof CursorPage)) {
      for (Object item : (Iterable<?>) value) walk(item, depth + 1);
      return;
    }
    if (value instanceof Map) {
      for (Object item : ((Map<?, ?>) value).values()) walk(item, depth + 1);
      return;
    }
    if (!value.getClass().getName().startsWith("com.qoder.cloudagents.managed.")) return;
    for (Method method : value.getClass().getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers())
          || Modifier.isStatic(method.getModifiers())
          || method.getParameterCount() != 0
          || method.getName().equals("toJsonValue")) continue;
      try {
        walk(method.invoke(value), depth + 1);
      } catch (InvocationTargetException e) {
        throw new AssertionError(
            value.getClass().getSimpleName()
                + "."
                + method.getName()
                + " cannot decode official response",
            e.getCause());
      }
    }
  }
}
