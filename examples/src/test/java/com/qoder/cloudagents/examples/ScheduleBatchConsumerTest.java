package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.errors.ApiStatusException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Timeout(15)
class ScheduleBatchConsumerTest {
  MockWebServer server;
  ForwardClient client;
  ByteArrayOutputStream bytes;
  PrintStream out;
  @TempDir Path temp;

  @BeforeEach
  void setup() throws Exception {
    assertTrue(
        ForwardClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()
            .endsWith(".jar"));
    server = new MockWebServer();
    server.start();
    client =
        ForwardClient.builder()
            .baseUrl(server.url("/api/v1/forward").toString())
            .pat("test-secret")
            .maxRetries(0)
            .build();
    bytes = new ByteArrayOutputStream();
    out = new PrintStream(bytes, true, "UTF-8");
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    server.close();
    out.close();
  }

  MockResponse json(String s) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(s);
  }

  RecordedRequest take() throws Exception {
    RecordedRequest r = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void scheduleOnlyRunsWhenOptedInAndAlwaysArchives(boolean execute) throws Exception {
    server.setDispatcher(
        new Dispatcher() {
          @Override
          public MockResponse dispatch(RecordedRequest r) {
            if (r.getPath().equals("/api/v1/forward/schedules/sched_owned/run"))
              return json("{\"id\":\"srun_owned\",\"status\":\"pending\"}");
            if (r.getPath().startsWith("/api/v1/forward/schedule_runs/srun_owned"))
              return json(
                  "{\"id\":\"srun_owned\",\"status\":\"running\",\"session_id\":\"sess_owned\"}");
            if (r.getPath().startsWith("/api/v1/forward/schedule_runs?"))
              return json("{\"data\":[],\"has_more\":false}");
            return json("{\"id\":\"sched_owned\",\"status\":\"active\"}");
          }
        });
    ScheduleLifecycleExample.run(
        client, "idn_existing", "tmpl_existing", "env_existing", execute, out);
    assertEquals(execute ? 9 : 7, server.getRequestCount());
    RecordedRequest create = take();
    JsonValue body = JsonValue.parse(create.getBody().readUtf8());
    assertEquals("manual", body.get("trigger_policy").get("type").asString());
    assertTrue(body.get("sinks").isMissing());
    assertEquals(
        "user.message", body.get("initial_events").asArray().get(0).get("type").asString());
    assertEquals(
        "Reply with exactly SDK_JAVA_SCHEDULE_OK",
        body.get("initial_events").asArray().get(0).get("content").asString());
    Set<String> keys = new HashSet<>();
    keys.add(create.getHeader("Idempotency-Key"));
    int runs = 0;
    RecordedRequest last = create;
    for (int i = 1; i < server.getRequestCount(); i++) {
      RecordedRequest r = take();
      last = r;
      assertEquals("Bearer test-secret", r.getHeader("Authorization"));
      if (r.getPath().endsWith("/run")) runs++;
      if ("POST".equals(r.getMethod()))
        assertTrue(
            keys.add(r.getHeader("Idempotency-Key")),
            "Separate actions need separate idempotency keys");
      assertNotEquals("DELETE", r.getMethod());
    }
    assertEquals(execute ? 1 : 0, runs);
    assertEquals("/api/v1/forward/schedules/sched_owned/archive", last.getPath());
    String text = bytes.toString("UTF-8");
    assertTrue(text.contains("archived_schedule=sched_owned"));
    assertFalse(text.contains("test-secret"));
  }

  @Test
  void scheduleRetainsOriginalFailureWhenCleanupFails() {
    server.enqueue(json("{\"id\":\"sched_owned\"}"));
    server.enqueue(json("{\"id\":\"sched_owned\"}"));
    server.enqueue(json("{\"error\":{\"message\":\"invalid update\"}}").setResponseCode(400));
    server.enqueue(json("{\"error\":{\"message\":\"archive failed\"}}").setResponseCode(503));
    ApiStatusException e =
        assertThrows(
            ApiStatusException.class,
            () -> ScheduleLifecycleExample.run(client, "i", "t", "e", false, out));
    assertEquals(400, e.statusCode());
    assertEquals(1, e.getSuppressed().length);
    assertEquals(503, ((ApiStatusException) e.getSuppressed()[0]).statusCode());
    assertEquals(4, server.getRequestCount());
  }

  Path input() throws Exception {
    Path p = temp.resolve("batch.jsonl");
    Files.write(
        p,
        "{\"custom_id\":\"task-1\",\"identity_id\":\"idn_existing\",\"template_id\":\"tmpl_existing\",\"body\":{\"input\":\"Reply hello\"}}\n"
            .getBytes(StandardCharsets.UTF_8));
    return p;
  }

  @Test
  void batchSubmissionUploadsCorrectPurposeAndRetainsPendingInput() throws Exception {
    server.enqueue(json("{\"id\":\"file_input\"}"));
    server.enqueue(
        json(
            "{\"id\":\"batch_owned\",\"status\":\"validating\",\"queue_reason\":\"global_capacity\"}"));
    server.enqueue(
        json(
            "{\"id\":\"batch_owned\",\"status\":\"queued\",\"queue_reason\":\"global_capacity\"}"));
    server.enqueue(json("{\"data\":[],\"has_more\":false}"));
    BatchExample.submit(client, input(), true, out);
    BatchExample.inspect(client, "batch_owned", out);
    RecordedRequest upload = take();
    assertEquals("/api/v1/forward/files", upload.getRequestUrl().encodedPath());
    String multipart = upload.getBody().readUtf8();
    assertTrue(multipart.contains("name=\"purpose\""));
    assertTrue(multipart.contains("session_resource"));
    assertTrue(multipart.contains("task-1"));
    RecordedRequest create = take();
    JsonValue body = JsonValue.parse(create.getBody().readUtf8());
    assertTrue(body.get("ignore_idle_window").asBoolean());
    assertEquals("file_input", body.get("input_file_id").asString());
    assertNotNull(create.getHeader("Idempotency-Key"));
    assertEquals("GET", take().getMethod());
    assertEquals("/api/v1/forward/batches/batch_owned/tasks?limit=20", take().getPath());
    assertEquals(4, server.getRequestCount());
    String text = bytes.toString("UTF-8");
    assertTrue(text.contains("Input file retained"));
    assertTrue(text.contains("queue_reason=global_capacity"));
    assertFalse(text.contains("test-secret"));
  }

  @Test
  void terminalResultGrantsAreSeparateFromFileApiAndNeverPrinted() throws Exception {
    server.enqueue(
        json(
            "{\"id\":\"b\",\"status\":\"completed\",\"output_file_id\":\"result\",\"error_file_id\":\"error\"}"));
    server.enqueue(
        json("{\"data\":[{\"custom_id\":\"t\",\"status\":\"failed\"}],\"has_more\":false}"));
    server.enqueue(
        json(
            "{\"url\":\"https://storage.example/result?token=private-signature\",\"expires_at\":\"soon\"}"));
    server.enqueue(
        json(
            "{\"url\":\"https://storage.example/error?token=private-signature\",\"expires_at\":\"soon\"}"));
    BatchExample.inspect(client, "b", out);
    take();
    take();
    assertEquals("/api/v1/forward/batches/b/output", take().getPath());
    assertEquals("/api/v1/forward/batches/b/error", take().getPath());
    assertEquals(4, server.getRequestCount());
    String text = bytes.toString("UTF-8");
    assertTrue(text.contains("output_url_expires_at=soon"));
    assertFalse(text.contains("private-signature"));
    assertFalse(text.contains("storage.example"));
  }

  @Test
  void ambiguousBatchCreationLeavesInputForReconciliation() throws Exception {
    server.enqueue(json("{\"id\":\"file_retained\"}"));
    server.enqueue(json("{\"error\":{\"message\":\"temporary\"}}").setResponseCode(503));
    assertThrows(ApiStatusException.class, () -> BatchExample.submit(client, input(), false, out));
    assertEquals(2, server.getRequestCount());
    String text = bytes.toString("UTF-8");
    assertTrue(text.contains("batch_input_file=file_retained"));
    assertTrue(text.contains("batch_create_key="));
  }
}
