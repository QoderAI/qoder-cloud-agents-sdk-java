package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.List;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class EffectiveConfigSession extends JsonObject {
  private EffectiveConfigSession(JsonValue value) {
    super(value);
  }

  public static EffectiveConfigSession fromJson(JsonValue value) {
    return new EffectiveConfigSession(value);
  }

  public String environmentId() {
    return optional("environment_id", JsonValue::asString);
  }

  public EnvironmentVariables environmentVariables() {
    return optional("environment_variables", EnvironmentVariables::fromJson);
  }

  public List<String> vaultIds() {
    return optionalList("vault_ids", JsonValue::asString);
  }

  public List<EffectiveConfigResource> resources() {
    return optionalList("resources", EffectiveConfigResource::fromJson);
  }
}
