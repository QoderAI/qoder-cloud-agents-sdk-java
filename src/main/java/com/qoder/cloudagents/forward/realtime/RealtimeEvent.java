package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ResponseValidationException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/** Validated server envelope, typed payload views, and lossless access to future event fields. */
public final class RealtimeEvent extends JsonObject {
  public static final String VERSION = "voice.realtime.v1";

  private RealtimeEvent(JsonValue value) {
    super(value);
    validate();
  }

  public static RealtimeEvent fromJson(JsonValue value) {
    return new RealtimeEvent(value);
  }

  public String version() {
    return field("version").asString();
  }

  public String type() {
    return field("type").asString();
  }

  public String eventId() {
    return field("event_id").asString();
  }

  public long sequence() {
    return field("sequence").asLong();
  }

  public String conversationId() {
    return field("conversation_id").asString();
  }

  public String timestamp() {
    return field("timestamp").asString();
  }

  public String workId() {
    return optionalString("work_id");
  }

  public String announcementId() {
    return optionalString("announcement_id");
  }

  public JsonValue payload() {
    return field("payload");
  }

  public RealtimeReady ready() {
    return type().equals("voice.ready") ? RealtimeReady.fromJson(payload()) : null;
  }

  public RealtimeTranscript transcript() {
    return type().startsWith("transcript.") ? RealtimeTranscript.fromJson(payload()) : null;
  }

  public RealtimeWork work() {
    return type().startsWith("work.") ? RealtimeWork.fromJson(payload()) : null;
  }

  public RealtimeError error() {
    return type().equals("error") ? RealtimeError.fromJson(payload()) : null;
  }

  public RealtimeCloseResult closed() {
    return type().equals("connection.closed") ? RealtimeCloseResult.fromJson(payload()) : null;
  }

  public byte[] audio() {
    return type().equals("audio.delta") ? decodeAudio(payload().get("audio").asString()) : null;
  }

  private static void require(boolean value) {
    if (!value) throw new ResponseValidationException("Invalid Realtime event");
  }

  private static String nonempty(JsonValue value) {
    String text = value.asString();
    require(!text.isEmpty());
    return text;
  }

  private void validate() {
    require(VERSION.equals(version()));
    nonempty(field("type"));
    nonempty(field("event_id"));
    nonempty(field("conversation_id"));
    require(sequence() >= 0);
    payload().asObject();
    require(
        timestamp()
            .matches(
                "\\d{4}-\\d{2}-\\d{2}[Tt]\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?([Zz]|[+-]\\d{2}:\\d{2})"));
    try {
      OffsetDateTime.parse(timestamp().toUpperCase(Locale.ROOT));
    } catch (DateTimeParseException e) {
      throw new ResponseValidationException("Invalid Realtime timestamp");
    }
    workId();
    announcementId();
    JsonValue p = payload();
    switch (type()) {
      case "voice.ready":
        nonempty(p.get("provider"));
        nonempty(p.get("config").get("audio").get("output").get("voice"));
        audioFormat(p.get("input_audio"), 16000);
        audioFormat(p.get("output_audio"), 24000);
        p.get("capabilities").get("graceful_close").asBoolean();
        break;
      case "voice.state":
        nonempty(p.get("state"));
        break;
      case "voice.replaced":
      case "playback.interrupt":
        nonempty(p.get("reason"));
        break;
      case "transcript.delta":
      case "transcript.final":
        p.get("role").asString();
        p.get("text").asString();
        transcript().itemId();
        transcript().responseId();
        break;
      case "audio.delta":
        audio();
        break;
      case "work.accepted":
        require("accepted".equals(p.get("status").asString()));
        p.get("objective").asString();
        break;
      case "work.running":
        require("running".equals(p.get("status").asString()));
        break;
      case "work.progress":
        nonempty(p.get("kind"));
        p.get("detail").asString();
        work().title();
        work().isError();
        work().eventId();
        break;
      case "work.milestone":
        nonempty(p.get("announcement_id"));
        nonempty(p.get("milestone_type"));
        p.get("summary").asString();
        break;
      case "work.completed":
        require("completed".equals(p.get("status").asString()));
        p.get("result").asString();
        work().eventId();
        break;
      case "work.failed":
        require("failed".equals(p.get("status").asString()));
        nonempty(p.get("error").get("code"));
        work().eventId();
        break;
      case "connection.closed":
        nonempty(p.get("request_id"));
        require(
            Arrays.asList("no_active_response", "no_content", "saved", "saved_interrupted")
                .contains(p.get("outcome").asString()));
        break;
      case "error":
        nonempty(p.get("code"));
        p.get("message").asString();
        error().retryable();
        Long delay = error().retryAfterMs();
        require(delay == null || delay >= 0);
        break;
      default:
        break;
    }
  }

  private static void audioFormat(JsonValue value, long rate) {
    require("pcm16".equals(value.get("format").asString()));
    require(value.get("sample_rate").asLong() == rate && value.get("channels").asLong() == 1);
  }

  static byte[] decodeAudio(String text) {
    try {
      byte[] audio = Base64.getDecoder().decode(text);
      require(
          audio.length > 0
              && audio.length % 2 == 0
              && Base64.getEncoder().encodeToString(audio).equals(text));
      return audio;
    } catch (IllegalArgumentException e) {
      throw new ResponseValidationException("Invalid Realtime PCM audio");
    }
  }

  boolean fatal() {
    return type().equals("voice.replaced")
        || (error() != null
            && Arrays.asList(
                    "provider_initialization_failed",
                    "voice_configuration_failed",
                    "context_restore_failed",
                    "provider_unavailable",
                    "service_restarting",
                    "event_persistence_failed")
                .contains(error().code()));
  }
}
