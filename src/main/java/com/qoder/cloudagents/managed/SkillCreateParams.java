package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** Multipart Skill package. Upload files keep their relative path in the filename. */
public final class SkillCreateParams {
  private final List<Upload> files;
  private final Upload file;
  private final JsonValue fields;

  private SkillCreateParams(Builder b, JsonValue fields) {
    files = Collections.unmodifiableList(new ArrayList<>(b.files));
    file = b.file;
    this.fields = fields;
  }

  public List<Upload> files() {
    return files;
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
    private final List<Upload> files = new ArrayList<>();
    private Upload file;

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

    /**
     * @deprecated Use addFile or files instead.
     */
    @Deprecated
    public Builder file(Upload value) {
      if (value == null) throw new ConfigurationException("file cannot be null");
      file = value;
      return this;
    }

    public Builder metadata(Map<String, JsonValue> value) {
      return put("metadata", value);
    }

    public Builder displayTitle(String value) {
      return put("display_title", value);
    }

    /**
     * @deprecated The canonical name is read from SKILL.md.
     */
    @Deprecated
    public Builder name(String value) {
      return put("name", value);
    }

    /**
     * @deprecated The description is read from SKILL.md.
     */
    @Deprecated
    public Builder description(String value) {
      return put("description", value);
    }

    /**
     * @deprecated Use custom package creation.
     */
    @Deprecated
    public Builder type(String value) {
      return put("type", value);
    }

    public SkillCreateParams build() {
      if (files.isEmpty() && file == null)
        throw new ConfigurationException("Supply Skill package files");
      if (!json().get("file").isMissing() || !json().get("files").isMissing())
        throw new ConfigurationException("Use Upload setters for multipart files");
      return new SkillCreateParams(this, json());
    }
  }

  @Override
  public String toString() {
    return "SkillCreateParams([redacted])";
  }
}
