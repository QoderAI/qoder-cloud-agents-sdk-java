package com.qoder.cloudagents.core;

import com.qoder.cloudagents.errors.ConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/** Upload source. Bytes are snapshotted; path/stream uploads are never automatically replayed. */
public final class Upload {
  private final byte[] bytes;
  private final Path path;
  private final InputStream stream;
  private final String filename, mediaType;
  private final AtomicBoolean claimed = new AtomicBoolean();

  private Upload(byte[] bytes, Path path, InputStream stream, String filename, String mediaType) {
    if (filename == null
        || filename.isEmpty()
        || filename.chars().anyMatch(c -> c < 32 || c == 127))
      throw new ConfigurationException(
          "A nonempty filename without control characters is required");
    if (mediaType == null || mediaType.indexOf('\r') >= 0 || mediaType.indexOf('\n') >= 0)
      throw new ConfigurationException("Invalid media type");
    this.bytes = bytes;
    this.path = path;
    this.stream = stream;
    this.filename = filename;
    this.mediaType = mediaType;
  }

  public static Upload bytes(byte[] data, String filename, String mediaType) {
    if (data == null) throw new ConfigurationException("Upload data must not be null");
    return new Upload(data.clone(), null, null, filename, mediaType);
  }

  public static Upload path(Path path, String mediaType) {
    if (path == null || path.getFileName() == null)
      throw new ConfigurationException("File path is required");
    return path(path, path.getFileName().toString(), mediaType);
  }

  /** Supplies a multipart relative filename, for example review/SKILL.md. */
  public static Upload path(Path path, String filename, String mediaType) {
    if (path == null) throw new ConfigurationException("File path is required");
    return new Upload(null, path, null, filename, mediaType);
  }

  /**
   * Ownership transfers to the SDK when upload is invoked, including on failure or cancellation.
   */
  public static Upload stream(InputStream stream, String filename, String mediaType) {
    if (stream == null) throw new ConfigurationException("Upload stream is required");
    return new Upload(null, null, stream, filename, mediaType);
  }

  public String filename() {
    return filename;
  }

  public String mediaType() {
    return mediaType;
  }

  public boolean replayable() {
    return bytes != null;
  }

  /** Internal transport hook: each stream source can be claimed only once. */
  public InputStream claimStream() {
    if (stream != null && !claimed.compareAndSet(false, true))
      throw new ConfigurationException("Upload stream has already been used");
    return stream;
  }

  /** Internal transport hook; disk sources are opened only when the request is written. */
  public InputStream open() throws IOException {
    if (bytes != null) return new ByteArrayInputStream(bytes);
    if (path != null) return java.nio.file.Files.newInputStream(path);
    return stream;
  }

  @Override
  public String toString() {
    return "Upload([redacted])";
  }
}
