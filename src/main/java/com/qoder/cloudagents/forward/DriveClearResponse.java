package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class DriveClearResponse extends JsonModel {
  public DriveClearResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public Long deletedCount() {
    return optionalLong("deleted_count");
  }
}
