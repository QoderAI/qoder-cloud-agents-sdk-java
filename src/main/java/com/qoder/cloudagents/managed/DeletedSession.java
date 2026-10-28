package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class DeletedSession extends JsonModel {
  public DeletedSession(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static DeletedSession fromJson(JsonValue value) {
    return new DeletedSession(value, null);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public Boolean deleted() {
    return optional("deleted", JsonValue::asBoolean);
  }
}
