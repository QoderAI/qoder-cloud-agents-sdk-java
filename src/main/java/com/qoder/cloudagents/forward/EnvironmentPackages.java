package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;

/** Immutable API configuration preserving unknown fields. */
public final class EnvironmentPackages extends JsonObject {
  private EnvironmentPackages(JsonValue value) {
    super(value);
  }

  public static EnvironmentPackages fromJson(JsonValue value) {
    return new EnvironmentPackages(value);
  }

  public String type() {
    return optional("type", JsonValue::asString);
  }

  public List<String> apt() {
    return optionalList("apt", JsonValue::asString);
  }

  public List<String> npm() {
    return optionalList("npm", JsonValue::asString);
  }

  public List<String> pip() {
    return optionalList("pip", JsonValue::asString);
  }

  public List<String> cargo() {
    return optionalList("cargo", JsonValue::asString);
  }

  public List<String> gem() {
    return optionalList("gem", JsonValue::asString);
  }

  public List<String> go() {
    return optionalList("go", JsonValue::asString);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends JsonObjectBuilder<Builder> {
    private Builder() {}

    public Builder apt(List<String> value) {
      return put("apt", value);
    }

    public Builder npm(List<String> value) {
      return put("npm", value);
    }

    public Builder pip(List<String> value) {
      return put("pip", value);
    }

    public EnvironmentPackages build() {
      return new EnvironmentPackages(json());
    }
  }
}
