package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable JSON configuration; unknown fields are preserved. */
public final class RepositoryCheckout extends JsonObject {
  private RepositoryCheckout(JsonValue value) {
    super(value);
  }

  public static RepositoryCheckout fromJson(JsonValue value) {
    return new RepositoryCheckout(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public String name() {
    return optional("name", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder type(String value) {
      return put("type", value);
    }

    public Builder name(String value) {
      return put("name", value);
    }

    public RepositoryCheckout build() {
      require("type", "name");

      return new RepositoryCheckout(json());
    }
  }
}
