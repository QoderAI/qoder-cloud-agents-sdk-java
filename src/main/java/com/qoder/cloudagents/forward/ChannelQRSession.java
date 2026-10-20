package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class ChannelQRSession extends JsonModel {
  public ChannelQRSession(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String sessionKey() {
    return optional("session_key", JsonValue::asString);
  }

  public String channelId() {
    return optional("channel_id", JsonValue::asString);
  }

  public String channelType() {
    return optional("channel_type", JsonValue::asString);
  }

  public String status() {
    return optional("status", JsonValue::asString);
  }

  public String qrCodeContent() {
    return optional("qr_code_content", JsonValue::asString);
  }

  public String qrCodeImageBase64() {
    return optional("qr_code_image_base64", JsonValue::asString);
  }

  public String expiresAt() {
    return optional("expires_at", JsonValue::asString);
  }

  public String errCode() {
    return optional("err_code", JsonValue::asString);
  }

  public String errMsg() {
    return optional("err_msg", JsonValue::asString);
  }

  public Long pollIntervalSeconds() {
    return optional("poll_interval_seconds", JsonValue::asLong);
  }
}
