package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/**
 * An explicit, bounded set of Schedule IDs. Requests always carry the only supported scope, {@code
 * by_schedule_ids}; the server rejects a missing scope and other extra fields.
 */
public final class ScheduleArchiveManyParams implements JsonSerializable {
  private static final String SCOPE = "by_schedule_ids";
  private final List<String> ids;

  private ScheduleArchiveManyParams(List<String> value) {
    if (value == null) throw new ConfigurationException("scheduleIds are required");
    LinkedHashSet<String> unique = new LinkedHashSet<>();
    for (String id : value) {
      if (id == null || id.trim().isEmpty())
        throw new ConfigurationException("scheduleIds must not contain empty IDs");
      unique.add(id);
    }
    if (unique.isEmpty() || unique.size() > 50)
      throw new ConfigurationException("Supply 1..50 unique schedule IDs");
    ids = Collections.unmodifiableList(new ArrayList<>(unique));
  }

  public List<String> scheduleIds() {
    return ids;
  }

  public JsonValue toJsonValue() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("scope", SCOPE);
    body.put("schedule_ids", ids);
    return JsonValue.of(body);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private List<String> ids;

    public Builder scheduleIds(List<String> value) {
      ids = value == null ? null : new ArrayList<>(value);
      return this;
    }

    public ScheduleArchiveManyParams build() {
      return new ScheduleArchiveManyParams(ids);
    }
  }
}
