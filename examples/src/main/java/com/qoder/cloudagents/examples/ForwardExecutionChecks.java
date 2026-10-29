package com.qoder.cloudagents.examples;

import static com.qoder.cloudagents.examples.ForwardLiveCheck.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.forward.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Packaged-consumer acceptance, isolated from the SDK and the default Maven test lifecycle. */
final class ForwardExecutionChecks {
  private final ForwardClient client;
  private final String sourceTemplate;
  private final PrintStream out;

  ForwardExecutionChecks(ForwardClient client, String sourceTemplate, PrintStream out) {
    this.client = client;
    this.sourceTemplate = sourceTemplate;
    this.out = out;
  }

  void run(String group) throws Exception {
    if ("usage".equals(group)) usage();
    else if ("schedule".equals(group)) schedule();
    else if ("batch".equals(group)) batch();
    else throw new IllegalArgumentException("Unknown execution group");
  }

  void schedule() throws Exception {
    try (Fixture f = new Fixture()) {
      String schedule = null;
      Exception failure = null;
      try {
        schedule =
            client
                .schedules()
                .create(
                    ScheduleCreateParams.builder()
                        .identityId(f.identity.id)
                        .templateId(f.template)
                        .environmentId(f.environment)
                        .name("sdk-java-manual-" + f.key)
                        .initialEvents(
                            Collections.singletonList(
                                ScheduleInitialEvent.builder()
                                    .type("user.message")
                                    .content("Reply with exactly " + f.marker)
                                    .build()))
                        .triggerPolicy(ScheduleTriggerPolicyParams.builder().type("manual").build())
                        .execution(
                            ScheduleExecution.builder()
                                .maxAttempts(1L)
                                .maxConcurrentRuns(1L)
                                .build())
                        .build(),
                    write(f.key + "-schedule"))
                .id();
        log(out, "created_schedule", "id", schedule);
        // A lost Run response must not cause us to delete resources an unobserved Run may use.
        f.safeToClean = false;
        ScheduleRun run = client.schedules().run(schedule, write(f.key + "-run"));
        log(out, "created_run", "id", run.id(), "request_id", run.requestId());
        long deadline = System.nanoTime() + Duration.ofMinutes(4).toNanos();
        String previous = null;
        while (true) {
          run =
              client
                  .scheduleRuns()
                  .retrieve(
                      run.id(),
                      ScheduleRunRetrieveParams.builder().identityId(f.identity.id).build(),
                      options());
          if (!Objects.equals(previous, run.status())) {
            log(
                out,
                "run_status",
                "id",
                run.id(),
                "status",
                run.status(),
                "session_id",
                run.sessionId());
            previous = run.status();
          }
          if (Arrays.asList("completed", "failed", "skipped").contains(run.status())) break;
          pause(deadline, "Schedule Run did not finish within four minutes");
        }
        f.safeToClean = true;
        require("completed".equals(run.status()), "Schedule Run did not complete successfully");
        verifySession(run.sessionId(), f.identity.id, f.marker);
        ScheduleRunListResponse runs =
            client
                .scheduleRuns()
                .list(
                    ScheduleRunListParams.builder()
                        .identityId(f.identity.id)
                        .scheduleId(schedule)
                        .limit(10)
                        .build(),
                    options());
        String id = run.id();
        require(
            runs.data().stream().anyMatch(item -> id.equals(item.id())),
            "Run missing from Schedule listing");
        log(out, "schedule_verified", "run_id", id, "session_id", run.sessionId());
      } catch (Exception e) {
        failure = e;
        throw e;
      } finally {
        if (schedule != null) {
          try {
            client.schedules().archive(schedule, write(f.key + "-archive-schedule"));
            log(out, "archived_schedule", "id", schedule);
          } catch (RuntimeException cleanup) {
            log(out, "cleanup_failed_schedule", "id", schedule);
            if (failure != null) failure.addSuppressed(cleanup);
            else throw cleanup;
          }
        }
      }
    }
  }

  void batch() throws Exception {
    try (Fixture f = new Fixture()) {
      String file = null, batchId = null;
      boolean submitted = false;
      Exception failure = null;
      try {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("custom_id", f.key);
        row.put("template_id", f.template);
        row.put("identity_id", f.identity.id);
        row.put("body", Collections.singletonMap("input", "Reply with exactly " + f.marker));
        file =
            client
                .files()
                .upload(
                    FileUploadParams.builder()
                        .file(
                            Upload.bytes(
                                (JsonValue.of(row).toJson() + "\n")
                                    .getBytes(StandardCharsets.UTF_8),
                                "java-batch.jsonl",
                                "application/jsonl"))
                        .purpose("session_resource")
                        .build(),
                    options())
                .id();
        log(out, "created_batch_input", "id", file, "create_key", f.key + "-batch");
        f.safeToClean = false;
        submitted = true;
        Batch batch =
            client
                .batches()
                .create(
                    BatchCreateParams.builder()
                        .inputFileId(file)
                        .completionWindow("24h")
                        .ignoreIdleWindow(true)
                        .build(),
                    write(f.key + "-batch"));
        batchId = batch.id();
        log(out, "created_batch", "id", batchId, "request_id", batch.requestId());
        batch = awaitBatch(batchId, Duration.ofMinutes(4));
        f.safeToClean = true;
        require("completed".equals(batch.status()), "Batch did not complete successfully");
        BatchRequestCounts counts = batch.requestCounts();
        require(
            counts != null
                && Long.valueOf(1).equals(counts.completed())
                && Long.valueOf(0).equals(counts.failed()),
            "Batch did not report one completed and zero failed task");
        BatchTaskListResponse tasks =
            client
                .batches()
                .tasks()
                .list(batchId, BatchTaskListParams.builder().limit(10).build(), options());
        require(
            tasks.data().size() == 1
                && !tasks.hasNextPage()
                && f.key.equals(tasks.data().get(0).customId())
                && "completed".equals(tasks.data().get(0).status()),
            "Batch task result differs from submitted task");
        BatchFile output = client.batches().getOutput(batchId, options());
        String session =
            validateBatchOutput(download(output.url()), f.key, f.identity.id, f.template, f.marker);
        verifySession(session, f.identity.id, f.marker);
        log(
            out,
            "batch_verified",
            "id",
            batchId,
            "session_id",
            session,
            "completed",
            counts.completed());
      } catch (Exception e) {
        failure = e;
        throw e;
      } finally {
        Exception cleanupFailure = null;
        if (batchId != null && !f.safeToClean) {
          try {
            Batch state = client.batches().retrieve(batchId, options());
            if (!terminal(state.status())) {
              client.batches().cancel(batchId, write(f.key + "-cancel-batch"));
              log(out, "cancelled_batch", "id", batchId);
              awaitBatch(batchId, Duration.ofMinutes(1));
            }
            f.safeToClean = true;
          } catch (Exception e) {
            cleanupFailure = e;
            log(out, "cleanup_failed_batch", "id", batchId);
          }
        }
        if (file != null) {
          if (!submitted || f.safeToClean) {
            try {
              client.files().delete(file, write(f.key + "-delete-input"));
              log(out, "deleted_batch_input", "id", file);
            } catch (RuntimeException e) {
              log(out, "cleanup_failed_batch_input", "id", file);
              if (cleanupFailure == null) cleanupFailure = e;
              else cleanupFailure.addSuppressed(e);
            }
          } else log(out, "retained_batch_input", "id", file, "batch_id", batchId);
        }
        if (cleanupFailure != null) {
          if (failure != null) failure.addSuppressed(cleanupFailure);
          else throw cleanupFailure;
        }
      }
    }
  }

  private Batch awaitBatch(String id, Duration timeout) throws Exception {
    long deadline = System.nanoTime() + timeout.toNanos();
    String previous = null;
    while (true) {
      Batch batch = client.batches().retrieve(id, options());
      if (!Objects.equals(previous, batch.status())) {
        log(
            out,
            "batch_status",
            "id",
            id,
            "status",
            batch.status(),
            "queue_reason",
            batch.queueReason());
        previous = batch.status();
      }
      if (terminal(batch.status())) return batch;
      pause(deadline, "Batch did not reach a terminal state before deadline");
    }
  }

  private static boolean terminal(String status) {
    return Arrays.asList("completed", "failed", "cancelled", "expired").contains(status);
  }

  static String validateBatchOutput(
      String jsonl, String custom, String identity, String template, String marker) {
    String[] lines = jsonl.trim().split("\\r?\\n");
    require(lines.length == 1, "Batch output must contain exactly one result");
    JsonValue row = JsonValue.parse(lines[0]);
    require(
        custom.equals(row.get("custom_id").stringOrNull())
            && identity.equals(row.get("identity_id").stringOrNull())
            && template.equals(row.get("template_id").stringOrNull()),
        "Batch output ownership differs");
    require(
        "completed".equals(row.get("status").stringOrNull()),
        "Batch output status is not completed");
    require(
        row.get("error").isNull() || row.get("error").isMissing(),
        "Batch output contains an error");
    require(
        !row.get("response").isMissing() && row.get("response").toJson().contains(marker),
        "Batch output lacks expected response marker");
    String session = row.get("session_id").stringOrNull();
    require(session != null && !session.isEmpty(), "Batch output lacks Session ID");
    return session;
  }

  static String download(String address) throws IOException {
    URL url = new URL(address);
    require(
        "https".equals(url.getProtocol())
            || ("http".equals(url.getProtocol()) && "localhost".equals(url.getHost())),
        "Batch download requires HTTPS (localhost allowed for tests)");
    require(url.getUserInfo() == null, "Batch download URL contains user info");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setInstanceFollowRedirects(false);
    connection.setConnectTimeout(30000);
    connection.setReadTimeout(30000);
    try {
      require(connection.getResponseCode() == 200, "Batch output download did not return 200");
      try (InputStream in = connection.getInputStream();
          ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
        byte[] chunk = new byte[4096];
        for (int n; (n = in.read(chunk)) != -1; ) {
          require(buffer.size() + n <= 2 * 1024 * 1024, "Batch output exceeds two MiB");
          buffer.write(chunk, 0, n);
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
      }
    } finally {
      connection.disconnect();
    }
  }

  private void verifySession(String id, String identity, String marker) {
    require(id != null && !id.isEmpty(), "Execution did not return a Session");
    Session session = client.sessions().retrieve(id, options());
    require(identity.equals(session.identityId()), "Execution Session belongs to another Identity");
    boolean matched = false, idle = false;
    int pages = 0;
    SessionEventListResponse first =
        client
            .sessions()
            .events()
            .list(id, SessionEventListParams.builder().order("asc").limit(100).build(), options());
    for (SessionEventListResponse page : first.iterPages()) {
      for (SessionEvent event : page.data()) {
        log(out, "execution_event", "session_id", id, "id", event.id(), "type", event.type());
        require(
            !"session.error".equals(event.type())
                && !"session.status_terminated".equals(event.type()),
            "Execution history contains a failure");
        if ("agent.message".equals(event.type()) && marker.equals(text(event).trim()))
          matched = true;
        if (matched && "session.status_idle".equals(event.type())) {
          JsonValue stop = event.toJsonValue().get("stop_reason");
          String reason = stop.isObject() ? stop.get("type").stringOrNull() : stop.stringOrNull();
          require(
              reason == null
                  || reason.isEmpty()
                  || "end_turn".equals(reason)
                  || "stop_sequence".equals(reason),
              "Execution stopped abnormally");
          idle = true;
        }
      }
      require(++pages < 10 || !page.hasNextPage(), "Execution history exceeded page limit");
    }
    require(
        matched && idle,
        "Missing exact assistant reply or subsequent idle event in execution history");
    log(out, "execution_session_verified", "id", id);
  }

  void usage() {
    LocalDateTime end = LocalDateTime.now(ZoneId.of("Asia/Shanghai")).truncatedTo(ChronoUnit.HOURS);
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    String start = end.minusHours(24).format(format), stop = end.format(format);
    log(out, "usage_window", "start_at", start, "end_at", stop, "timezone", "Asia/Shanghai");
    UsageListParams params = UsageListParams.builder().startAt(start).endAt(stop).limit(1).build();
    IdentityUsageListResponse identities = client.usage().listIdentities(params, options());
    Set<String> seen = new HashSet<>();
    int pages = 0;
    for (IdentityUsageListResponse page : identities.iterPages()) {
      for (IdentityUsage item : page.data()) {
        require(
            item.identityId() != null && seen.add(item.identityId()),
            "Usage Identity cursor repeated or lacks ID");
        validateUsage(item.activeSeconds(), item.credits(), item.sessionCount());
      }
      if (++pages == 2) break;
    }
    log(
        out,
        "identity_usage_verified",
        "rows",
        seen.size(),
        "pages",
        pages,
        "empty",
        seen.isEmpty(),
        "request_id",
        identities.requestId());
    if (!seen.isEmpty()) {
      String id = seen.iterator().next();
      IdentityUsageListResponse filtered =
          client
              .usage()
              .listIdentities(
                  UsageListParams.builder()
                      .startAt(start)
                      .endAt(stop)
                      .identityIds(Collections.singletonList(id))
                      .limit(10)
                      .build(),
                  options());
      require(
          !filtered.data().isEmpty()
              && filtered.data().stream().allMatch(item -> id.equals(item.identityId())),
          "Identity Usage filter differs");
    }
    TemplateUsageListResponse templates = client.usage().listTemplates(params, options());
    seen.clear();
    pages = 0;
    for (TemplateUsageListResponse page : templates.iterPages()) {
      for (TemplateUsage item : page.data()) {
        require(
            item.templateId() != null && seen.add(item.templateId()),
            "Usage Template cursor repeated or lacks ID");
        validateUsage(item.activeSeconds(), item.credits(), item.sessionCount());
        require(
            item.activeIdentities() != null && item.activeIdentities() >= 0,
            "Invalid active Identity count");
      }
      if (++pages == 2) break;
    }
    log(
        out,
        "template_usage_verified",
        "rows",
        seen.size(),
        "pages",
        pages,
        "empty",
        seen.isEmpty(),
        "request_id",
        templates.requestId());
    if (!seen.isEmpty()) {
      String id = seen.iterator().next();
      TemplateUsageListResponse filtered =
          client
              .usage()
              .listTemplates(
                  UsageListParams.builder()
                      .startAt(start)
                      .endAt(stop)
                      .templateIds(Collections.singletonList(id))
                      .limit(10)
                      .build(),
                  options());
      require(
          !filtered.data().isEmpty()
              && filtered.data().stream().allMatch(item -> id.equals(item.templateId())),
          "Template Usage filter differs");
    }
  }

  private static void validateUsage(
      java.math.BigDecimal seconds, java.math.BigDecimal credits, Long sessions) {
    require(
        seconds != null
            && seconds.signum() >= 0
            && credits != null
            && credits.signum() >= 0
            && sessions != null
            && sessions >= 0,
        "Usage counters are missing or negative");
  }

  private static void pause(long deadline, String message) throws InterruptedException {
    require(System.nanoTime() < deadline, message);
    Thread.sleep(2000);
  }

  private final class Fixture implements AutoCloseable {
    final IdentityFixture identity = new IdentityFixture(client, out);
    final String key = UUID.randomUUID().toString();
    // Identifier-shaped replies can trigger the model's internal-information refusal.
    final String marker = "Hello from the Java SDK.";
    final String environment;
    final String template;
    boolean safeToClean = true;

    Fixture() {
      try {
        environment = client.templates().retrieve(sourceTemplate, options()).environmentId();
        require(environment != null, "Source Template has no Environment");
        template =
            client
                .templates()
                .create(
                    TemplateCreateParams.builder()
                        .name("sdk-java-execution-" + key)
                        .environmentId(environment)
                        .modelId("auto")
                        .system(
                            "Follow the user's request. For an exact reply request, return only the"
                                + " requested text.")
                        .build(),
                    write(key + "-template"))
                .id();
        log(out, "created_template", "id", template);
      } catch (RuntimeException error) {
        try {
          identity.close();
        } catch (RuntimeException cleanup) {
          error.addSuppressed(cleanup);
        }
        throw error;
      }
    }

    @Override
    public void close() throws Exception {
      if (!safeToClean) {
        log(
            out,
            "retained_execution_resources",
            "identity_id",
            identity.id,
            "template_id",
            template,
            "reason",
            "asynchronous execution not confirmed terminal");
        return;
      }
      // Stop only Sessions belonging to this fixture before the common archive/clear cleanup.
      for (Session session :
          client
              .sessions()
              .list(
                  SessionListParams.builder()
                      .identityIds(Collections.singletonList(identity.id))
                      .limit(100)
                      .build(),
                  options())) {
        require(identity.id.equals(session.identityId()), "Cleanup Session ownership differs");
        if (!Arrays.asList("idle", "terminated").contains(session.status())) {
          client.sessions().cancel(session.id(), write(key + "-cancel-" + session.id()));
          long deadline = System.nanoTime() + Duration.ofMinutes(1).toNanos();
          while (!Arrays.asList("idle", "terminated")
              .contains(client.sessions().retrieve(session.id(), options()).status()))
            pause(deadline, "Execution Session did not stop for cleanup");
        }
      }
      Exception failure = null;
      try {
        identity.close();
      } catch (Exception error) {
        failure = error;
      }
      try {
        client.templates().archive(template, write(key + "-archive-template"));
        log(out, "archived_template", "id", template);
      } catch (RuntimeException error) {
        log(out, "cleanup_failed_template", "id", template);
        if (failure == null) failure = error;
        else failure.addSuppressed(error);
      }
      if (failure != null) throw failure;
    }
  }
}
