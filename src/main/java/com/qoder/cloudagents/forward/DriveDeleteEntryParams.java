package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

public final class DriveDeleteEntryParams extends JsonObject {
  private final String identityId;

  private DriveDeleteEntryParams(JsonValue value, String identityId) {
    super(value);
    this.identityId = identityId;
  }

  public String identityId() {
    return identityId;
  }

  Map<String, String> query() {
    return Collections.singletonMap("identity_id", identityId);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private String identityId;

    public Builder identityId(String v) {
      identityId = v;
      return this;
    }

    public Builder path(String v) {
      return put("path", v);
    }

    public Builder recursive(Boolean v) {
      return put("recursive", v);
    }

    public DriveDeleteEntryParams build() {
      if (identityId == null || identityId.isEmpty())
        throw new ConfigurationException("identityId is required");
      require("path");
      return new DriveDeleteEntryParams(json(), identityId);
    }
  }
}
