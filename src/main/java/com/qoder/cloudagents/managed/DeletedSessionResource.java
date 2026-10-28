package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class DeletedSessionResource extends JsonModel {
  public DeletedSessionResource(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static DeletedSessionResource fromJson(JsonValue value) {
    return new DeletedSessionResource(value, null);
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
