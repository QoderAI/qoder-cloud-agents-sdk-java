package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class OutcomeRubric extends JsonObject {
  private OutcomeRubric(JsonValue value) {
    super(value);
  }

  public static OutcomeRubric fromJson(JsonValue value) {
    return new OutcomeRubric(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String content() {
    return optional("content", JsonValue::asString);
  }

  /**
   * @deprecated Use {@link #content()}.
   */
  @Deprecated
  public String text() {
    return content();
  }

  public String fileId() {
    return optional("file_id", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder content(String value) {
      return put("content", value);
    }

    /**
     * @deprecated Use {@link #content(String)}; this alias also writes content.
     */
    @Deprecated
    public Builder text(String value) {
      return content(value);
    }

    public Builder fileId(String value) {
      return put("file_id", value);
    }

    public OutcomeRubric build() {
      require("type");
      return new OutcomeRubric(json());
    }
  }
}
