package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class SessionResource extends JsonModel {
  public SessionResource(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String fileId() {
    return optional("file_id", JsonValue::asString);
  }

  public String mountPath() {
    return optional("mount_path", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }
}
