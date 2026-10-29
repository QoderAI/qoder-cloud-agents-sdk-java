package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.errors.ApiStatusException;
import java.io.*;
import java.util.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class ForwardExecutionChecksTest {
  @Test
  void scheduleChecksActualReplyAndCleansOnlyOwnResources() throws Exception {
    exercise("schedule", "success");
  }

  @Test
  void missingScheduleReplyFailsAndStillCleansResources() throws Exception {
    exercise("schedule", "wrong-reply");
  }

  @Test
  void ambiguousRunRetainsIdentityAndTemplate() throws Exception {
    exercise("schedule", "lost-run");
  }

  @Test
  void batchRequiresOutputAndHistoryAndDeletesInput() throws Exception {
    exercise("batch", "success");
  }

  @Test
  void batchPollingFailureCancelsBeforeDeletingInputAndKeepsPrimaryError() throws Exception {
    exercise("batch", "poll-failure");
  }

  private void exercise(String group, String scenario) throws Exception {
    List<String> calls = Collections.synchronizedList(new ArrayList<>());
    try (MockWebServer server = new MockWebServer()) {
      server.setDispatcher(
          new Dispatcher() {
            String marker, custom;
            int batchReads;

            @Override
            public MockResponse dispatch(RecordedRequest request) {
              String path = request.getRequestUrl().encodedPath(), method = request.getMethod();
              calls.add(method + " " + path);
              if (path.endsWith("/identities") && "POST".equals(method))
                return ok("{\"id\":\"owned\"}");
              if (path.endsWith("/templates/existing")) return ok("{\"environment_id\":\"env\"}");
              if (path.endsWith("/templates") && "POST".equals(method))
                return ok("{\"id\":\"template-owned\"}");
              if (path.endsWith("/schedules") && "POST".equals(method)) {
                JsonValue body = JsonValue.parse(request.getBody().readUtf8());
                assertEquals("manual", body.get("trigger_policy").get("type").asString());
                String prompt =
                    body.get("initial_events").asArray().get(0).get("content").asString();
                assertEquals("Reply with exactly Hello from the Java SDK.", prompt);
                marker = prompt.substring("Reply with exactly ".length());
                return ok("{\"id\":\"schedule-owned\"}");
              }
              if (path.endsWith("/schedules/schedule-owned/run")) {
                if ("lost-run".equals(scenario))
                  return new MockResponse().setResponseCode(503).setBody("{}");
                return ok("{\"id\":\"run-owned\",\"status\":\"pending\"}");
              }
              if (path.endsWith("/schedule_runs/run-owned")) {
                assertEquals("owned", request.getRequestUrl().queryParameter("identity_id"));
                return ok(
                    "{\"id\":\"run-owned\",\"status\":\"completed\",\"session_id\":\"session-owned\"}");
              }
              if (path.endsWith("/schedule_runs"))
                return ok("{\"data\":[{\"id\":\"run-owned\"}],\"has_more\":false}");
              if (path.endsWith("/files") && "POST".equals(method)) {
                String body = request.getBody().readUtf8();
                int start = body.indexOf("{\"custom_id\"");
                JsonValue row =
                    JsonValue.parse(body.substring(start, body.indexOf('\n', start)).trim());
                custom = row.get("custom_id").asString();
                marker =
                    row.get("body")
                        .get("input")
                        .asString()
                        .substring("Reply with exactly ".length());
                assertEquals("Hello from the Java SDK.", marker);
                return ok("{\"id\":\"input-owned\"}");
              }
              if (path.endsWith("/batches") && "POST".equals(method)) {
                assertTrue(
                    JsonValue.parse(request.getBody().readUtf8())
                        .get("ignore_idle_window")
                        .asBoolean());
                return ok("{\"id\":\"batch-owned\",\"status\":\"pending\"}");
              }
              if (path.endsWith("/batches/batch-owned") && "GET".equals(method)) {
                batchReads++;
                if ("poll-failure".equals(scenario)) {
                  if (batchReads == 1) return new MockResponse().setResponseCode(503).setBody("{}");
                  return ok("{\"status\":\"" + (batchReads == 2 ? "running" : "cancelled") + "\"}");
                }
                return ok(
                    "{\"status\":\"completed\",\"request_counts\":{\"completed\":1,\"failed\":0}}");
              }
              if (path.endsWith("/batches/batch-owned/tasks"))
                return ok(
                    "{\"data\":[{\"custom_id\":\""
                        + custom
                        + "\",\"status\":\"completed\"}],\"has_more\":false}");
              if (path.endsWith("/batches/batch-owned/output"))
                return ok("{\"url\":\"" + server.url("/output") + "\"}");
              if (path.equals("/output")) {
                assertNull(request.getHeader("Authorization"));
                return ok(
                    "{\"custom_id\":\""
                        + custom
                        + "\",\"identity_id\":\"owned\",\"template_id\":\"template-owned\",\"session_id\":\"session-owned\",\"status\":\"completed\",\"error\":null,\"response\":\""
                        + marker
                        + "\"}\n");
              }
              if (path.endsWith("/sessions/session-owned"))
                return ok(
                    "{\"id\":\"session-owned\",\"identity_id\":\"owned\",\"status\":\"idle\"}");
              if (path.endsWith("/sessions/session-owned/events"))
                return ok(
                    "{\"data\":[{\"id\":\"reply\",\"type\":\"agent.message\",\"text\":\""
                        + ("wrong-reply".equals(scenario) ? "wrong" : marker)
                        + "\"},{\"id\":\"idle\",\"type\":\"session.status_idle\"}],\"has_more\":false}");
              if (path.endsWith("/sessions")) {
                assertEquals("owned", request.getRequestUrl().queryParameter("identity_ids"));
                return ok(
                    "{\"data\":[{\"id\":\"session-owned\",\"identity_id\":\"owned\",\"status\":\"idle\"}],\"has_more\":false}");
              }
              if (path.endsWith("/clear")) return ok("{\"status\":\"completed\"}");
              if (path.endsWith("/archive") || path.endsWith("/cancel") || "DELETE".equals(method))
                return ok("{}");
              return new MockResponse().setResponseCode(404);
            }
          });
      server.start();
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient client =
              ForwardClient.builder()
                  .baseUrl(server.url("/api/v1/forward").toString())
                  .pat("synthetic")
                  .maxRetries(0)
                  .build();
          PrintStream out = new PrintStream(bytes)) {
        ForwardExecutionChecks check = new ForwardExecutionChecks(client, "existing", out);
        if ("success".equals(scenario)) check.run(group);
        else if ("wrong-reply".equals(scenario))
          assertThrows(IllegalStateException.class, () -> check.run(group));
        else
          assertEquals(
              503, assertThrows(ApiStatusException.class, () -> check.run(group)).statusCode());
      }
      if ("lost-run".equals(scenario)) {
        assertFalse(calls.contains("DELETE /api/v1/forward/identities/owned"));
        assertFalse(calls.contains("POST /api/v1/forward/templates/template-owned/archive"));
        assertTrue(bytes.toString("UTF-8").contains("retained_execution_resources"));
      } else {
        assertTrue(calls.contains("DELETE /api/v1/forward/identities/owned"));
        assertTrue(calls.contains("POST /api/v1/forward/templates/template-owned/archive"));
      }
      if ("batch".equals(group))
        assertTrue(calls.contains("DELETE /api/v1/forward/files/input-owned"));
      if ("poll-failure".equals(scenario))
        assertTrue(
            calls.indexOf("POST /api/v1/forward/batches/batch-owned/cancel")
                < calls.indexOf("DELETE /api/v1/forward/files/input-owned"));
    }
  }

  @Test
  void outputOwnershipAndRedirectsCannotBeSilentlyAccepted() throws Exception {
    assertThrows(
        IllegalStateException.class,
        () ->
            ForwardExecutionChecks.validateBatchOutput(
                "{\"custom_id\":\"mine\",\"identity_id\":\"someone-else\",\"template_id\":\"template\"}",
                "mine",
                "owned",
                "template",
                "marker"));
    try (MockWebServer server = new MockWebServer()) {
      server.enqueue(new MockResponse().setResponseCode(302).setHeader("Location", "/redirected"));
      server.start();
      assertThrows(
          IllegalStateException.class,
          () -> ForwardExecutionChecks.download(server.url("/output").toString()));
      assertEquals(1, server.getRequestCount());
    }
  }

  private static MockResponse ok(String body) {
    return new MockResponse().setBody(body);
  }
}
