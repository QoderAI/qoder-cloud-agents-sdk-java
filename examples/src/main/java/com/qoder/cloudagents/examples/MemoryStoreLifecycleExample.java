package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/** Creates an isolated, unmounted Store and deletes it with its test data on completion. */
public final class MemoryStoreLifecycleExample {
  private MemoryStoreLifecycleExample() {}

  public static void main(String[] args) {
    if (!"true".equals(System.getenv("QODER_ALLOW_WRITE")))
      throw new IllegalArgumentException("Set QODER_ALLOW_WRITE=true for the Memory Store example");
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), System.out);
    }
  }

  public static void run(ForwardClient client, PrintStream output) {
    String batch = UUID.randomUUID().toString();
    String name = "sdk-java-memory-" + batch;
    String storeId = null;
    RuntimeException failure = null;
    output.println("test_store_name=" + name + " create_idempotency_key=" + batch + "-store");
    try {
      storeId =
          client
              .memoryStores()
              .create(
                  MemoryStoreCreateParams.builder()
                      .name(name)
                      .metadata(Collections.singletonMap("purpose", "java-sdk-example"))
                      .build(),
                  writeOptions(batch + "-store"))
              .id();
      output.println("created_memory_store=" + storeId);
      MemoryStore store = client.memoryStores().retrieve(storeId, readOptions());
      output.println("store_status=" + store.status() + " request_id=" + store.requestId());
      client
          .memoryStores()
          .update(
              storeId,
              MemoryStoreUpdateParams.builder().description("Isolated SDK test data").build(),
              writeOptions(batch + "-store-update"));

      Memory memory =
          client
              .memoryStores()
              .memories()
              .create(
                  storeId,
                  MemoryCreateParams.builder()
                      .path("sdk-example/note.md")
                      .content("# Example\nOriginal text.")
                      .build(),
                  writeOptions(batch + "-memory"));
      output.println("created_memory=" + memory.id());
      Memory current =
          client.memoryStores().memories().retrieve(storeId, memory.id(), readOptions());
      if (current.contentSha256() == null || current.contentSha256().isEmpty())
        throw new IllegalStateException("The Memory response is missing its concurrency hash");
      client
          .memoryStores()
          .memories()
          .update(
              storeId,
              memory.id(),
              MemoryUpdateParams.builder()
                  .content("# Example\nUpdated text.")
                  .contentSha256(current.contentSha256())
                  .build(),
              writeOptions(batch + "-memory-update"));
      output.println(
          "memories="
              + client
                  .memoryStores()
                  .memories()
                  .list(
                      storeId,
                      MemoryListParams.builder().pathPrefix("sdk-example/").limit(10).build(),
                      readOptions())
                  .data()
                  .size());

      MemoryVersionListResponse versions =
          client
              .memoryStores()
              .memoryVersions()
              .list(
                  storeId,
                  MemoryVersionListParams.builder().memoryId(memory.id()).limit(10).build(),
                  readOptions());
      output.println("versions_on_first_page=" + versions.data().size());
      if (!versions.data().isEmpty()) {
        MemoryVersion version =
            client
                .memoryStores()
                .memoryVersions()
                .retrieve(storeId, versions.data().get(0).id(), readOptions());
        output.println(
            "version_operation=" + version.operation() + " redacted=" + version.redacted());
      }
      client
          .memoryStores()
          .memories()
          .delete(storeId, memory.id(), writeOptions(batch + "-memory-delete"));
      output.println("deleted_memory=" + memory.id());
    } catch (RuntimeException error) {
      failure = error;
      throw error;
    } finally {
      if (storeId != null) {
        // This Store was created by this invocation, never mounted, and contains only test data.
        try {
          client.memoryStores().delete(storeId, writeOptions(batch + "-store-delete"));
          output.println("deleted_memory_store=" + storeId);
        } catch (RuntimeException cleanup) {
          output.println("cleanup_failed_memory_store=" + storeId);
          if (failure != null) failure.addSuppressed(cleanup);
          else throw cleanup;
        }
      }
    }
  }

  private static RequestOptions writeOptions(String key) {
    return readOptions().toBuilder().idempotencyKey(key).build();
  }

  private static RequestOptions readOptions() {
    return RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build();
  }
}
