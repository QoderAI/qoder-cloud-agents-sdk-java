package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

public final class DriveClearParams {
  private final String identityId;

  private DriveClearParams(Builder b) {
    if (b.identityId == null || b.identityId.isEmpty())
      throw new ConfigurationException("identityId is required");
    identityId = b.identityId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String identityId() {
    return identityId;
  }

  Map<String, String> query() {
    return Collections.singletonMap("identity_id", identityId);
  }

  public static final class Builder {
    private String identityId;

    public Builder identityId(String value) {
      identityId = value;
      return this;
    }

    public DriveClearParams build() {
      return new DriveClearParams(this);
    }
  }
}
