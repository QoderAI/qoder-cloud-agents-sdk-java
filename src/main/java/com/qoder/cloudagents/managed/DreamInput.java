package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class DreamInput extends JsonObject {
  private DreamInput(JsonValue value) {
    super(value);
  }

  public static DreamInput fromJson(JsonValue value) {
    return new DreamInput(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String memoryStoreId() {
    return optional("memory_store_id", JsonValue::asString);
  }

  public List<String> sessionIds() {
    return optionalList("session_ids", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder memoryStoreId(String value) {
      return put("memory_store_id", value);
    }

    public Builder sessionIds(List<String> value) {
      return put("session_ids", value);
    }

    public DreamInput build() {
      require("type");
      String type = json().get("type").asString();
      if ("memory_store".equals(type)) require("memory_store_id");
      else if ("sessions".equals(type)) require("session_ids");
      else
        throw new ConfigurationException("Managed Dream inputs must be memory_store or sessions");
      return new DreamInput(json());
    }
  }
}
