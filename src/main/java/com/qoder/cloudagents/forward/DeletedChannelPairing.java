package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class DeletedChannelPairing extends JsonModel {
  public DeletedChannelPairing(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public Boolean deleted() {
    return optional("deleted", JsonValue::asBoolean);
  }
}
