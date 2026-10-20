package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/** Writes one uniquely named file and deletes only that file; never clears an existing Drive. */
public final class DriveExample {
  private DriveExample() {}

  public static void main(String[] args) throws IOException {
    if (!"true".equals(System.getenv("QODER_ALLOW_WRITE")))
      throw new IllegalArgumentException("Set QODER_ALLOW_WRITE=true");
    String identity = System.getenv("QODER_IDENTITY_ID");
    if (identity == null || identity.isEmpty())
      throw new IllegalArgumentException("Set QODER_IDENTITY_ID");
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), identity, System.out);
    }
  }

  public static void run(ForwardClient client, String identity, PrintStream out)
      throws IOException {
    String dir = "sdk-java/" + UUID.randomUUID(), path = dir + "/round-trip.txt";
    byte[] bytes = "QCA Java Drive round trip\n文件\n".getBytes(StandardCharsets.UTF_8);
    boolean mayHaveWritten = false;
    Exception failure = null;
    out.println("example_drive_path=" + path);
    try {
      DriveUploadUrl upload =
          client
              .drives()
              .createUploadUrl(
                  DriveCreateUploadUrlParams.builder()
                      .identityId(identity)
                      .path(path)
                      .contentType("text/plain")
                      .build(),
                  options());
      if (!"PUT".equals(upload.method())) throw new IllegalStateException("Expected a PUT grant");
      mayHaveWritten = true;
      transfer(upload.url(), upload.method(), upload.headers(), bytes, null);
      DriveListEntriesResponse entries =
          client
              .drives()
              .listEntries(
                  DriveListEntriesParams.builder().identityId(identity).path(dir).limit(20).build(),
                  options());
      out.println("directory_entries=" + entries.entries().size());
      DriveDownloadUrl download =
          client
              .drives()
              .createDownloadUrl(
                  DriveCreateDownloadUrlParams.builder().identityId(identity).path(path).build(),
                  options());
      if (!"GET".equals(download.method())) throw new IllegalStateException("Expected a GET grant");
      transfer(download.url(), download.method(), download.headers(), null, bytes);
      out.println("verified_drive_bytes=" + bytes.length);
    } catch (IOException | RuntimeException e) {
      failure = e;
      throw e;
    } finally {
      if (mayHaveWritten)
        try {
          client
              .drives()
              .deleteEntry(
                  DriveDeleteEntryParams.builder()
                      .identityId(identity)
                      .path(path)
                      .recursive(false)
                      .build(),
                  options());
          out.println("deleted_drive_path=" + path);
        } catch (RuntimeException e) {
          out.println("cleanup_failed_drive_path=" + path);
          if (failure != null) failure.addSuppressed(e);
          else throw e;
        }
    }
  }

  private static void transfer(
      String address, String method, Map<String, String> headers, byte[] upload, byte[] expected)
      throws IOException {
    URL url = new URL(address);
    boolean local =
        "localhost".equals(url.getHost())
            || "127.0.0.1".equals(url.getHost())
            || "[::1]".equals(url.getHost());
    if ((!"https".equals(url.getProtocol()) && !(local && "http".equals(url.getProtocol())))
        || url.getUserInfo() != null)
      throw new IOException("Storage grant must use HTTPS (HTTP is allowed only for local tests)");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setInstanceFollowRedirects(false);
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(30000);
    connection.setRequestMethod(method);
    // This connection has no SDK authentication or default headers. Copy only the grant headers.
    if (headers != null) headers.forEach(connection::setRequestProperty);
    try {
      if (upload != null) {
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(upload.length);
        try (OutputStream sink = connection.getOutputStream()) {
          sink.write(upload);
        }
      }
      int status = connection.getResponseCode();
      if (status < 200 || status >= 300)
        throw new IOException("Storage request failed with HTTP " + status);
      try (InputStream source = connection.getInputStream()) {
        int offset = 0, n;
        byte[] buffer = new byte[1024];
        while ((n = source.read(buffer)) != -1) {
          if (expected != null)
            for (int i = 0; i < n; i++)
              if (offset >= expected.length || buffer[i] != expected[offset++])
                throw new IOException("Drive contents differ");
        }
        if (expected != null && offset != expected.length)
          throw new IOException("Drive length differs");
      }
    } finally {
      connection.disconnect();
    }
  }

  private static RequestOptions options() {
    return RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build();
  }
}
