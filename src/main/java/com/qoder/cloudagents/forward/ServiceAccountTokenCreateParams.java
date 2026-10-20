package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

public final class ServiceAccountTokenCreateParams extends JsonObject {
  private ServiceAccountTokenCreateParams(JsonValue value) {
    super(value);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    public Builder metadata(Map<String, ?> value) {
      return put("metadata", value);
    }

    public Builder ttlSeconds(long value) {
      if (value < 1 || value > 43200)
        throw new ConfigurationException("ttlSeconds must be 1..43200");
      return put("ttl_seconds", value);
    }

    public ServiceAccountTokenCreateParams build() {
      return new ServiceAccountTokenCreateParams(json());
    }
  }
}
