package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class DeletedChannel extends JsonModel {
  public DeletedChannel(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public Boolean deleted() {
    return optional("deleted", JsonValue::asBoolean);
  }
}
