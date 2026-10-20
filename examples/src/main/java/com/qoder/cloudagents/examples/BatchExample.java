package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.forward.*;
import java.io.PrintStream;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

/** Submit JSONL or inspect a Batch. Submission can execute tasks and consume credits. */
public final class BatchExample {
  private BatchExample() {}

  public static void main(String[] args) {
    String batchId = System.getenv("QODER_BATCH_ID");
    String file = System.getenv("QODER_BATCH_INPUT");
    if (batchId == null || batchId.isEmpty()) {
      if (!"true".equals(System.getenv("QODER_ALLOW_EXECUTION"))
          || !"true".equals(System.getenv("QODER_ALLOW_WRITE")))
        throw new IllegalArgumentException(
            "Submission requires QODER_ALLOW_WRITE=true and QODER_ALLOW_EXECUTION=true");
      if (file == null || file.isEmpty())
        throw new IllegalArgumentException("Set QODER_BATCH_INPUT to a JSONL file");
    }
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      if (batchId != null && !batchId.isEmpty()) inspect(setup.client(), batchId, System.out);
      else
        submit(
            setup.client(),
            Paths.get(file),
            "true".equals(System.getenv("QODER_IGNORE_IDLE_WINDOW")),
            System.out);
    }
  }

  public static Batch submit(
      ForwardClient client, Path input, boolean ignoreIdleWindow, PrintStream out) {
    String key = UUID.randomUUID().toString();
    out.println("batch_create_key=" + key + "-create");
    FileMetadata file =
        client
            .files()
            .upload(
                FileUploadParams.builder()
                    .file(Upload.path(input, "application/jsonl"))
                    .purpose("session_resource")
                    .build(),
                read().toBuilder().maxRetries(0).build());
    out.println("batch_input_file=" + file.id());
    // Retain the input until the Batch is terminal; on ambiguous failure use the key/ID to
    // reconcile.
    Batch batch =
        client
            .batches()
            .create(
                BatchCreateParams.builder()
                    .inputFileId(file.id())
                    .completionWindow("24h")
                    .ignoreIdleWindow(ignoreIdleWindow)
                    .build(),
                read().toBuilder().idempotencyKey(key + "-create").build());
    out.println(
        "created_batch="
            + batch.id()
            + " status="
            + batch.status()
            + " queue_reason="
            + batch.queueReason());
    out.println(
        "Input file retained. Inspect until terminal, then delete it when no longer needed.");
    return batch;
  }

  public static Batch inspect(ForwardClient client, String batchId, PrintStream out) {
    Batch batch = client.batches().retrieve(batchId, read());
    out.println(
        "batch="
            + batch.id()
            + " status="
            + batch.status()
            + " queue_reason="
            + batch.queueReason()
            + " request_id="
            + batch.requestId());
    BatchTaskListResponse page =
        client
            .batches()
            .tasks()
            .list(batchId, BatchTaskListParams.builder().limit(20).build(), read());
    for (BatchTask task : page.data())
      out.println("task=" + task.customId() + " status=" + task.status());
    out.println("has_more_tasks=" + page.hasNextPage());
    Set<String> terminal =
        new HashSet<>(Arrays.asList("completed", "failed", "cancelled", "expired"));
    if (terminal.contains(batch.status())) {
      if (batch.outputFileId() != null && !batch.outputFileId().isEmpty()) {
        BatchFile output = client.batches().getOutput(batchId, read());
        out.println("output_url_expires_at=" + output.expiresAt());
      }
      if (batch.errorFileId() != null && !batch.errorFileId().isEmpty()) {
        BatchFile error = client.batches().getError(batchId, read());
        out.println("error_url_expires_at=" + error.expiresAt());
      }
    }
    return batch;
  }

  private static RequestOptions read() {
    return RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build();
  }
}
