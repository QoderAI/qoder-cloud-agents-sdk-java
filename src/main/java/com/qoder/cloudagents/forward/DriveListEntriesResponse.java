package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class DriveListEntriesResponse extends JsonModel {
  public DriveListEntriesResponse(JsonValue value, String requestId) {
    super(value, requestId);
  }

  public String nextPageToken() {
    return optionalString("next_page_token");
  }

  public List<DriveEntry> entries() {
    return optionalList("entries", v -> new DriveEntry(v, requestId()));
  }

  /** Absence of a token, including legacy responses, means the final page. */
  public boolean hasNextPage() {
    return nextPageToken() != null && !nextPageToken().isEmpty();
  }
}
