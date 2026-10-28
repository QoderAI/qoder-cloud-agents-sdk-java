package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.List;

/** A non-paginated collection returned by the Managed API. */
public final class DreamListResponse extends JsonModel {
  public DreamListResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public List<Dream> data() {
    return optionalList("data", v -> new Dream(v, requestId()));
  }
}
