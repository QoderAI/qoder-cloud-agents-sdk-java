package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonModel;
import com.qoder.cloudagents.core.JsonValue;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ModelListResponse extends JsonModel {
  private final List<Model> data;

  public ModelListResponse(JsonValue value, String requestId) {
    super(value, requestId);
    data =
        Collections.unmodifiableList(
            field("data").asArray().stream()
                .map(item -> new Model(item, requestId))
                .collect(Collectors.toList()));
  }

  public List<Model> data() {
    return data;
  }

  public boolean hasMore() {
    return field("has_more").asBoolean();
  }
}
