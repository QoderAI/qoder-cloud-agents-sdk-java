package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.List;

/** Immutable API response; unknown fields remain accessible through field(). */
public final class IdentityListTemplatesResponse extends JsonModel {
  public IdentityListTemplatesResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public List<IdentityTemplate> data() {
    return optionalList("data", IdentityTemplate::fromJson);
  }
}
