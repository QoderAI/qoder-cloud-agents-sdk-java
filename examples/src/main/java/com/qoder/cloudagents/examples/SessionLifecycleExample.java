package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/** Creates an isolated Session without sending model input; optionally attaches a shared store. */
public final class SessionLifecycleExample {
  private SessionLifecycleExample() {}

  public static void main(String[] args) {
    if (!"true".equals(System.getenv("QODER_ALLOW_WRITE")))
      throw new IllegalArgumentException(
          "Set QODER_ALLOW_WRITE=true for the isolated Session example");
    String templateId = System.getenv("QODER_TEMPLATE_ID");
    if (templateId == null || templateId.isEmpty())
      throw new IllegalArgumentException("Set QODER_TEMPLATE_ID to an accessible test Template");
    String storeId = System.getenv("QODER_MEMORY_STORE_ID");
    if (storeId != null && storeId.isEmpty()) storeId = null;
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), templateId, storeId, System.out);
    }
  }

  public static void run(
      ForwardClient client, String templateId, String storeId, PrintStream output) {
    String batch = UUID.randomUUID().toString();
    String identityId = null, sessionId = null;
    boolean mounted = false;
    RuntimeException failure = null;
    try {
      identityId =
          client
              .identities()
              .create(
                  IdentityCreateParams.builder()
                      .externalId("sdk-java-session-" + batch)
                      .name("Java Session example")
                      .build(),
                  options(batch + "-identity"))
              .id();
      output.println("created_identity=" + identityId);
      if (storeId != null) {
        client
            .identities()
            .memoryStores()
            .mount(
                identityId,
                templateId,
                IdentityMemoryStoreMountParams.builder().memoryStoreId(storeId).build(),
                options(batch + "-mount"));
        mounted = true;
        output.println(
            "mounts="
                + client
                    .identities()
                    .memoryStores()
                    .list(identityId, templateId, readOptions())
                    .data()
                    .size());
      }
      sessionId =
          client
              .sessions()
              .create(
                  SessionCreateParams.builder()
                      .identityId(identityId)
                      .templateId(templateId)
                      .title("Java Session example")
                      .config(
                          SessionConfig.builder()
                              .environmentVariables(Collections.singletonMap("SDK_EXAMPLE", "java"))
                              .build())
                      .build(),
                  options(batch + "-session"))
              .id();
      output.println("created_session=" + sessionId);
      Session session = client.sessions().retrieve(sessionId, readOptions());
      output.println("session_status=" + session.status() + " request_id=" + session.requestId());
      client
          .sessions()
          .update(
              sessionId,
              SessionUpdateParams.builder().title("Java Session example updated").build(),
              options(batch + "-update"));
      output.println(
          "events="
              + client
                  .sessions()
                  .events()
                  .list(
                      sessionId, SessionEventListParams.builder().limit(10).build(), readOptions())
                  .data()
                  .size());
      output.println(
          "threads="
              + client
                  .sessions()
                  .threads()
                  .list(
                      sessionId, SessionThreadListParams.builder().limit(10).build(), readOptions())
                  .data()
                  .size());
    } catch (RuntimeException error) {
      failure = error;
      throw error;
    } finally {
      RuntimeException cleanupFailure = null;
      if (sessionId != null) {
        try {
          client.sessions().archive(sessionId, options(batch + "-archive"));
          output.println("archived_session=" + sessionId);
        } catch (RuntimeException error) {
          output.println("cleanup_failed_session=" + sessionId);
          cleanupFailure = error;
        }
      }
      if (mounted) {
        try {
          client
              .identities()
              .memoryStores()
              .detach(identityId, templateId, storeId, options(batch + "-detach"));
          output.println("detached_memory_store=" + storeId);
        } catch (RuntimeException error) {
          output.println("cleanup_failed_mount=" + storeId);
          if (cleanupFailure == null) cleanupFailure = error;
          else cleanupFailure.addSuppressed(error);
        }
      }
      // Keep the Identity for recovery if its Session or mount could not be cleaned.
      if (identityId != null && cleanupFailure == null) {
        try {
          IdentityClearResponse cleared =
              client
                  .identities()
                  .clear(
                      identityId,
                      IdentityClearParams.builder().reason("Java Session example cleanup").build(),
                      options(batch + "-clear"));
          if (!"completed".equals(cleared.status()))
            throw new IllegalStateException("Identity cleanup did not complete");
          client.identities().delete(identityId, options(batch + "-delete"));
          output.println("deleted_identity=" + identityId);
        } catch (RuntimeException error) {
          cleanupFailure = error;
        }
      }
      if (cleanupFailure != null) {
        output.println("cleanup_failed_identity=" + identityId);
        if (failure != null) failure.addSuppressed(cleanupFailure);
        else throw cleanupFailure;
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
