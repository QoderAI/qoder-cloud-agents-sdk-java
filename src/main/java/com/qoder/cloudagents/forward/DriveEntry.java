package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class DriveEntry extends JsonModel {
  public DriveEntry(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String path() {
    return optionalString("path");
  }

  public String name() {
    return optionalString("name");
  }

  public String type() {
    return optionalString("type");
  }

  public Long size() {
    return optionalLong("size");
  }

  public String etag() {
    return optionalString("etag");
  }

  public String lastModified() {
    return optionalString("last_modified");
  }
}
