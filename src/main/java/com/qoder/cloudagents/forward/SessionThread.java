package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class SessionThread extends JsonModel {
  public SessionThread(JsonValue value, String requestId) {
    super(value, requestId);
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

  public String templateId() {
    return optional("template_id", JsonValue::asString);
  }

  public String role() {
    return optional("role", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public SessionThreadStopReason stopReason() {
    return optional("stop_reason", SessionThreadStopReason::fromJson);
  }

  public SessionThreadStats stats() {
    return optional("stats", SessionThreadStats::fromJson);
  }

  public SessionUsage usage() {
    return optional("usage", SessionUsage::fromJson);
  }

  public String parentThreadId() {
    return optional("parent_thread_id", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String createdByToolUseId() {
    return optional("created_by_tool_use_id", JsonValue::asString);
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
