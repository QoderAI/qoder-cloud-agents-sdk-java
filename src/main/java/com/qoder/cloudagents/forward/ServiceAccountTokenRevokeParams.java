package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class ServiceAccountTokenRevokeParams extends JsonObject {
  private ServiceAccountTokenRevokeParams(JsonValue value) {
    super(value);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    public Builder targetCredentialId(String value) {
      return put("target_credential_id", value);
    }

    public Builder targetExpiresAt(String value) {
      return put("target_expires_at", value);
    }

    public Builder reason(String value) {
      return put("reason", value);
    }

    public ServiceAccountTokenRevokeParams build() {
      return new ServiceAccountTokenRevokeParams(json());
    }
  }
}
