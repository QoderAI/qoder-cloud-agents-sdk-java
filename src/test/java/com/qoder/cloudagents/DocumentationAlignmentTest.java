package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.forward.*;
import com.qoder.cloudagents.streaming.EventStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

/** Official Session/Environment schemas retrieved 2026-11-03; loopback only. */
@Timeout(15)
class DocumentationAlignmentTest {
  @ParameterizedTest
  @CsvSource({
    "allow,always_allow,",
    "ask,always_ask,",
    "allow,auto,",
    "ask,auto,indeterminate",
    "deny,auto,high_risk"
  })
  void approvalsKeepRootStringAndNestedDecision(String result, String policy, String reason) {
    String detail =
        "auto".equals(policy)
            ? ",\"evaluated_permission\":{\"type\":\""
                + result
                + "\""
                + (reason == null ? "" : ",\"reason_code\":\"" + reason + "\"")
                + "}"
            : "";
    JsonValue json =
        JsonValue.parse(
            "{\"evaluated_permission\":\""
                + result
                + "\",\"evaluation\":{\"type\":\""
                + policy
                + "\""
                + detail
                + "},\"future\":17}");
    SessionEvent event = new SessionEvent(json, "request");
    assertEquals(result, event.evaluatedPermission());
    assertEquals(policy, event.evaluation().type());
    if ("auto".equals(policy)) {
      assertEquals(result, event.evaluation().evaluatedPermission().type());
      assertEquals(reason, event.evaluation().evaluatedPermission().reasonCode());
    } else assertNull(event.evaluation().evaluatedPermission());
    assertEquals(17L, event.field("future").asLong());
    assertEquals(
        result,
        new com.qoder.cloudagents.managed.SessionEvent(json, "request").evaluatedPermission());
    assertNull(new SessionEvent(JsonValue.parse("{}"), "request").evaluation());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void forwardBudgetAndModelPreserveWireStates(boolean async) throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      try (ForwardClient client =
          ForwardClient.builder()
              .baseUrl(server.url("/forward").toString())
              .pat("test")
              .maxRetries(0)
              .build()) {
        SessionBudget budget =
            SessionBudget.builder().type("limit").maxCreditCost("100.00").build();
        SessionConfig config =
            SessionConfig.builder()
                .model(
                    ModelConfig.builder()
                        .id("ultimate")
                        .speed("high")
                        .contextWindow(200000L)
                        .build())
                .build();
        String payload =
            "{\"id\":\"sess_test\",\"budget\":"
                + budget.rawJson()
                + ",\"config\":"
                + config.rawJson()
                + "}";
        server.enqueue(new MockResponse().setBody(payload));
        SessionCreateParams create =
            SessionCreateParams.builder()
                .identityId("idn_test")
                .templateId("tmpl_test")
                .budget(budget)
                .config(config)
                .build();
        Session response =
            async
                ? client.async().sessions().create(create).get()
                : client.sessions().create(create);
        JsonValue wire = JsonValue.parse(server.takeRequest().getBody().readUtf8());
        assertEquals("100.00", wire.get("budget").get("max_credit_cost").asString());
        assertEquals("true", wire.get("metadata").get("__qca_sdk_created").asString());
        assertEquals("high", response.config().model().speed());
        assertEquals("100.00", response.budget().maxCreditCost());
        for (int state = 0; state < 3; state++) {
          SessionUpdateParams.Builder b = SessionUpdateParams.builder();
          if (state == 1) b.budget(null).config(SessionConfig.builder().model(null).build());
          if (state == 2)
            b.budget(budget)
                .config(SessionConfig.builder().model(ModelConfig.of("ultimate")).build());
          SessionUpdateParams params = b.build();
          server.enqueue(new MockResponse().setBody(params.rawJson()));
          if (async) client.async().sessions().update("sess_test", params).get();
          else client.sessions().update("sess_test", params);
          wire = JsonValue.parse(server.takeRequest().getBody().readUtf8());
          assertEquals(state == 0, wire.get("budget").isMissing());
          assertEquals(state == 1, wire.get("budget").isNull());
          if (state == 1) assertTrue(wire.get("config").get("model").isNull());
          if (state == 2) assertEquals("ultimate", wire.get("config").get("model").asString());
        }
        assertTrue(create.toJsonValue().get("metadata").isMissing());
      }
    }
  }

  @Test
  void startupScriptsAndBudgetEventsRetainPresence() {
    String script = "#!/bin/sh\necho ready\n";
    EnvironmentConfig f = EnvironmentConfig.builder().type("cloud").startupScript(script).build();
    assertEquals(script, EnvironmentConfig.fromJson(f.toJsonValue()).startupScript());
    for (String type : Arrays.asList("cloud", "self_hosted")) {
      com.qoder.cloudagents.managed.EnvironmentConfig m =
          com.qoder.cloudagents.managed.EnvironmentConfig.builder()
              .type(type)
              .startupScript(script)
              .build();
      assertEquals(
          script,
          com.qoder.cloudagents.managed.EnvironmentConfig.fromJson(m.toJsonValue())
              .startupScript());
    }
    for (String payload :
        Arrays.asList(
            "{}",
            "{\"budget\":null}",
            "{\"budget\":{\"type\":\"limit\",\"max_credit_cost\":\"0.01\"}}")) {
      SessionEvent event = new SessionEvent(JsonValue.parse(payload), null);
      assertEquals(payload.contains("budget"), !event.field("budget").isMissing());
      if (payload.contains("0.01")) assertEquals("0.01", event.budget().maxCreditCost());
      else assertNull(event.budget());
    }
    SessionEvent usage =
        new SessionEvent(
            JsonValue.parse(
                "{\"model_credits\":0.68,\"sandbox_runtime_credits\":0.04,\"total_credits\":0.72}"),
            null);
    assertEquals(new BigDecimal("0.72"), usage.totalCredits());
  }

  @ParameterizedTest
  @CsvSource({
    "false,false,false",
    "false,false,true",
    "false,true,false",
    "false,true,true",
    "true,false,false",
    "true,false,true",
    "true,true,false",
    "true,true,true"
  })
  void threadTerminationStopsOnlyThreadSubscriptions(boolean managed, boolean async, boolean thread)
      throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(
          sse("first", "session.thread_status_idle") // Idle is not terminal.
              .setBody(
                  frame("idle", "session.thread_status_idle")
                      + frame("end", "session.thread_status_terminated")));
      server.enqueue(sse("session-end", "session.status_terminated"));
      List<String> ids = new ArrayList<>();
      if (managed) {
        try (ManagedClient c =
                ManagedClient.builder()
                    .baseUrl(server.url("/cloud").toString())
                    .pat("test")
                    .maxRetries(0)
                    .build();
            EventStream<com.qoder.cloudagents.managed.SessionEvent> stream =
                thread
                    ? (async
                        ? c.async().sessions().threads().events().resumableStream("s", "t")
                        : c.sessions().threads().events().resumableStream("s", "t"))
                    : (async
                        ? c.async().sessions().events().resumableStream("s")
                        : c.sessions().events().resumableStream("s"))) {
          for (com.qoder.cloudagents.managed.SessionEvent event : stream) ids.add(event.id());
        }
      } else {
        try (ForwardClient c =
                ForwardClient.builder()
                    .baseUrl(server.url("/forward").toString())
                    .pat("test")
                    .maxRetries(0)
                    .build();
            EventStream<SessionEvent> stream =
                thread
                    ? (async
                        ? c.async().sessions().threads().events().resumableStream("s", "t")
                        : c.sessions().threads().events().resumableStream("s", "t"))
                    : (async
                        ? c.async().sessions().events().resumableStream("s")
                        : c.sessions().events().resumableStream("s"))) {
          for (SessionEvent event : stream) ids.add(event.id());
        }
      }
      assertEquals(
          thread ? Arrays.asList("idle", "end") : Arrays.asList("idle", "end", "session-end"), ids);
      assertEquals(thread ? 1 : 2, server.getRequestCount());
      assertNotNull(server.takeRequest(1, TimeUnit.SECONDS));
      if (!thread) assertEquals("end", server.takeRequest().getHeader("Last-Event-ID"));
    }
  }

  private static String frame(String id, String type) {
    return "id: "
        + id
        + "\nevent: "
        + type
        + "\ndata: {\"id\":\""
        + id
        + "\",\"type\":\""
        + type
        + "\"}\n\n";
  }

  private static MockResponse sse(String id, String type) {
    return new MockResponse()
        .setHeader("Content-Type", "text/event-stream")
        .setBody(frame(id, type));
  }
}
