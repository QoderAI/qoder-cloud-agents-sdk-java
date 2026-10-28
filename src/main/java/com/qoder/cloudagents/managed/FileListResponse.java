package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Cursor page with lazy iteration and cancellable next-page requests. */
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

  @Override
  protected FileListResponse self() {
    return this;
  }
}
