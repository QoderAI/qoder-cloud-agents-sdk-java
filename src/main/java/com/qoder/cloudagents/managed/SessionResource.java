package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class SessionResource extends JsonModel {
  public SessionResource(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static SessionResource fromJson(JsonValue value) {
    return new SessionResource(value, null);
  }

  public String id() {
    return optional("id", JsonValue::asString);
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

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }
}
