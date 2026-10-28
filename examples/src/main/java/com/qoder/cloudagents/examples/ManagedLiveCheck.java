package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ManagedClient;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ApiStatusException;
import com.qoder.cloudagents.managed.*;
import com.qoder.cloudagents.streaming.EventStream;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/** Opt-in Managed integration checks. Creates only sdk-contract resources owned by this run. */
public final class ManagedLiveCheck {
  private final ManagedClient client;
  private final PrintStream out;
  private final String name =
      "sdk-contract-managed-java-" + UUID.randomUUID().toString().substring(0, 8);
  private final String model;
  private final Deque<Runnable> cleanup = new ArrayDeque<>();

  public ManagedLiveCheck(ManagedClient client, String model, PrintStream out) {
    this.client = client;
    this.model = model;
    this.out = out;
  }

  public static void main(String[] args) throws Exception {
    String group = args.length == 1 ? args[0] : "";
    validate(group, System.getenv());
    try (ManagedExampleClient setup = ManagedExampleClient.fromEnvironment()) {
      String model = System.getenv("QODER_MODEL");
      try (ManagedClient traced =
          setup
              .client()
              .withOptions(
                  b ->
                      b.interceptor(
                          (request, chain) -> {
                            java.util.concurrent.CompletableFuture<
                                    com.qoder.cloudagents.core.http.HttpResponse>
                                pending = chain.proceed(request);
                            pending.whenComplete(
                                (response, error) -> {
                                  if (response != null)
                                    System.out.println(
                                        "http="
                                            + request.method()
                                            + " "
                                            + request.uri().getPath()
                                            + " status="
                                            + response.statusCode());
                                });
                            return pending;
                          }))) {
        new ManagedLiveCheck(traced, model == null ? "auto" : model, System.out).run(group);
      }
    } catch (ApiStatusException error) {
      System.err.println(
          "managed_failed status=" + error.statusCode() + " request_id=" + error.requestId());
      throw error;
    }
  }

  public static void validate(String group, Map<String, String> env) {
    check(
        Arrays.asList("read", "resources", "session", "deployment", "work").contains(group),
        "Choose read, resources, session, deployment or work");
    check("true".equals(env.get("QODER_RUN_LIVE")), "Set QODER_RUN_LIVE=true");
    if (!"read".equals(group))
      check("true".equals(env.get("QODER_ALLOW_WRITE")), "Set QODER_ALLOW_WRITE=true");
    if (Arrays.asList("session", "deployment", "work").contains(group))
      check("true".equals(env.get("QODER_ALLOW_EXECUTION")), "Set QODER_ALLOW_EXECUTION=true");
  }

  public void run(String group) throws Exception {
    try {
      if ("read".equals(group)) read();
      else if ("resources".equals(group)) resources();
      else if ("session".equals(group)) session();
      else if ("deployment".equals(group)) deployment();
      else if ("work".equals(group)) work();
      else throw new IllegalArgumentException("Unknown group");
      out.println("passed=" + group);
    } finally {
      while (!cleanup.isEmpty())
        try {
          cleanup.pop().run();
        } catch (RuntimeException error) {
          out.println("cleanup_failed type=" + error.getClass().getSimpleName());
        }
    }
  }

  private RequestOptions options(String step) {
    return RequestOptions.builder()
        .idempotencyKey(name + "-" + step)
        .totalTimeout(Duration.ofSeconds(120))
        .build();
  }

  private void read() throws Exception {
    check(!client.models().list().data().isEmpty(), "Model catalog is empty");
    check(
        client
            .agents()
            .search(AgentSearchParams.builder().name(name).limit(1).build())
            .data()
            .isEmpty(),
        "Unexpected Agent match");
    client.deployments().search(DeploymentSearchParams.builder().name(name).limit(1).build());
    client
        .sessions()
        .search(
            SessionSearchParams.builder()
                .metadata(Collections.singletonMap("sdk_contract", name))
                .limit(1)
                .build());
    client.environments().search(EnvironmentSearchParams.builder().name(name).limit(1).build());
    client.files().search(FileSearchParams.builder().name(name).limit(1).build());
    client.skills().search(SkillSearchParams.builder().displayTitle(name).limit(1).build());
    client.vaults().search(VaultSearchParams.builder().name(name).limit(1).build());
    client.memoryStores().search(MemoryStoreSearchParams.builder().name(name).limit(1).build());
    client.dreams().list(DreamListParams.builder().limit(1).build());
    check(
        client.async().models().withResponse().list().get().requestId() != null,
        "Missing request ID");
  }

  private Environment environment(String type) {
    Environment e =
        client
            .environments()
            .create(
                EnvironmentCreateParams.builder()
                    .name(name)
                    .config(EnvironmentConfig.builder().type(type).build())
                    .build(),
                options("environment"));
    cleanup.push(() -> client.environments().delete(e.id()));
    out.println("environment=" + e.id());
    return e;
  }

  private Agent agent() {
    Agent a =
        client
            .agents()
            .create(
                AgentCreateParams.builder()
                    .name(name)
                    .model(model)
                    .system("Reply with exactly SDK_MANAGED_OK, no other text.")
                    .build(),
                options("agent"));
    cleanup.push(() -> client.agents().archive(a.id()));
    out.println("agent=" + a.id());
    return a;
  }

  private void resources() throws Exception {
    Environment env = environment("self_hosted");
    Agent original = agent();
    Agent changed =
        client
            .agents()
            .update(
                original.id(),
                AgentUpdateParams.builder().version(original.version()).description(name).build(),
                options("agent-update"));
    check(changed.version() > original.version(), "Agent version did not advance");
    check(
        client
            .agents()
            .retrieve(
                original.id(), AgentRetrieveParams.builder().version(original.version()).build())
            .version()
            .equals(original.version()),
        "Version retrieval mismatch");
    check(
        !client
            .agents()
            .versions()
            .list(original.id(), AgentVersionListParams.builder().limit(1).build())
            .data()
            .isEmpty(),
        "Version list is empty");
    client
        .environments()
        .update(
            env.id(),
            EnvironmentUpdateParams.builder().description(name).build(),
            options("environment-update"));
    MemoryStore store =
        client
            .memoryStores()
            .create(MemoryStoreCreateParams.builder().name(name).build(), options("store"));
    cleanup.push(() -> client.memoryStores().delete(store.id()));
    Memory memory =
        client
            .memoryStores()
            .memories()
            .create(
                store.id(),
                MemoryCreateParams.builder().path("qa.md").content("sdk contract").build(),
                options("memory"));
    Memory updated =
        client
            .memoryStores()
            .memories()
            .update(
                store.id(),
                memory.id(),
                MemoryUpdateParams.builder()
                    .content("updated sdk contract")
                    .contentSha256(memory.contentSha256())
                    .build(),
                options("memory-update"));
    check(updated.version() > memory.version(), "Memory version did not advance");
    check(
        !client
            .memoryStores()
            .versions()
            .list(store.id(), MemoryVersionListParams.builder().memoryId(memory.id()).build())
            .data()
            .isEmpty(),
        "Memory versions missing");
    FileMetadata file =
        client
            .files()
            .upload(
                FileUploadParams.builder()
                    .file(
                        Upload.bytes(
                            "sdk contract".getBytes(StandardCharsets.UTF_8),
                            name + ".txt",
                            "text/plain"))
                    .build(),
                options("file"));
    cleanup.push(() -> client.files().delete(file.id()));
    check(client.files().getMetadata(file.id()).sizeBytes() > 0, "File metadata has no size");
    if (Boolean.TRUE.equals(file.downloadable())) {
      try (FileDownload download = client.files().download(file.id(), options("download"))) {
        check(download.body().read() == 's', "Downloaded file mismatch");
      }
    } else {
      check(Boolean.FALSE.equals(file.downloadable()), "Missing downloadable capability flag");
      try {
        client.files().getDownloadUrl(file.id());
        throw new IllegalStateException("A non-downloadable file unexpectedly returned a URL");
      } catch (ApiStatusException error) {
        if (error.statusCode() != 403) throw error;
        out.println("file_not_downloadable_expected_403");
      }
    }
    String skillName = name.toLowerCase(Locale.ROOT);
    byte[] skill =
        ("---\nname: "
                + skillName
                + "\ndescription: SDK acceptance\n---\n# Test\nOnly used for SDK acceptance.\n")
            .getBytes(StandardCharsets.UTF_8);
    Skill s =
        client
            .skills()
            .create(
                SkillCreateParams.builder()
                    .displayTitle(name)
                    .addFile(Upload.bytes(skill, skillName + "/SKILL.md", "text/markdown"))
                    .build(),
                options("skill"));
    cleanup.push(() -> client.skills().delete(s.id()));
    check(s.latestVersion() != null, "Skill version missing");
    check(
        client.skills().versions().retrieve(s.id(), s.latestVersion()).skillId().equals(s.id()),
        "Skill version owner mismatch");
    Vault vault =
        client
            .vaults()
            .create(VaultCreateParams.builder().displayName(name).build(), options("vault"));
    cleanup.push(() -> client.vaults().delete(vault.id()));
    VaultCredential credential =
        client
            .vaults()
            .credentials()
            .create(
                vault.id(),
                VaultCredentialCreateParams.builder()
                    .auth(
                        VaultCredentialCreateAuth.builder()
                            .type("environment_variable")
                            .secretName("SDK_CONTRACT_TEST")
                            .secretValue("temporary-nonsecret")
                            .build())
                    .build(),
                options("credential"));
    client
        .vaults()
        .credentials()
        .archive(vault.id(), credential.id(), options("credential-archive"));
    client.vaults().credentials().delete(vault.id(), credential.id());
  }

  private Session createSession(Agent a, Environment e, String key) {
    Map<String, JsonValue> metadata = new LinkedHashMap<>();
    metadata.put("sdk_contract", JsonValue.of(name));
    metadata.put("__qca_sdk_future", JsonValue.of("kept"));
    Session s =
        client
            .sessions()
            .create(
                SessionCreateParams.builder()
                    .agent(
                        AgentReference.builder()
                            .type("agent")
                            .id(a.id())
                            .version(a.version())
                            .build())
                    .environmentId(e.id())
                    .budget(SessionBudget.builder().type("limit").maxCreditCost("10.00").build())
                    .metadata(metadata)
                    .build(),
                options(key));
    cleanup.push(() -> closeSession(s.id()));
    out.println("session=" + s.id());
    check(
        s.metadata().get("__qca_sdk_created").asString().equals("true"), "Session marker missing");
    check(metadata.size() == 2, "Caller metadata changed");
    return s;
  }

  private void session() throws Exception {
    Environment e = environment("cloud");
    Agent a = agent();
    Session s = createSession(a, e, "session");
    client
        .sessions()
        .update(
            s.id(),
            SessionUpdateParams.builder()
                .agent(
                    SessionAgentPatch.builder()
                        .system("Reply with exactly SDK_MANAGED_OK, no other text.")
                        .build())
                .build(),
            options("session-patch").toBuilder()
                .header("x-qoder-beta", "session-agent-patch-2026-07-21")
                .build());
    SessionEventSendResponse sent =
        client
            .sessions()
            .events()
            .send(
                s.id(),
                SessionEventSendParams.builder()
                    .events(Collections.singletonList(message()))
                    .build(),
                options("message"));
    check(!sent.data().isEmpty(), "No input event ID");
    awaitReply(s.id(), sent.data().get(0).id());
    Session ended = client.sessions().retrieve(s.id());
    check(ended.usage() != null && ended.usage().totalCredits() != null, "Session Credits missing");
    check(!client.sessions().threads().list(s.id()).data().isEmpty(), "Session thread missing");
    client
        .sessions()
        .update(
            s.id(), SessionUpdateParams.builder().budget(null).build(), options("remove-budget"));
  }

  private SessionEventParam message() {
    return SessionEventParam.builder()
        .type("user.message")
        .content(EventContent.text("Reply with exactly SDK_MANAGED_OK."))
        .build();
  }

  private void awaitReply(String id, String cursor) {
    boolean replied = false, idle = false;
    try (EventStream<SessionEvent> stream =
        client
            .sessions()
            .events()
            .streamEvents(
                id,
                SessionEventStreamParams.builder().lastEventId(cursor).build(),
                options("stream"))) {
      for (SessionEvent event : stream) {
        if ("agent.message".equals(event.type()) && event.content() != null)
          replied = event.content().toJsonValue().toJson().contains("SDK_MANAGED_OK");
        if ("session.error".equals(event.type()))
          throw new IllegalStateException("Session emitted an error event");
        if ("session.status_idle".equals(event.type())) {
          idle = true;
          break;
        }
      }
    }
    check(replied && idle, "Session did not return the expected reply and idle event");
  }

  private void deployment() throws Exception {
    Environment e = environment("cloud");
    Agent a = agent();
    Deployment d =
        client
            .deployments()
            .create(
                DeploymentCreateParams.builder()
                    .name(name)
                    .agent(a.id())
                    .environmentId(e.id())
                    .initialEvents(Collections.singletonList(message()))
                    .environmentVariables("SDK_CONTRACT_TEST=true")
                    .build(),
                options("deployment"));
    cleanup.push(() -> client.deployments().archive(d.id()));
    out.println("deployment=" + d.id());
    client.deployments().pause(d.id(), options("pause"));
    client.deployments().unpause(d.id(), options("unpause"));
    DeploymentRun run = client.deployments().run(d.id(), options("run"));
    check(run.error() == null && run.sessionId() != null, "Deployment failed to create a Session");
    cleanup.push(() -> closeSession(run.sessionId()));
    check(
        client.deploymentRuns().retrieve(run.id()).sessionId().equals(run.sessionId()),
        "Global run differs");
    check(
        client.deployments().runs().retrieve(d.id(), run.id()).id().equals(run.id()),
        "Scoped run differs");
    check(!client.deployments().runs().list(d.id()).data().isEmpty(), "Run list missing");
    SessionEventListResponse events =
        client
            .sessions()
            .events()
            .list(
                run.sessionId(),
                SessionEventListParams.builder()
                    .types(Collections.singletonList("user.message"))
                    .limit(1)
                    .build());
    check(!events.data().isEmpty(), "Deployment initial event missing");
    awaitReply(run.sessionId(), events.data().get(0).id());
  }

  private void work() throws Exception {
    Environment e = environment("self_hosted");
    Agent a = agent();
    Session s = createSession(a, e, "work-session");
    client
        .sessions()
        .events()
        .send(
            s.id(),
            SessionEventSendParams.builder().events(Collections.singletonList(message())).build(),
            options("work-message"));
    RequestOptions worker = options("work").toBuilder().header("Worker-ID", name).build();
    WorkItem item =
        client
            .environments()
            .work()
            .poll(e.id(), WorkPollParams.builder().blockMs(999).build(), worker);
    check(item != null, "No self-hosted work item");
    check(item.data().id().equals(s.id()), "Work references a different Session");
    client.environments().work().ack(e.id(), item.id(), worker);
    client
        .environments()
        .work()
        .heartbeat(
            e.id(),
            item.id(),
            WorkHeartbeatParams.builder()
                .expectedLastHeartbeat("NO_HEARTBEAT")
                .desiredTtlSeconds(30)
                .build(),
            worker);
    client
        .environments()
        .work()
        .update(
            e.id(),
            item.id(),
            WorkUpdateParams.builder()
                .metadata(Collections.singletonMap("sdk_contract", name))
                .build(),
            worker);
    check(
        client.environments().work().retrieve(e.id(), item.id()).id().equals(item.id()),
        "Work retrieval mismatch");
    client.environments().work().list(e.id());
    client.environments().work().stats(e.id());
    client
        .environments()
        .work()
        .stop(e.id(), item.id(), WorkStopParams.builder().force(true).build(), worker);
  }

  private void closeSession(String id) {
    Session s = client.sessions().retrieve(id);
    if (!Arrays.asList("idle", "terminated").contains(s.status()))
      client.sessions().cancel(id, options("cleanup-cancel-" + id));
    client.sessions().delete(id, options("cleanup-delete-" + id));
  }

  private static void check(boolean condition, String message) {
    if (!condition) throw new IllegalStateException(message);
  }
}
