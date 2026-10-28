package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.List;

/** A non-paginated collection returned by the Managed API. */
public final class ModelListResponse extends JsonModel {
  public ModelListResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public List<Model> data() {
    return optionalList("data", v -> new Model(v, requestId()));
  }
}
