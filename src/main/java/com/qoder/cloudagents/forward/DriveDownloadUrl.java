package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class DriveDownloadUrl extends JsonModel {
  public DriveDownloadUrl(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String url() {
    return optionalString("url");
  }

  public String method() {
    return optionalString("method");
  }

  public Map<String, String> headers() {
    return optionalMap("headers", JsonValue::asString);
  }

  public String expiresAt() {
    return optionalString("expires_at");
  }

  public Long size() {
    return optionalLong("size");
  }

  public String etag() {
    return optionalString("etag");
  }

  public String contentType() {
    return optionalString("content_type");
  }
}
