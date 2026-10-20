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
class ForwardLiveCheckTest {
  @Test
  void requiresExplicitLiveWriteAndExecutionGates() {
    Map<String, String> env = new HashMap<>();
    assertThrows(IllegalStateException.class, () -> ForwardLiveCheck.validateGate("read", env));
    env.put("QODER_RUN_LIVE", "true");
    ForwardLiveCheck.validateGate("read", env);
    ForwardLiveCheck.validateGate("usage", env);
    assertThrows(
        IllegalStateException.class, () -> ForwardLiveCheck.validateGate("resources", env));
    env.put("QODER_ALLOW_WRITE", "true");
    ForwardLiveCheck.validateGate("resources", env);
    ForwardLiveCheck.validateGate("sa-tokens", env);
    for (String group :
        Arrays.asList(
            "session", "realtime-text", "realtime-pcm", "schedule", "batch", "realtime-work"))
      assertThrows(IllegalStateException.class, () -> ForwardLiveCheck.validateGate(group, env));
    env.put("QODER_ALLOW_EXECUTION", "true");
    ForwardLiveCheck.validateGate("session", env);
    assertThrows(IllegalStateException.class, () -> ForwardLiveCheck.validateGate("all", env));
  }

  @Test
  void sessionAndThreadStreamsUseOwnResourcesAndKeepPrimaryFailure() throws Exception {
    for (boolean fail : new boolean[] {false, true}) {
      List<String> calls = Collections.synchronizedList(new ArrayList<>());
      try (MockWebServer server = new MockWebServer()) {
        server.setDispatcher(
            new Dispatcher() {
              String marker;

              @Override
              public MockResponse dispatch(RecordedRequest request) {
                String path = request.getRequestUrl().encodedPath();
                calls.add(request.getMethod() + " " + path);
                if (path.endsWith("/identities") && request.getMethod().equals("POST"))
                  return ok("{\"id\":\"idn_owned\"}");
                if (path.endsWith("/templates/existing"))
                  return ok("{\"id\":\"existing\",\"environment_id\":\"env_existing\"}");
                if (path.endsWith("/templates") && request.getMethod().equals("POST"))
                  return ok("{\"id\":\"tmpl_owned\"}");
                if (path.endsWith("/sessions")) {
                  if (request.getMethod().equals("POST")) return ok("{\"id\":\"sess_owned\"}");
                  assertEquals("idn_owned", request.getRequestUrl().queryParameter("identity_ids"));
                  return ok("{\"data\":[{\"id\":\"sess_owned\"}],\"has_more\":false}");
                }
                if (path.endsWith("/sessions/sess_owned/events")
                    && request.getMethod().equals("POST")) {
                  JsonValue body = JsonValue.parse(request.getBody().readUtf8());
                  String text =
                      body.get("events")
                          .asArray()
                          .get(0)
                          .get("content")
                          .asArray()
                          .get(0)
                          .get("text")
                          .asString();
                  marker = text.substring("Reply with exactly ".length());
                  return ok("{\"data\":[{\"id\":\"user-event\",\"type\":\"user.message\"}]}");
                }
                if (path.endsWith("/stream")) {
                  assertEquals("user-event", request.getHeader("Last-Event-ID"));
                  if (fail)
                    return new MockResponse()
                        .setResponseCode(503)
                        .setHeader("x-request-id", "req-stream-failure")
                        .setBody(
                            "{\"error\":{\"code\":\"busy\",\"message\":\"do-not-log-this-token\"}}");
                  return ok("id: event-1\n"
                          + "data:"
                          + " {\"id\":\"message-1\",\"type\":\"agent.message\",\"text\":\""
                          + marker
                          + "\"}\n\n"
                          + "id: thread-event\n"
                          + "data:"
                          + " {\"id\":\"thread-idle\",\"type\":\"session.thread_status_idle\",\"session_thread_id\":\"thread-1\"}\n\n"
                          + "id: event-2\n"
                          + "data: {\"id\":\"idle-1\",\"type\":\"session.status_idle\"}\n\n")
                      .setHeader("Content-Type", "text/event-stream");
                }
                if (path.endsWith("/templates/tmpl_owned/archive") && fail)
                  return new MockResponse().setResponseCode(409).setBody("{}");
                if (path.endsWith("/clear")) return ok("{\"status\":\"completed\"}");
                if (path.endsWith("/archive") || request.getMethod().equals("DELETE"))
                  return ok("{}");
                return new MockResponse().setResponseCode(404);
              }
            });
        server.start();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ForwardClient client =
                ForwardClient.builder()
                    .baseUrl(server.url("/api/v1/forward").toString())
                    .pat("synthetic-token")
                    .maxRetries(0)
                    .build();
            PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
          ForwardLiveCheck check = new ForwardLiveCheck(client, "existing", out);
          if (fail) {
            ApiStatusException error = assertThrows(ApiStatusException.class, check::session);
            assertEquals(503, error.statusCode());
            assertEquals(1, error.getSuppressed().length);
            assertEquals(409, ((ApiStatusException) error.getSuppressed()[0]).statusCode());
            ForwardLiveCheck.reportFailure(out, "session_sse", error);
            assertTrue(bytes.toString("UTF-8").contains("req-stream-failure"));
            assertFalse(bytes.toString("UTF-8").contains("do-not-log-this-token"));
          } else {
            check.session();
            assertTrue(bytes.toString("UTF-8").contains("thread_verified"));
          }
        }
        assertTrue(calls.contains("DELETE /api/v1/forward/identities/idn_owned"));
        assertTrue(calls.contains("POST /api/v1/forward/sessions/sess_owned/archive"));
        assertFalse(
            calls.stream()
                .anyMatch(call -> call.startsWith("POST") && call.endsWith("/templates/existing")));
        assertFalse(bytes.toString("UTF-8").contains("synthetic-token"));
      }
    }
  }

  @Test
  void incompleteClearKeepsIdentityForRecovery() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.enqueue(ok("{\"id\":\"idn_owned\"}"));
      server.enqueue(ok("{\"data\":[],\"has_more\":false}"));
      server.enqueue(ok("{\"status\":\"pending\"}"));
      server.start();
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient client =
              ForwardClient.builder()
                  .baseUrl(server.url("/api/v1/forward").toString())
                  .pat("synthetic-token")
                  .build();
          PrintStream out = new PrintStream(bytes)) {
        ForwardLiveCheck.IdentityFixture fixture =
            new ForwardLiveCheck.IdentityFixture(client, out);
        assertThrows(IllegalStateException.class, fixture::close);
      }
      assertEquals(3, server.getRequestCount());
      assertTrue(bytes.toString("UTF-8").contains("cleanup_failed_identity"));
    }
  }

  private static MockResponse ok(String body) {
    return new MockResponse().setBody(body);
  }
}
