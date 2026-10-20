package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Creates isolated Templates, updates/clones them, and archives all created IDs on exit. */
public final class TemplateLifecycleExample {
  private TemplateLifecycleExample() {}

  public static void main(String[] args) {
    if (!"true".equals(System.getenv("QODER_ALLOW_WRITE")))
      throw new IllegalArgumentException(
          "Set QODER_ALLOW_WRITE=true to create and archive test Templates");
    String environmentId = System.getenv("QODER_ENVIRONMENT_ID");
    if (environmentId == null || environmentId.isEmpty())
      throw new IllegalArgumentException(
          "Set QODER_ENVIRONMENT_ID to an accessible test Environment");
    String model = System.getenv("QODER_MODEL");
    if (model == null || model.isEmpty()) model = "auto";
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), environmentId, model, System.out);
    }
  }

  public static void run(
      ForwardClient client, String environmentId, String model, PrintStream output) {
    String batch = UUID.randomUUID().toString();
    List<String> created = new ArrayList<>();
    RuntimeException failure = null;
    try {
      Template original =
          client
              .templates()
              .create(
                  TemplateCreateParams.builder()
                      .name("sdk-java-" + batch)
                      .modelId(model)
                      .environmentId(environmentId)
                      .managedToolConfig(
                          ManagedToolConfig.builder().enabledTools(Collections.emptyList()).build())
                      .build(),
                  options(batch + "-create"));
      created.add(original.id());
      output.println("created=" + original.id());
      client
          .templates()
          .update(
              original.id(),
              TemplateUpdateParams.builder().description("Java SDK lifecycle example").build(),
              options(batch + "-update"));
      Template copy =
          client
              .templates()
              .clone(
                  original.id(),
                  TemplateCloneParams.builder().name("sdk-java-copy-" + batch).build(),
                  options(batch + "-clone"));
      created.add(copy.id());
      Template retrieved =
          client
              .templates()
              .retrieve(
                  copy.id(), RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build());
      output.println(
          "clone="
              + retrieved.id()
              + " status="
              + retrieved.status()
              + " request_id="
              + retrieved.requestId());
    } catch (RuntimeException error) {
      failure = error;
      throw error;
    } finally {
      RuntimeException cleanupFailure = null;
      Collections.reverse(created);
      for (String id : created) {
        try {
          client.templates().archive(id, options(batch + "-archive-" + id));
          output.println("archived=" + id);
        } catch (RuntimeException error) {
          output.println("archive_failed=" + id);
          if (cleanupFailure == null) cleanupFailure = error;
          else cleanupFailure.addSuppressed(error);
        }
      }
      if (cleanupFailure != null) {
        if (failure != null) failure.addSuppressed(cleanupFailure);
        else throw cleanupFailure;
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
