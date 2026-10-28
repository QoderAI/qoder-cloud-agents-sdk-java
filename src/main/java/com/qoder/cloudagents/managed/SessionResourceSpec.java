package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class SessionResourceSpec extends JsonObject {
  private SessionResourceSpec(JsonValue value) {
    super(value);
  }

  public static SessionResourceSpec fromJson(JsonValue value) {
    return new SessionResourceSpec(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String fileId() {
    return optional("file_id", JsonValue::asString);
  }

  public String mountPath() {
    return optional("mount_path", JsonValue::asString);
  }

  public String url() {
    return optional("url", JsonValue::asString);
  }

  public String authorizationToken() {
    return optional("authorization_token", JsonValue::asString);
  }

  public String password() {
    return optional("password", JsonValue::asString);
  }

  public RepositoryCheckout checkout() {
    return optional("checkout", RepositoryCheckout::fromJson);
  }

  public String memoryStoreId() {
    return optional("memory_store_id", JsonValue::asString);
  }

  public String access() {
    return optional("access", JsonValue::asString);
  }

  public String instructions() {
    return optional("instructions", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder fileId(String value) {
      return put("file_id", value);
    }

    public Builder mountPath(String value) {
      return put("mount_path", value);
    }

    public Builder url(String value) {
      return put("url", value);
    }

    public Builder authorizationToken(String value) {
      return put("authorization_token", value);
    }

    public Builder password(String value) {
      return put("password", value);
    }

    public Builder checkout(RepositoryCheckout value) {
      return put("checkout", value);
    }

    public Builder memoryStoreId(String value) {
      return put("memory_store_id", value);
    }

    public Builder access(String value) {
      return put("access", value);
    }

    public Builder instructions(String value) {
      return put("instructions", value);
    }

    public SessionResourceSpec build() {
      require("type");
      String type = json().get("type").asString();
      if ("file".equals(type)) require("file_id");
      else if ("github_repository".equals(type) || "git_repository".equals(type)) require("url");
      else if ("memory_store".equals(type)) require("memory_store_id");
      return new SessionResourceSpec(json());
    }
  }
}
