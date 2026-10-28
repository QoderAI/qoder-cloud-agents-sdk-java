package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.Map;

/** Multipart fields and an upload source; headers/query are supplied through RequestOptions. */
public final class FileUploadParams {
  private final Upload file;
  private final JsonValue fields;

  private FileUploadParams(Upload file, JsonValue fields) {
    this.file = file;
    this.fields = fields;
  }

  public Upload file() {
    return file;
  }

  public JsonValue fields() {
    return fields;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Upload file;

    public Builder file(Upload value) {
      file = value;
      return this;
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public Builder metadata(Map<String, String> value) {
      return put("metadata", value);
    }

    public Builder metadata(JsonValue value) {
      return put("metadata", value);
    }

    public FileUploadParams build() {
      if (file == null) throw new ConfigurationException("Upload file is required");
      if (!json().get("file").isMissing())
        throw new ConfigurationException("Use file(Upload) for the multipart file");
      return new FileUploadParams(file, json());
    }
  }

  @Override
  public String toString() {
    return "FileUploadParams([redacted])";
  }
}
