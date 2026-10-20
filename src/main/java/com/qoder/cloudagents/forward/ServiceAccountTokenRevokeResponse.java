package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class ServiceAccountTokenRevokeResponse extends JsonModel {
  public ServiceAccountTokenRevokeResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public Boolean revoked() {
    return optionalBoolean("revoked");
  }
}
