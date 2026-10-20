package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;

/** Immutable configuration; unknown fields remain accessible through field(). */
public final class IdentityClearSummary extends JsonObject {
  private IdentityClearSummary(JsonValue value) {
    super(value);
  }

  public static IdentityClearSummary fromJson(JsonValue value) {
    return new IdentityClearSummary(value);
  }

  public Long identityConfigsArchived() {
    return optional("identity_configs_archived", JsonValue::asLong);
  }

  public Long resourceBindingsArchived() {
    return optional("resource_bindings_archived", JsonValue::asLong);
  }

  public Long identityOwnedResourcesArchived() {
    return optional("identity_owned_resources_archived", JsonValue::asLong);
  }

  public Long schedulesArchived() {
    return optional("schedules_archived", JsonValue::asLong);
  }

  public Long scheduleRunsSkipped() {
    return optional("schedule_runs_skipped", JsonValue::asLong);
  }

  public Long sessionsArchived() {
    return optional("sessions_archived", JsonValue::asLong);
  }
}
