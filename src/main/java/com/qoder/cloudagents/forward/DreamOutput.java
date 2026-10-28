package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DreamOutput extends JsonObject {
  private DreamOutput(JsonValue value) {
    super(value);
  }

  public static DreamOutput fromJson(JsonValue value) {
    return new DreamOutput(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String memoryStoreId() {
    return optional("memory_store_id", JsonValue::asString);
  }

  public List<String> filesTouched() {
    return optionalList("files_touched", JsonValue::asString);
  }
}
