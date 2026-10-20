package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class BatchTaskArtifact extends JsonObject {
  private BatchTaskArtifact(JsonValue value) {
    super(value);
  }

  public static BatchTaskArtifact fromJson(JsonValue value) {
    return new BatchTaskArtifact(value);
  }

  public String fileId() {
    return optional("file_id", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String contentType() {
    return optional("content_type", JsonValue::asString);
  }

  public Long size() {
    return optional("size", JsonValue::asLong);
  }
}
