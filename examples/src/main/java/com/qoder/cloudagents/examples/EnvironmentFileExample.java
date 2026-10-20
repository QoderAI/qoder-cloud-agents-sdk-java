package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.forward.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/** Isolated Environment and text File lifecycle; does not create a Session or run setup scripts. */
public final class EnvironmentFileExample {
  private EnvironmentFileExample() {}

  public static void main(String[] args) {
    if (!"true".equals(System.getenv("QODER_ALLOW_WRITE")))
      throw new IllegalArgumentException("Set QODER_ALLOW_WRITE=true");
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), System.out);
    }
  }

  public static void run(ForwardClient client, PrintStream out) {
    String batch = UUID.randomUUID().toString();
    String envId = null, fileId = null;
    RuntimeException failure = null;
    byte[] expected = "QCA Java SDK\n文件 round trip\n".getBytes(StandardCharsets.UTF_8);
    out.println(
        "example_batch="
            + batch
            + " create_env_key="
            + batch
            + "-env create_file_key="
            + batch
            + "-file");
    try {
      Environment env =
          client
              .environments()
              .create(
                  EnvironmentCreateParams.builder()
                      .name("sdk-java-env-" + batch)
                      .config(EnvironmentConfig.builder().type("cloud").build())
                      .build(),
                  options(batch + "-env"));
      envId = env.id();
      out.println("created_environment=" + envId);
      client.environments().retrieve(envId, readOptions());
      client
          .environments()
          .update(
              envId,
              EnvironmentUpdateParams.builder().description("Isolated Java example").build(),
              options(batch + "-env-update"));
      out.println(
          "environment_page_size="
              + client
                  .environments()
                  .list(EnvironmentListParams.builder().limit(10).build(), readOptions())
                  .data()
                  .size());
      FileMetadata file =
          client
              .files()
              .upload(
                  FileUploadParams.builder()
                      .file(Upload.bytes(expected, "sdk-java-" + batch + ".txt", "text/plain"))
                      .purpose("session_resource")
                      .metadata(Collections.singletonMap("purpose", "sdk-example"))
                      .build(),
                  options(batch + "-file"));
      fileId = file.id();
      out.println("created_file=" + fileId);
      client.files().getMetadata(fileId, readOptions());
      client
          .files()
          .list(FileListParams.builder().name(file.filename()).limit(10).build(), readOptions());
      try (FileDownload download = client.files().download(fileId, readOptions())) {
        // Compare in bounded chunks; do not trust a remote filename as a local path.
        int offset = 0, count;
        byte[] buffer = new byte[1024];
        while ((count = download.body().read(buffer)) != -1) {
          for (int i = 0; i < count; i++) {
            if (offset >= expected.length || buffer[i] != expected[offset++])
              throw new IllegalStateException("File download contents differ");
          }
        }
        if (offset != expected.length)
          throw new IllegalStateException("File download length differs");
        out.println("verified_download_bytes=" + offset + " request_id=" + download.requestId());
      } catch (IOException e) {
        throw new IllegalStateException("Unable to read download", e);
      }
    } catch (RuntimeException error) {
      failure = error;
      throw error;
    } finally {
      RuntimeException cleanup = null;
      if (fileId != null)
        try {
          client.files().delete(fileId, options(batch + "-file-delete"));
          out.println("deleted_file=" + fileId);
        } catch (RuntimeException e) {
          out.println("cleanup_failed_file=" + fileId);
          cleanup = e;
        }
      if (envId != null)
        try {
          client.environments().delete(envId, options(batch + "-env-delete"));
          out.println("deleted_environment=" + envId);
        } catch (RuntimeException e) {
          out.println("cleanup_failed_environment=" + envId);
          if (cleanup == null) cleanup = e;
          else cleanup.addSuppressed(e);
        }
      if (cleanup != null) {
        if (failure != null) failure.addSuppressed(cleanup);
        else throw cleanup;
      }
    }
  }

  private static RequestOptions options(String key) {
    return readOptions().toBuilder().idempotencyKey(key).build();
  }

  private static RequestOptions readOptions() {
    return RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build();
  }
}
