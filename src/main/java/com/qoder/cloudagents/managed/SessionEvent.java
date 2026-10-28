package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.math.BigDecimal;
import java.util.*;

/** Immutable API response; unknown fields are preserved. */
public final class SessionEvent extends JsonModel {
  public SessionEvent(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public BigDecimal totalCredits() {
    return optional("total_credits", JsonValue::asDecimal);
  }

  public BigDecimal sandboxRuntimeCredits() {
    return optional("sandbox_runtime_credits", JsonValue::asDecimal);
  }

  public BigDecimal modelCredits() {
    return optional("model_credits", JsonValue::asDecimal);
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

  public String processedAt() {
    return optional("processed_at", JsonValue::asString);
  }

  public String sessionThreadId() {
    return optional("session_thread_id", JsonValue::asString);
  }

  public String thinking() {
    return optional("thinking", JsonValue::asString);
  }

  public String text() {
    return optional("text", JsonValue::asString);
  }

  public String toolUseId() {
    return optional("tool_use_id", JsonValue::asString);
  }

  public String customToolUseId() {
    return optional("custom_tool_use_id", JsonValue::asString);
  }

  public String mcpToolUseId() {
    return optional("mcp_tool_use_id", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public String mcpServerName() {
    return optional("mcp_server_name", JsonValue::asString);
  }

  public String result() {
    return optional("result", JsonValue::asString);
  }

  public String denyMessage() {
    return optional("deny_message", JsonValue::asString);
  }

  public String reason() {
    return optional("reason", JsonValue::asString);
  }

  public String description() {
    return optional("description", JsonValue::asString);
  }

  public String outcomeId() {
    return optional("outcome_id", JsonValue::asString);
  }

  public String messageId() {
    return optional("message_id", JsonValue::asString);
  }

  public String eventId() {
    return optional("event_id", JsonValue::asString);
  }

  public String fileId() {
    return optional("file_id", JsonValue::asString);
  }

  public String originalFilename() {
    return optional("original_filename", JsonValue::asString);
  }

  public String contentType() {
    return optional("content_type", JsonValue::asString);
  }

  public String title() {
    return optional("title", JsonValue::asString);
  }

  public String modelRequestStartId() {
    return optional("model_request_start_id", JsonValue::asString);
  }

  public String explanation() {
    return optional("explanation", JsonValue::asString);
  }

  public String outcomeEvaluationStartId() {
    return optional("outcome_evaluation_start_id", JsonValue::asString);
  }

  public EventContent content() {
    return optional("content", EventContent::fromJson);
  }

  public Boolean isError() {
    return optional("is_error", JsonValue::asBoolean);
  }

  public OutcomeRubric rubric() {
    return optional("rubric", OutcomeRubric::fromJson);
  }

  public Long maxIterations() {
    return optional("max_iterations", JsonValue::asLong);
  }

  public Long index() {
    return optional("index", JsonValue::asLong);
  }

  public Long size() {
    return optional("size", JsonValue::asLong);
  }

  public Long iteration() {
    return optional("iteration", JsonValue::asLong);
  }

  public Map<String, JsonValue> input() {
    return optionalMap("input", item -> item);
  }

  /** Credits for context compaction, when usage.credits is present. */
  public BigDecimal usageCredits() {
    JsonValue value = field("usage").get("credits");
    return value.isMissing() || value.isNull() ? null : value.asDecimal();
  }

  /** Incremental credits for a model request, when model_usage.credits is present. */
  public BigDecimal modelUsageCredits() {
    JsonValue value = field("model_usage").get("credits");
    return value.isMissing() || value.isNull() ? null : value.asDecimal();
  }

  public Map<String, JsonValue> usage() {
    return optionalMap("usage", item -> item);
  }

  public Map<String, JsonValue> stopReason() {
    return optionalMap("stop_reason", item -> item);
  }

  public Map<String, JsonValue> error() {
    return optionalMap("error", item -> item);
  }

  public Map<String, JsonValue> evaluatedPermission() {
    return optionalMap("evaluated_permission", item -> item);
  }

  public Agent agent() {
    return optional("agent", Agent::fromJson);
  }

  public Map<String, JsonValue> metadata() {
    return optionalMap("metadata", item -> item);
  }

  public Map<String, JsonValue> modelUsage() {
    return optionalMap("model_usage", item -> item);
  }

  public JsonValue message() {
    return optional("message", item -> item);
  }

  public JsonValue contentBlock() {
    return optional("content_block", item -> item);
  }

  public JsonValue delta() {
    return optional("delta", item -> item);
  }

  public JsonValue event() {
    return optional("event", item -> item);
  }
}
