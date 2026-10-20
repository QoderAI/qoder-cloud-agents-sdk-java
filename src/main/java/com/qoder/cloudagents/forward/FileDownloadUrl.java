package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API response preserving unknown fields. */
public final class FileDownloadUrl extends JsonModel {
  public FileDownloadUrl(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String url() {
    return optional("url", JsonValue::asString);
  }

  public String expiresAt() {
    return optional("expires_at", JsonValue::asString);
  }

  public String filename() {
    return optional("filename", JsonValue::asString);
  }
}
