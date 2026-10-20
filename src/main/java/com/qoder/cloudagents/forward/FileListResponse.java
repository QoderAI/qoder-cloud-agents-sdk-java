package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public final class FileListResponse extends CursorPage<FileMetadata, FileListResponse> {
  public FileListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  FileListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<Map<String, String>, Set<String>, CompletableFuture<FileListResponse>> fetcher) {
    super(value, requestId, query, seen, fetcher, item -> new FileMetadata(item, requestId), true);
  }

  public String nextPage() {
    return optionalString("next_page");
  }

  @Override
  protected FileListResponse self() {
    return this;
  }
}
