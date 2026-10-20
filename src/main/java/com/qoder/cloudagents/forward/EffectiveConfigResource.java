package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class EffectiveConfigResource extends JsonObject {
  private EffectiveConfigResource(JsonValue value) {
    super(value);
  }

  public static EffectiveConfigResource fromJson(JsonValue value) {
    return new EffectiveConfigResource(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String fileId() {
    return optional("file_id", JsonValue::asString);
  }

  public String url() {
    return optional("url", JsonValue::asString);
  }

  public String mountPath() {
    return optional("mount_path", JsonValue::asString);
  }
}
