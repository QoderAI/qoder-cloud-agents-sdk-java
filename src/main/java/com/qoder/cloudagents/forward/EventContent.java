package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import com.qoder.cloudagents.errors.ResponseValidationException;
import java.util.*;
import java.util.stream.Collectors;

/** Text or content blocks. Use text() for a user.message's required content-block array. */
public final class EventContent implements JsonSerializable {
  private final JsonValue value;

  private EventContent(JsonValue value) {
    if (value == null || (!value.isString() && !value.isArray()))
      throw new ResponseValidationException("Expected event content string or array");
    this.value = value;
  }

  public static EventContent fromJson(JsonValue value) {
    return new EventContent(value);
  }

  public static EventContent blocks(List<ContentBlock> value) {
    if (value == null || value.stream().anyMatch(Objects::isNull))
      throw new ConfigurationException("Content blocks must not be null");
    return fromJson(JsonValue.of(value));
  }

  public static EventContent text(String text) {
    if (text == null) throw new ConfigurationException("Text must not be null");
    return blocks(
        Collections.singletonList(ContentBlock.builder().type("text").text(text).build()));
  }

  /** String content for event types that accept it; user.message requires blocks instead. */
  public static EventContent ofString(String text) {
    if (text == null) throw new ConfigurationException("Text must not be null");
    return fromJson(JsonValue.of(text));
  }

  public boolean isString() {
    return value.isString();
  }

  public String asString() {
    return value.asString();
  }

  public List<ContentBlock> asBlocks() {
    return Collections.unmodifiableList(
        value.asArray().stream().map(ContentBlock::fromJson).collect(Collectors.toList()));
  }

  @Override
  public JsonValue toJsonValue() {
    return value;
  }

  @Override
  public String toString() {
    return "EventContent";
  }
}
