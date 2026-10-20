package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;

/** An explicit, bounded set of Schedule IDs. The server rejects extra fields including scope. */
public final class ScheduleArchiveManyParams implements JsonSerializable {
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
    return JsonValue.of(Collections.singletonMap("schedule_ids", ids));
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
