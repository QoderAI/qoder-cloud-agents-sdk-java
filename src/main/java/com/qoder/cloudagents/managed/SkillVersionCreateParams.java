package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Multipart Skill package. Upload files keep their relative path in the filename. */
public final class SkillVersionCreateParams {
  private final List<Upload> files;

  private final JsonValue fields;

  private SkillVersionCreateParams(Builder b, JsonValue fields) {
    files = Collections.unmodifiableList(new ArrayList<>(b.files));
    this.fields = fields;
  }

  public List<Upload> files() {
    return files;
  }

  public JsonValue fields() {
    return fields;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private final List<Upload> files = new ArrayList<>();

    public Builder files(List<Upload> value) {
      if (value == null || value.stream().anyMatch(Objects::isNull))
        throw new ConfigurationException("files cannot contain null");
      files.clear();
      files.addAll(value);
      return this;
    }

    public Builder addFile(Upload value) {
      if (value == null) throw new ConfigurationException("file cannot be null");
      files.add(value);
      return this;
    }

    public SkillVersionCreateParams build() {
      if (files.isEmpty()) throw new ConfigurationException("Supply Skill package files");
      if (!json().get("file").isMissing() || !json().get("files").isMissing())
        throw new ConfigurationException("Use Upload setters for multipart files");
      return new SkillVersionCreateParams(this, json());
    }
  }

  @Override
  public String toString() {
    return "SkillVersionCreateParams([redacted])";
  }
}
