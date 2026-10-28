package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class SessionThread extends JsonModel {
  public SessionThread(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public static SessionThread fromJson(JsonValue value) {
    return new SessionThread(value, null);
  }

  public String id() {
    return optional("id", JsonValue::asString);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String sessionId() {
    return optional("session_id", JsonValue::asString);
  }

  public String parentThreadId() {
    return optional("parent_thread_id", JsonValue::asString);
  }

  public Agent agent() {
    return optional("agent", v -> new Agent(v, null));
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public SessionThreadStats stats() {
    return optional("stats", SessionThreadStats::fromJson);
  }

  public String archivedAt() {
    return optional("archived_at", JsonValue::asString);
  }

  public String createdAt() {
    return optional("created_at", JsonValue::asString);
  }

  public String updatedAt() {
    return optional("updated_at", JsonValue::asString);
  }
}
