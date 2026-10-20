package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.nio.charset.StandardCharsets;
import java.util.*;

final class RealtimeValidation {
  private RealtimeValidation() {}

  static String trim(String value) {
    if (value == null) return null;
    int start = 0, end = value.length();
    while (start < end && space(value.codePointAt(start)))
      start += Character.charCount(value.codePointAt(start));
    while (end > start && space(value.codePointBefore(end)))
      end -= Character.charCount(value.codePointBefore(end));
    return value.substring(start, end);
  }

  private static boolean space(int c) {
    return Character.isWhitespace(c) || Character.isSpaceChar(c) || c == 0x85;
  }

  static boolean validUnicode(String value) {
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (Character.isHighSurrogate(c)) {
        if (++i >= value.length() || !Character.isLowSurrogate(value.charAt(i))) return false;
      } else if (Character.isLowSurrogate(c)) return false;
    }
    return true;
  }

  static void text(String value, String name, int maximumBytes) {
    if (value == null
        || trim(value).isEmpty()
        || !value.equals(trim(value))
        || !validUnicode(value)
        || value.getBytes(StandardCharsets.UTF_8).length > maximumBytes
        || value.codePoints().anyMatch(Character::isISOControl))
      throw new ConfigurationException("Invalid " + name);
  }

  private static void characters(String value, String name, int maximum) {
    text(value, name, Integer.MAX_VALUE);
    if (value.codePointCount(0, value.length()) > maximum)
      throw new ConfigurationException("Invalid " + name);
  }

  static void create(RealtimeConversationCreateParams params) {
    if (params == null) throw new ConfigurationException("Parameters must not be null");
    characters(params.identityId(), "identity_id", 128);
    characters(params.templateId(), "template_id", 128);
    if (params.title() != null) characters(trim(params.title()), "title", 256);
    if (params.metadata() != null) {
      JsonValue metadata = params.field("metadata");
      if (metadata.toJson().getBytes(StandardCharsets.UTF_8).length > 16384 || hasNul(metadata))
        throw new ConfigurationException("Invalid conversation metadata");
    }
    if (params.field("config").isNull()) throw new ConfigurationException("config cannot be null");
    if (params.config() != null) params.config().toJsonValue().asObject();
  }

  private static boolean hasNul(JsonValue value) {
    if (value.isString()) return value.asString().indexOf('\0') >= 0;
    if (value.isArray()) return value.asArray().stream().anyMatch(RealtimeValidation::hasNul);
    if (value.isObject())
      return value.asObject().entrySet().stream()
          .anyMatch(e -> e.getKey().indexOf('\0') >= 0 || hasNul(e.getValue()));
    return false;
  }

  static Map<String, String> history(Map<String, String> query) {
    if (query.containsKey("limit")) {
      try {
        int n = Integer.parseInt(query.get("limit"));
        if (n < 1 || n > 100) throw new NumberFormatException();
      } catch (NumberFormatException e) {
        throw new ConfigurationException("limit must be 1–100");
      }
    }
    if (query.containsKey("before")
        && (query.get("before") == null || query.get("before").isEmpty()))
      throw new ConfigurationException("before must not be empty");
    if (query.containsKey("types")) {
      if (query.get("types") == null)
        throw new ConfigurationException("History types must not be null");
      Set<String> types = new HashSet<>(Arrays.asList(query.get("types").split(",", -1)));
      if (types.isEmpty() || !new HashSet<>(Arrays.asList("message", "work")).containsAll(types))
        throw new ConfigurationException("History types must be message and/or work");
    }
    return Collections.unmodifiableMap(new LinkedHashMap<>(query));
  }
}
