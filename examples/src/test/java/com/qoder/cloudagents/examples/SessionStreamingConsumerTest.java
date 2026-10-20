package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.errors.StreamException;
import java.io.*;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Timeout(10)
class SessionStreamingConsumerTest {
  MockResponse stream(String text) {
    return new MockResponse()
        .setHeader("Content-Type", "text/event-stream")
        .setHeader("x-request-id", "req-stream")
        .setBody(text);
  }

  String event(String id) {
    return "id: " + id + "\ndata: {\"type\":\"agent.message\",\"content\":\"private text\"}\n\n";
  }

  ForwardClient client(MockWebServer server) {
    return ForwardClient.builder()
        .baseUrl(server.url("/api/v1/forward").toString())
        .pat("secret-pat")
        .maxRetries(0)
        .build();
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void packagedExampleResumesAndClosesWithoutChangingSession(boolean async) throws Exception {
    assertTrue(
        ForwardClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()
            .endsWith(".jar"));
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(stream(event("first")));
      server.enqueue(stream(event("second") + event("third")));
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try (ForwardClient c = client(server);
          PrintStream out = new PrintStream(output, true, "UTF-8")) {
        SessionStreamingExample.run(c, "sess_existing", null, "history-last", async, 2, out);
      }
      RecordedRequest first = server.takeRequest(1, TimeUnit.SECONDS),
          second = server.takeRequest(1, TimeUnit.SECONDS);
      assertNotNull(first);
      assertNotNull(second);
      assertEquals("history-last", first.getHeader("Last-Event-ID"));
      assertEquals("first", second.getHeader("Last-Event-ID"));
      assertEquals("GET", first.getMethod());
      assertEquals("GET", second.getMethod());
      assertEquals(2, server.getRequestCount());
      assertEquals("agent.message", first.getRequestUrl().queryParameter("event_deltas[]"));
      String text = output.toString("UTF-8");
      assertTrue(text.contains("last_event_id=second"));
      assertTrue(text.contains("request_id=req-stream"));
      assertFalse(text.contains("third"));
      assertFalse(text.contains("secret-pat"));
      assertFalse(text.contains("private text"));
    }
  }

  @Test
  void threadExampleUsesThreadCursorAndNoFilters() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(stream(event("thread-event")));
      try (ForwardClient c = client(server);
          PrintStream out = new PrintStream(new ByteArrayOutputStream())) {
        SessionStreamingExample.run(c, "s", "t", "thread-history-last", false, 1, out);
      }
      RecordedRequest r = server.takeRequest();
      assertEquals("/api/v1/forward/sessions/s/threads/t/stream", r.getPath());
      assertEquals("thread-history-last", r.getHeader("Last-Event-ID"));
    }
  }

  @Test
  void failureKeepsCheckpointAndRequestIdButNotPayload() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(
          stream(event("ok") + "event: error\ndata: {\"detail\":\"private-error\"}\n\n"));
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try (ForwardClient c = client(server);
          PrintStream out = new PrintStream(output, true, "UTF-8")) {
        assertThrows(
            StreamException.class,
            () -> SessionStreamingExample.run(c, "s", null, null, false, 5, out));
      }
      String text = output.toString("UTF-8");
      assertTrue(text.contains("last_event_id=ok"));
      assertTrue(text.contains("request_id=req-stream"));
      assertFalse(text.contains("private-error"));
      assertEquals(1, server.getRequestCount());
    }
  }
}
