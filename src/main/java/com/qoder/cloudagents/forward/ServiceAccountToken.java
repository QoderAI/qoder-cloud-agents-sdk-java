package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class ServiceAccountToken extends JsonModel {
  public ServiceAccountToken(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String type() {
    return optionalString("type");
  }

  public String accessToken() {
    return optionalString("access_token");
  }

  public String tokenType() {
    return optionalString("token_type");
  }

  public Long expiresIn() {
    return optionalLong("expires_in");
  }

  public String expiresAt() {
    return optionalString("expires_at");
  }

  public String authTokenId() {
    return optionalString("auth_token_id");
  }

  public String serviceAccountId() {
    return optionalString("service_account_id");
  }

  public String credentialId() {
    return optionalString("credential_id");
  }

  public String subjectType() {
    return optionalString("subject_type");
  }

  public String identityId() {
    return optionalString("identity_id");
  }
}
