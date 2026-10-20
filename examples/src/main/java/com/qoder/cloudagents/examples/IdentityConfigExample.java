package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/** Demonstrates per-Identity configuration without modifying the supplied Template. */
public final class IdentityConfigExample {
  private IdentityConfigExample() {}

  public static void main(String[] args) {
    if (!"true".equals(System.getenv("QODER_ALLOW_WRITE")))
      throw new IllegalArgumentException(
          "Set QODER_ALLOW_WRITE=true to create and clean up a test Identity");
    String templateId = System.getenv("QODER_TEMPLATE_ID");
    if (templateId == null || templateId.isEmpty())
      throw new IllegalArgumentException("Set QODER_TEMPLATE_ID to an accessible test Template");
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), templateId, System.out);
    }
  }

  public static void run(ForwardClient client, String templateId, PrintStream output) {
    String batch = UUID.randomUUID().toString();
    String identityId = null;
    RuntimeException failure = null;
    try {
      Identity identity =
          client
              .identities()
              .create(
                  IdentityCreateParams.builder()
                      .externalId("sdk-java-" + batch)
                      .name("Java SDK example")
                      .enabled(true)
                      .build(),
                  options(batch + "-create"));
      identityId = identity.id();
      output.println("created_identity=" + identityId);
      IdentityConfig config =
          client
              .identities()
              .configs()
              .upsert(
                  identityId,
                  templateId,
                  IdentityConfigUpsertParams.builder()
                      .name("Java SDK example")
                      .identityConfig(
                          IdentityConfigSpec.builder()
                              .system(
                                  SystemOverride.builder()
                                      .mode("append")
                                      .content("Prefer concise answers.")
                                      .build())
                              .environmentVariables(
                                  Collections.singletonMap(
                                      "SDK_EXAMPLE",
                                      EnvironmentVariableOverride.builder()
                                          .op("set")
                                          .value("java")
                                          .build()))
                              .build())
                      .build(),
                  options(batch + "-config"));
      output.println("config=" + config.id());
      EffectiveConfig effective =
          client
              .identities()
              .configs()
              .getEffective(
                  identityId,
                  templateId,
                  RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build());
      output.println(
          "effective_hash=" + effective.effectiveHash() + " request_id=" + effective.requestId());
    } catch (RuntimeException error) {
      failure = error;
      throw error;
    } finally {
      if (identityId != null) {
        try {
          IdentityClearResponse cleared =
              client
                  .identities()
                  .clear(
                      identityId,
                      IdentityClearParams.builder()
                          .reason("Java SDK isolated example cleanup")
                          .build(),
                      options(batch + "-clear"));
          if (!"completed".equals(cleared.status()))
            throw new IllegalStateException("Identity cleanup did not complete");
          output.println("cleared_identity=" + identityId);
          client.identities().delete(identityId, options(batch + "-delete"));
          output.println("deleted_identity=" + identityId);
        } catch (RuntimeException error) {
          // Keep the Identity visible for recovery if clear failed before delete.
          output.println("cleanup_failed_identity=" + identityId);
          if (failure != null) failure.addSuppressed(error);
          else throw error;
        }
      }
    }
  }

  private static RequestOptions options(String key) {
    return RequestOptions.builder()
        .idempotencyKey(key)
        .totalTimeout(Duration.ofSeconds(30))
        .build();
  }
}
