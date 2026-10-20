package com.qoder.cloudagents.core;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/** Owned streaming download. Close after reading; no content is buffered in memory by the SDK. */
public final class FileDownload implements AutoCloseable {
  private final InputStream body;
  private final ApiResponse<Void> response;
  private final String apiRequestId;
  private final Runnable release;

  public FileDownload(
      InputStream body, ApiResponse<Void> response, String apiRequestId, Runnable release) {
    this.body = body;
    this.response = response;
    this.apiRequestId = apiRequestId;
    this.release = release;
  }

  public InputStream body() {
    return body;
  }

  public int statusCode() {
    return response.statusCode();
  }

  public Map<String, List<String>> headers() {
    return response.headers();
  }

  public String header(String name) {
    return response.header(name);
  }

  /** The QCA API request ID (the grant request ID for a two-hop File download). */
  public String requestId() {
    return apiRequestId;
  }

  /** Final response request ID; for a direct Skill download this is the API request ID. */
  public String storageRequestId() {
    return response.requestId();
  }

  @Override
  public void close() {
    release.run();
  }

  @Override
  public String toString() {
    return "FileDownload";
  }
}
