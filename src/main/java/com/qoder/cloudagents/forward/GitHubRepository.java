package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.JsonObject;
import com.qoder.cloudagents.core.JsonValue;

/** Immutable GitHubRepository configuration. Unknown fields are retained through field(). */
public final class GitHubRepository extends JsonObject {
  private GitHubRepository(JsonValue value) {
    super(value);
  }

  public static GitHubRepository fromJson(JsonValue value) {
    return new GitHubRepository(value);
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
}
