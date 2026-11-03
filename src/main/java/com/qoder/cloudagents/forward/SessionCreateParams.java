package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionCreateParams extends JsonObject {
  private SessionCreateParams(JsonValue value) {
    super(value);
  }

  public static SessionCreateParams fromJson(JsonValue value) {
    return new SessionCreateParams(value);
  }

  public String identityId() {
    return optional("identity_id", JsonValue::asString);
  }

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String title() {
    return optional("title", JsonValue::asString);
  }

  /** Null means no limit; field("budget") distinguishes omission from explicit null. */
  public SessionBudget budget() {
    return optional("budget", SessionBudget::fromJson);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", item -> item);
  }

  public SessionConfig config() {
    return optional("config", SessionConfig::fromJson);
  }

  public List<SessionResourceSpec> resources() {
    return optionalList("resources", SessionResourceSpec::fromJson);
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

    public Builder title(String value) {
      return put("title", value);
    }

    /** Omit to keep the default/current budget; null clears it. */
    public Builder budget(SessionBudget value) {
      return put("budget", value);
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public Builder config(SessionConfig value) {
      return put("config", value);
    }

    public Builder resources(List<SessionResourceSpec> value) {
      return put("resources", value);
    }

    public SessionCreateParams build() {
      require("identity_id");
      require("template_id");
      return new SessionCreateParams(json());
    }
  }
}
