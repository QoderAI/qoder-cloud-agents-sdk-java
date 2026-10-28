package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ManagedClient;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.managed.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Timeout(15)
class ManagedConsumerTest {
  @Test
  void compiledConsumerUsesJarAndCreatesBudgetedSession() throws Exception {
    assertTrue(
        ManagedClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .toString()
            .endsWith(".jar"));
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(
          new MockResponse().setBody("{\"id\":\"sess_test\",\"usage\":{\"total_credits\":0.25}}"));
      try (ManagedClient c =
          ManagedClient.builder()
              .pat("test-pat")
              .baseUrl(server.url("/api/v1/cloud").toString())
              .build()) {
        Session s =
            c.sessions()
                .create(
                    SessionCreateParams.builder()
                        .agent("agent_test")
                        .environmentId("env_test")
                        .budget(
                            SessionBudget.builder().type("limit").maxCreditCost("10.00").build())
                        .build());
        assertEquals("sess_test", s.id());
        assertEquals("0.25", s.usage().totalCredits().toPlainString());
        RecordedRequest r = server.takeRequest(2, TimeUnit.SECONDS);
        JsonValue body = JsonValue.parse(r.getBody().readUtf8());
        assertEquals("10.00", body.get("budget").get("max_credit_cost").asString());
        assertEquals("java", body.get("metadata").get("__qca_sdk_language").asString());
      }
    }
  }

  @Test
  void readOnlyLiveEntryExercisesManagedResourcesThroughTheJar() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      for (int i = 0; i < 11; i++)
        server.enqueue(
            new MockResponse()
                .setHeader("X-Request-ID", "req_live")
                .setBody(
                    i == 0
                        ? "{\"data\":[{\"id\":\"auto\"}]}"
                        : "{\"data\":[],\"has_more\":false}"));
      try (ManagedClient c =
          ManagedClient.builder()
              .pat("test-pat")
              .baseUrl(server.url("/api/v1/cloud").toString())
              .maxRetries(0)
              .build()) {
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        new ManagedLiveCheck(c, "auto", new PrintStream(log)).run("read");
        assertTrue(log.toString("UTF-8").contains("passed=read"));
        assertEquals(11, server.getRequestCount());
        for (int i = 0; i < 11; i++) {
          RecordedRequest r = server.takeRequest();
          assertTrue(r.getPath().startsWith("/api/v1/cloud/"));
          if (r.getPath().endsWith("/search"))
            assertEquals("search-2026-08-31", r.getHeader("x-qoder-beta"));
        }
      }
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"resources", "session", "deployment", "work"})
  void liveWritesRequireExplicitOptIn(String group) {
    Map<String, String> env = new HashMap<>();
    env.put("QODER_RUN_LIVE", "true");
    assertThrows(IllegalStateException.class, () -> ManagedLiveCheck.validate(group, env));
    env.put("QODER_ALLOW_WRITE", "true");
    if (!group.equals("resources"))
      assertThrows(IllegalStateException.class, () -> ManagedLiveCheck.validate(group, env));
    env.put("QODER_ALLOW_EXECUTION", "true");
    assertDoesNotThrow(() -> ManagedLiveCheck.validate(group, env));
  }

  @Test
  void liveReadRequiresOptInAndRejectsUnknownGroups() {
    assertThrows(
        IllegalStateException.class,
        () -> ManagedLiveCheck.validate("read", Collections.emptyMap()));
    assertThrows(
        IllegalStateException.class,
        () ->
            ManagedLiveCheck.validate(
                "delete-all", Collections.singletonMap("QODER_RUN_LIVE", "true")));
    assertDoesNotThrow(
        () ->
            ManagedLiveCheck.validate("read", Collections.singletonMap("QODER_RUN_LIVE", "true")));
  }
}
