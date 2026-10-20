package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import java.io.PrintStream;
import java.time.Duration;
import java.util.*;

/** Creates and archives one manual Schedule. Running it requires separate explicit opt-in. */
public final class ScheduleLifecycleExample {
  private ScheduleLifecycleExample() {}

  public static void main(String[] args) {
    if (!"true".equals(System.getenv("QODER_ALLOW_WRITE")))
      throw new IllegalArgumentException("Set QODER_ALLOW_WRITE=true");
    String identity = required("QODER_IDENTITY_ID"),
        template = required("QODER_TEMPLATE_ID"),
        environment = required("QODER_ENVIRONMENT_ID");
    boolean execute = "true".equals(System.getenv("QODER_ALLOW_EXECUTION"));
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), identity, template, environment, execute, System.out);
    }
  }

  public static void run(
      ForwardClient client,
      String identityId,
      String templateId,
      String environmentId,
      boolean execute,
      PrintStream out) {
    String key = UUID.randomUUID().toString(), scheduleId = null;
    RuntimeException failure = null;
    out.println("schedule_create_key=" + key + "-create");
    try {
      Schedule s =
          client
              .schedules()
              .create(
                  ScheduleCreateParams.builder()
                      .identityId(identityId)
                      .templateId(templateId)
                      .environmentId(environmentId)
                      .name("sdk-java-manual-" + key)
                      .initialEvents(
                          Collections.singletonList(
                              ScheduleInitialEvent.builder()
                                  .type("user.message")
                                  .content("Reply with exactly SDK_JAVA_SCHEDULE_OK")
                                  .build()))
                      .triggerPolicy(ScheduleTriggerPolicyParams.builder().type("manual").build())
                      .execution(ScheduleExecution.builder().maxAttempts(1L).build())
                      .build(),
                  write(key + "-create"));
      scheduleId = s.id();
      out.println("created_schedule=" + scheduleId);
      client.schedules().retrieve(scheduleId, read());
      client
          .schedules()
          .update(
              scheduleId,
              ScheduleUpdateParams.builder().description("Isolated manual SDK example").build(),
              write(key + "-update"));
      client.schedules().pause(scheduleId, write(key + "-pause"));
      client.schedules().unpause(scheduleId, write(key + "-unpause"));
      if (execute) {
        ScheduleRun run = client.schedules().run(scheduleId, write(key + "-run"));
        out.println("created_run=" + run.id());
        ScheduleRun current =
            client
                .scheduleRuns()
                .retrieve(
                    run.id(),
                    ScheduleRunRetrieveParams.builder().identityId(identityId).build(),
                    read());
        out.println(
            "run_status="
                + current.status()
                + " session_id="
                + current.sessionId()
                + " request_id="
                + current.requestId());
        out.println(
            "Run continues asynchronously; archiving the Schedule does not cancel this Run.");
      }
      ScheduleRunListResponse runs =
          client
              .scheduleRuns()
              .list(
                  ScheduleRunListParams.builder()
                      .identityId(identityId)
                      .scheduleId(scheduleId)
                      .limit(10)
                      .build(),
                  read());
      out.println("visible_runs=" + runs.data().size());
    } catch (RuntimeException e) {
      failure = e;
      throw e;
    } finally {
      if (scheduleId != null)
        try {
          client.schedules().archive(scheduleId, write(key + "-archive"));
          out.println("archived_schedule=" + scheduleId);
        } catch (RuntimeException cleanup) {
          out.println("cleanup_failed_schedule=" + scheduleId);
          if (failure != null) failure.addSuppressed(cleanup);
          else throw cleanup;
        }
    }
  }

  private static String required(String name) {
    String value = System.getenv(name);
    if (value == null || value.isEmpty()) throw new IllegalArgumentException("Set " + name);
    return value;
  }

  private static RequestOptions read() {
    return RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build();
  }

  private static RequestOptions write(String key) {
    return read().toBuilder().idempotencyKey(key).build();
  }
}
