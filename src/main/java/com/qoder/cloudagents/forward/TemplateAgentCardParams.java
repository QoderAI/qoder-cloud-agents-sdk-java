package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.RequestOptions;
import java.util.*;

/** Agent Card selectors; unset values omit the Identity query or A2A-Version header. */
public final class TemplateAgentCardParams {
  private final String identityId;
  private final String a2aVersion;

  private TemplateAgentCardParams(Builder b) {
    identityId = b.identityId;
    a2aVersion = b.a2aVersion;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String identityId() {
    return identityId;
  }

  public String a2aVersion() {
    return a2aVersion;
  }

  Map<String, String> query() {
    Map<String, String> values = new LinkedHashMap<>();
    if (identityId != null) values.put("identity_id", identityId);
    return values;
  }

  public static final class Builder {
    private String identityId;
    private String a2aVersion;

    public Builder identityId(String value) {
      identityId = value;
      return this;
    }

    public Builder a2aVersion(String value) {
      RequestOptions.builder().header("A2A-Version", value);
      a2aVersion = value;
      return this;
    }

    public TemplateAgentCardParams build() {
      return new TemplateAgentCardParams(this);
    }
  }
}
