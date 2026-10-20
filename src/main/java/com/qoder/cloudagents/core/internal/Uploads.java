package com.qoder.cloudagents.core.internal;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import com.qoder.cloudagents.forward.FileUploadParams;
import java.io.*;
import java.util.*;
import okhttp3.*;
import okio.*;

/** A request owns all upload inputs, including cancellation before body transmission. */
public final class Uploads implements AutoCloseable {
  private final List<Part> parts = new ArrayList<>();
  private boolean closed;
  private final RequestBody body;

  public Uploads(FileUploadParams params) {
    this(
        Collections.singletonMap("file", Collections.singletonList(params.file())),
        params.fields());
  }

  public Uploads(Map<String, List<Upload>> files, JsonValue fields) {
    MultipartBody.Builder form = new MultipartBody.Builder().setType(MultipartBody.FORM);
    try {
      for (Map.Entry<String, List<Upload>> entry : files.entrySet()) {
        for (Upload upload : entry.getValue()) {
          Part part = new Part(upload);
          parts.add(part);
          form.addFormDataPart(entry.getKey(), upload.filename(), part);
        }
      }
      fields
          .asObject()
          .forEach(
              (name, value) ->
                  form.addFormDataPart(name, value.isString() ? value.asString() : value.toJson()));
      // Claim only after validating every part, so malformed input never partially takes ownership.
      for (Part part : parts) part.active = part.upload.claimStream();
      body = form.build();
    } catch (RuntimeException e) {
      close();
      throw e;
    }
  }

  private final class Part extends RequestBody {
    final Upload upload;
    final MediaType media;
    InputStream active;

    Part(Upload upload) {
      this.upload = upload;
      try {
        media = MediaType.get(upload.mediaType());
      } catch (IllegalArgumentException e) {
        throw new ConfigurationException("Invalid upload media type");
      }
    }

    public MediaType contentType() {
      return media;
    }

    public boolean isOneShot() {
      return !upload.replayable();
    }

    public void writeTo(BufferedSink sink) throws IOException {
      InputStream input;
      synchronized (Uploads.this) {
        if (closed) throw new IOException("Upload closed");
        input = active != null ? active : upload.open();
        active = input;
      }
      try (InputStream owned = input;
          Source source = Okio.source(owned)) {
        sink.writeAll(source);
      } finally {
        synchronized (Uploads.this) {
          if (active == input) active = null;
        }
      }
    }
  }

  public RequestBody body() {
    return body;
  }

  public boolean replayable() {
    return parts.stream().allMatch(p -> p.upload.replayable());
  }

  public synchronized void close() {
    closed = true;
    for (Part part : parts) {
      if (part.active != null) {
        try {
          part.active.close();
        } catch (IOException ignored) {
        }
        part.active = null;
      }
    }
  }
}
