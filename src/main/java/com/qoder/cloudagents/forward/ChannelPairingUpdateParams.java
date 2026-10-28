package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class ChannelPairingUpdateParams extends JsonObject {
  private ChannelPairingUpdateParams(JsonValue value) {
    super(value);
  }

  public static ChannelPairingUpdateParams fromJson(JsonValue value) {
    return new ChannelPairingUpdateParams(value);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder identityId(String value) {
      return put("identity_id", value);
    }

    public Builder templateId(String value) {
      return put("template_id", value);
    }

    public ChannelPairingUpdateParams build() {
      if (json().asObject().isEmpty())
        throw new ConfigurationException("At least one binding field is required");
      for (Map.Entry<String, JsonValue> entry : json().asObject().entrySet()) {
        if (!entry.getKey().equals("identity_id") && !entry.getKey().equals("template_id"))
          throw new ConfigurationException("Unsupported pairing update field");
        JsonValue value = entry.getValue();
        if (!value.isString()
            || value.asString().trim().isEmpty()
            || value.asString().chars().anyMatch(Character::isISOControl))
          throw new ConfigurationException(
              "Pairing binding IDs must be nonblank strings without control characters");
      }
      return new ChannelPairingUpdateParams(json());
    }
  }
}
