package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ApiStatusException;
import com.qoder.cloudagents.errors.RealtimeException;
import com.qoder.cloudagents.forward.*;
import com.qoder.cloudagents.forward.realtime.*;
import com.qoder.cloudagents.streaming.EventStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

/** Opt-in, packaged-JAR acceptance. Never run by Maven's default test lifecycle. */
public final class ForwardLiveCheck {
  private final ForwardClient client;
  private final PrintStream out;
  private final String template;
  private String phase = "setup";

  ForwardLiveCheck(ForwardClient client, String template, PrintStream out) {
    this.client = client;
    this.template = template;
    this.out = out;
  }

  public static void main(String[] args) {
    String group = args.length == 1 ? args[0] : "";
    int exit = 1;
    ForwardLiveCheck check = null;
    try {
      validateGate(group, System.getenv());
      String jar =
          ForwardClient.class.getProtectionDomain().getCodeSource().getLocation().getPath();
      require(jar.endsWith(".jar"), "SDK must be loaded from the installed JAR");
      try (ExampleClient setup = ExampleClient.fromEnvironment()) {
        check = new ForwardLiveCheck(setup.client(), required("QODER_TEMPLATE_ID"), System.out);
        check.record(
            "start", "group", group, "java", System.getProperty("java.version"), "sdk_jar", jar);
        check.run(group);
        check.record("passed", "group", group);
        exit = 0;
      }
    } catch (Exception error) {
      reportFailure(System.out, check == null ? "setup" : check.phase, error);
    }
    System.exit(exit);
  }

  static void validateGate(String group, Map<String, String> env) {
    require(
        Arrays.asList(
                "read",
                "resources",
                "session",
                "realtime-text",
                "realtime-pcm",
                "schedule",
                "batch",
                "usage",
                "sa-tokens",
                "realtime-work")
            .contains(group),
        "Select a documented live-check group");
    require("true".equals(env.get("QODER_RUN_LIVE")), "Set QODER_RUN_LIVE=true");
    if (!"read".equals(group) && !"usage".equals(group))
      require("true".equals(env.get("QODER_ALLOW_WRITE")), "Set QODER_ALLOW_WRITE=true");
    if ("session".equals(group)
        || "schedule".equals(group)
        || "batch".equals(group)
        || group.startsWith("realtime-"))
      require("true".equals(env.get("QODER_ALLOW_EXECUTION")), "Set QODER_ALLOW_EXECUTION=true");
  }

  private void run(String group) throws Exception {
    if ("read".equals(group)) read();
    else if ("resources".equals(group)) resources();
    else if ("session".equals(group)) session();
    else if ("sa-tokens".equals(group)) {
      phase = group;
      ForwardTokenLiveCheck.run(
          client, required("QODER_FORWARD_BASE_URL"), required("QODER_SA_KEY"), out);
    } else if (Arrays.asList("schedule", "batch", "usage").contains(group)) {
      phase = group;
      new ForwardExecutionChecks(client, template, out).run(group);
    } else realtime(group);
  }

  void read() throws Exception {
    phase = "models";
    ModelListResponse models = client.models().list(options());
    require(!models.data().isEmpty(), "Model list is empty");
    record("models", "count", models.data().size(), "request_id", models.requestId());
    phase = "template";
    Template found = client.templates().retrieve(template, options());
    require(template.equals(found.id()), "Template ID differs");
    record(
        "template",
        "id",
        found.id(),
        "environment_id",
        found.environmentId(),
        "request_id",
        found.requestId());
    phase = "async_templates";
    TemplateListResponse page =
        client
            .async()
            .templates()
            .list(TemplateListParams.builder().limit(1).build(), options())
            .get();
    record("async_templates", "count", page.data().size(), "request_id", page.requestId());
  }

  private void resources() throws Exception {
    // Every reused example creates and cleans its own resources; the supplied Template is
    // read-only.
    List<Exception> failures = new ArrayList<>();
    scenario("environment_file", () -> EnvironmentFileExample.run(client, out), failures);
    scenario("memory", () -> MemoryStoreLifecycleExample.run(client, out), failures);
    scenario("skill_vault", () -> SkillVaultExample.run(client, out), failures);
    scenario(
        "template_lifecycle",
        () -> {
          Template source = client.templates().retrieve(template, options());
          require(source.environmentId() != null, "Test Template has no Environment");
          TemplateLifecycleExample.run(client, source.environmentId(), "auto", out);
        },
        failures);
    scenario("identity_config", () -> IdentityConfigExample.run(client, template, out), failures);
    scenario(
        "drive",
        () -> {
          try (IdentityFixture identity = new IdentityFixture(client, out)) {
            DriveExample.run(client, identity.id, out);
          }
        },
        failures);
    if (!failures.isEmpty()) {
      phase = "resources";
      CheckFailure failure = new CheckFailure("Resource scenario failures: " + failures.size());
      failures.forEach(failure::addSuppressed);
      throw failure;
    }
  }

  private void scenario(String name, Operation operation, List<Exception> failures) {
    phase = name;
    record("scenario_start", "name", name);
    try {
      operation.run();
      record("scenario_passed", "name", name);
    } catch (Exception error) {
      reportFailure(out, name, error);
      failures.add(error);
    }
  }

  void session() throws Exception {
    String batch = UUID.randomUUID().toString();
    String ownedTemplate = null;
    Exception primary = null;
    try (IdentityFixture identity = new IdentityFixture(client, out)) {
      phase = "session_template";
      Template source = client.templates().retrieve(template, options());
      require(source.environmentId() != null, "Test Template has no Environment");
      ownedTemplate =
          client
              .templates()
              .create(
                  TemplateCreateParams.builder()
                      .name("sdk-java-stream-" + batch)
                      .environmentId(source.environmentId())
                      .modelId("auto")
                      .system(
                          "Follow the user's request. For an exact reply request, return only the"
                              + " requested text.")
                      .build(),
                  write(batch + "-template"))
              .id();
      record("created_template", "id", ownedTemplate);
      phase = "session_create";
      Session created =
          client
              .sessions()
              .create(
                  SessionCreateParams.builder()
                      .identityId(identity.id)
                      .templateId(ownedTemplate)
                      .title("Java live SSE check")
                      .build(),
                  write(batch + "-session"));
      record("created_session", "id", created.id(), "request_id", created.requestId());
      String marker = "java_check_" + batch.replace("-", "");
      phase = "session_send";
      SessionEventSendResponse sent =
          client
              .sessions()
              .events()
              .send(
                  created.id(),
                  SessionEventSendParams.builder()
                      .events(
                          Collections.singletonList(
                              SessionEventParam.builder()
                                  .type("user.message")
                                  .content(EventContent.text("Reply with exactly " + marker))
                                  .build()))
                      .build(),
                  write(batch + "-input"));
      require(
          sent.data() != null && sent.data().size() == 1 && sent.data().get(0).id() != null,
          "Send response must return the submitted user event ID");
      String inputCursor = sent.data().get(0).id();
      phase = "session_sse";
      String thread = null;
      boolean matched = false, idle = false;
      try (EventStream<SessionEvent> stream =
          client
              .sessions()
              .events()
              .streamEvents(
                  created.id(),
                  SessionEventStreamParams.builder().lastEventId(inputCursor).build(),
                  executionOptions())) {
        for (SessionEvent event : stream) {
          recordEvent(event);
          require(
              !"session.error".equals(event.type())
                  && !"session.status_terminated".equals(event.type()),
              "Session execution failed before idle");
          if (event.sessionThreadId() != null) thread = event.sessionThreadId();
          if ("agent.message".equals(event.type()) && marker.equals(text(event).trim())) {
            matched = true;
          }
          if (matched && "session.status_idle".equals(event.type())) {
            JsonValue stop = event.toJsonValue().get("stop_reason");
            String reason = stop.isObject() ? stop.get("type").stringOrNull() : stop.stringOrNull();
            require(
                reason == null
                    || reason.isEmpty()
                    || "end_turn".equals(reason)
                    || "stop_sequence".equals(reason),
                "Session stopped without completing the turn");
            idle = true;
            break;
          }
        }
        require(matched && idle, "Missing exact assistant reply or subsequent idle event");
        require(stream.lastEventId() != null, "Missing SSE checkpoint");
        record("session_verified", "session_id", created.id(), "checkpoint", stream.lastEventId());
      }
      require(thread != null && !thread.isEmpty(), "Session events have no Thread ID");
      phase = "thread_sse";
      boolean threadMatched = false;
      try (EventStream<SessionEvent> stream =
          client
              .sessions()
              .threads()
              .events()
              .streamEvents(
                  created.id(),
                  thread,
                  SessionThreadEventStreamParams.builder().lastEventId(inputCursor).build(),
                  options())) {
        for (SessionEvent event : stream) {
          recordEvent(event);
          if ("agent.message".equals(event.type()) && marker.equals(text(event).trim())) {
            threadMatched = true;
            break;
          }
        }
      }
      require(threadMatched, "Thread SSE did not contain the matching assistant message");
      record("thread_verified", "thread_id", thread);
    } catch (Exception error) {
      primary = error;
      throw error;
    } finally {
      if (ownedTemplate != null) {
        // Preserve cleanup failure evidence without replacing a pending primary exception.
        try {
          client.templates().archive(ownedTemplate, write(batch + "-archive"));
          record("archived_template", "id", ownedTemplate);
        } catch (RuntimeException error) {
          reportFailure(out, "template_cleanup", error);
          record("cleanup_failed_template", "id", ownedTemplate);
          if (primary != null) primary.addSuppressed(error);
          else throw error;
        }
      }
    }
  }

  private void realtime(String group) throws Exception {
    boolean pcm = "realtime-pcm".equals(group), work = "realtime-work".equals(group);
    byte[] audio = null;
    if (pcm) {
      Path path = Paths.get(required("QODER_PCM_FILE"));
      require(Files.size(path) <= 960000, "PCM fixture exceeds 30 seconds");
      audio = Files.readAllBytes(path);
      RealtimeExample.validateInput(null, audio);
    }
    phase = "realtime_auth";
    ServiceAccountToken issued = null;
    ForwardClient runtime = client;
    Exception primary = null;
    String key = null;
    try {
      // Forward-bound SAT is required by the supplied SA Realtime templates.
      if ("sa".equals(System.getenv("QODER_AUTH_MODE")) && System.getenv("QODER_SA_KEY") != null) {
        key = required("QODER_SA_KEY");
        issued =
            client
                .serviceAccountTokens()
                .create(
                    key,
                    ServiceAccountTokenCreateParams.builder()
                        .ttlSeconds(600)
                        .metadata(Collections.singletonMap("purpose", "java-sdk-live-check"))
                        .build(),
                    options());
        record("issued_sat", "auth_token_id", issued.authTokenId());
        runtime =
            ForwardClient.builder()
                .baseUrl(required("QODER_FORWARD_BASE_URL"))
                .authMode(AuthMode.SA)
                .accessToken(issued.accessToken())
                .maxRetries(0)
                .build();
      }
      try (IdentityFixture identity = new IdentityFixture(runtime, out)) {
        phase = "realtime_create";
        String conversation =
            RealtimeExample.create(runtime, identity.id, template, UUID.randomUUID().toString());
        record("created_conversation", "id", conversation);
        phase = group;
        if (work) RealtimeWorkLiveCheck.run(runtime, conversation, out);
        else RealtimeExample.run(runtime, conversation, pcm ? null : "你好，请用一句话介绍你自己。", audio, out);
        phase = "realtime_history";
        RealtimeHistoryPage page =
            runtime
                .realtime()
                .conversations()
                .history(
                    conversation,
                    RealtimeHistoryParams.builder().limit(1).types("message").build(),
                    options());
        require(!page.events().isEmpty(), "Realtime history has no persisted messages");
        record("history", "first_id", page.events().get(0).id(), "has_next", page.hasNextPage());
        if (page.hasNextPage()) {
          RealtimeHistoryPage next = page.getNextPage();
          require(!next.events().isEmpty(), "Next history page is empty");
          require(
              !page.events().get(0).id().equals(next.events().get(0).id()),
              "History cursor repeated a message");
          record("history_next", "first_id", next.events().get(0).id());
        }
      }
    } catch (Exception error) {
      primary = error;
      throw error;
    } finally {
      if (runtime != client) runtime.close();
      if (issued != null) {
        try {
          ServiceAccountTokenRevokeParams.Builder params =
              ServiceAccountTokenRevokeParams.builder().reason("java-sdk-live-complete");
          if (issued.credentialId() != null) params.targetCredentialId(issued.credentialId());
          if (issued.expiresAt() != null) params.targetExpiresAt(issued.expiresAt());
          require(
              Boolean.TRUE.equals(
                  client
                      .serviceAccountTokens()
                      .revoke(issued.authTokenId(), key, params.build(), options())
                      .revoked()),
              "Temporary SAT revocation was not acknowledged");
          record("revoked_sat", "auth_token_id", issued.authTokenId());
        } catch (Exception cleanup) {
          record("cleanup_failed_sat", "auth_token_id", issued.authTokenId());
          if (primary != null) primary.addSuppressed(cleanup);
          else throw cleanup;
        }
      }
    }
  }

  static String text(SessionEvent event) {
    if (event.text() != null) return event.text();
    EventContent content = event.content();
    if (content == null) return "";
    if (content.isString()) return content.asString();
    StringBuilder result = new StringBuilder();
    for (ContentBlock block : content.asBlocks())
      if ("text".equals(block.type()) && block.text() != null) result.append(block.text());
    return result.toString();
  }

  private void recordEvent(SessionEvent event) {
    record(
        "sse_event",
        "id",
        event.id(),
        "type",
        event.type(),
        "thread_id",
        event.sessionThreadId(),
        "text_length",
        text(event).length());
  }

  static final class IdentityFixture implements AutoCloseable {
    final ForwardClient client;
    final PrintStream out;
    final String id;
    final String batch = UUID.randomUUID().toString();

    IdentityFixture(ForwardClient client, PrintStream out) {
      this.client = client;
      this.out = out;
      log(out, "identity_create_start", "external_id", "sdk-java-live-" + batch);
      id =
          client
              .identities()
              .create(
                  IdentityCreateParams.builder()
                      .externalId("sdk-java-live-" + batch)
                      .name("Java live check")
                      .build(),
                  write(batch + "-identity"))
              .id();
      require(id != null && !id.isEmpty(), "Identity response has no ID");
      log(out, "created_identity", "id", id);
    }

    @Override
    public void close() {
      try {
        // Filter by this newly created Identity; never archive another caller's Session.
        SessionListResponse page =
            client
                .sessions()
                .list(
                    SessionListParams.builder()
                        .identityIds(Collections.singletonList(id))
                        .limit(100)
                        .build(),
                    options());
        for (Session session : page) {
          client.sessions().archive(session.id(), write(batch + "-archive-" + session.id()));
          log(out, "archived_session", "id", session.id());
        }
        IdentityClearResponse cleared =
            client
                .identities()
                .clear(
                    id,
                    IdentityClearParams.builder().reason("Java live check cleanup").build(),
                    write(batch + "-clear"));
        require("completed".equals(cleared.status()), "Identity clear did not complete");
        client.identities().delete(id, write(batch + "-delete"));
        log(out, "deleted_identity", "id", id);
      } catch (RuntimeException error) {
        log(out, "cleanup_failed_identity", "id", id);
        throw error;
      }
    }
  }

  static void reportFailure(PrintStream out, String phase, Throwable error) {
    log(out, "failed", "phase", phase, "error_type", error.getClass().getSimpleName());
    if (error instanceof CheckFailure) log(out, "assertion", "detail", error.getMessage());
    if (error instanceof ApiStatusException) {
      ApiStatusException status = (ApiStatusException) error;
      log(
          out,
          "http_failure",
          "status",
          status.statusCode(),
          "code",
          status.code(),
          "request_id",
          status.requestId());
    }
    if (error instanceof RealtimeException) {
      RealtimeException realtime = (RealtimeException) error;
      log(
          out,
          "realtime_failure",
          "conversation_id",
          realtime.conversationId(),
          "request_id",
          realtime.requestId(),
          "close_code",
          realtime.closeCode());
    }
    if (error.getCause() != null && error.getCause() != error)
      reportFailure(out, phase + ".cause", error.getCause());
    for (Throwable suppressed : error.getSuppressed())
      reportFailure(out, phase + ".suppressed", suppressed);
  }

  private void record(String event, Object... values) {
    log(out, event, values);
  }

  static void log(PrintStream out, String event, Object... values) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("event", event);
    for (int i = 0; i < values.length; i += 2) row.put((String) values[i], values[i + 1]);
    out.println(JsonValue.of(row).toJson());
  }

  static void require(boolean condition, String message) {
    if (!condition) throw new CheckFailure(message);
  }

  private static String required(String name) {
    String value = System.getenv(name);
    require(value != null && !value.trim().isEmpty(), "Set " + name);
    return value;
  }

  static RequestOptions options() {
    return RequestOptions.builder().maxRetries(0).totalTimeout(Duration.ofSeconds(30)).build();
  }

  static RequestOptions write(String key) {
    return options().toBuilder().idempotencyKey(key).build();
  }

  private static RequestOptions executionOptions() {
    return RequestOptions.builder().maxRetries(0).totalTimeout(Duration.ofMinutes(3)).build();
  }

  private interface Operation {
    void run() throws Exception;
  }

  private static final class CheckFailure extends IllegalStateException {
    CheckFailure(String message) {
      super(message);
    }
  }
}
