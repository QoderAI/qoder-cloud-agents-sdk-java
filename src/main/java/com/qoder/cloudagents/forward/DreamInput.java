package com.qoder.cloudagents.forward;

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

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
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

    public Builder identityId(String value) {
      return put("identity_id", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public Builder sessionIds(List<String> value) {
      return put("session_ids", value);
    }

    public DreamInput build() {
      require("type");
      String type = json().get("type").asString();
      if (type.equals("memory_store")) require("memory_store_id");
      if (type.equals("identity_template")) require("identity_id", "template_id");
      if (type.equals("sessions")) {
        require("session_ids");
        if (json().get("session_ids").asArray().isEmpty())
          throw new ConfigurationException("Session inputs must not be empty");
      }
      return new DreamInput(json());
    }
  }
}
