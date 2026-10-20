package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class ScheduleArchiveManyResponse extends JsonModel {
  public ScheduleArchiveManyResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public Long archivedCount() {
    return optional("archived_count", JsonValue::asLong);
  }
}
