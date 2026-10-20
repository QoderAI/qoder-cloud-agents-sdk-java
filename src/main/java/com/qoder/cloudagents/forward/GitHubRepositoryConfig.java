package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonObjectBuilder;
import com.qoder.cloudagents.core.JsonValue;

/** Immutable GitHubRepositoryConfig configuration. Unknown fields are retained through field(). */
public final class GitHubRepositoryConfig extends JsonObject {
  private GitHubRepositoryConfig(JsonValue value) {
    super(value);
  }

  public static GitHubRepositoryConfig fromJson(JsonValue value) {
    return new GitHubRepositoryConfig(value);
  }

  public String url() {
    return optional("url", JsonValue::asString);
  }

  public String mountPath() {
    return optional("mount_path", JsonValue::asString);
  }

  public Boolean enabled() {
    return optional("enabled", JsonValue::asBoolean);
  }

  public String authorizationToken() {
    return optional("authorization_token", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder url(String value) {
      return put("url", value);
    }

    public Builder mountPath(String value) {
      return put("mount_path", value);
    }

    public Builder enabled(Boolean value) {
      return put("enabled", value);
    }

    public Builder authorizationToken(String value) {
      return put("authorization_token", value);
    }

    public GitHubRepositoryConfig build() {
      return new GitHubRepositoryConfig(json());
    }
  }
}
