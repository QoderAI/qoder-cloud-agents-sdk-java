package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.forward.realtime.RealtimeEvent;
import java.io.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(10)
class RealtimeWorkLiveCheckTest {
  @Test
  void completionMustFollowMatchingAcceptanceAndFailuresAreNotSuccess() {
    RealtimeWorkLiveCheck.WorkTracker tracker = new RealtimeWorkLiveCheck.WorkTracker();
    assertThrows(IllegalStateException.class, () -> tracker.accept(event("completed", "work-1")));
    tracker.accept(event("accepted", "work-1"));
    assertThrows(IllegalStateException.class, () -> tracker.accept(event("completed", "work-2")));
    assertThrows(IllegalStateException.class, () -> tracker.accept(event("failed", "work-1")));
    assertFalse(tracker.completed);
    tracker.accept(event("completed", "work-1"));
    assertTrue(tracker.completed);
  }

  @Test
  void workHistoryRequiresSameWorkAndPreservesFilterAcrossPages() throws Exception {
    for (boolean wrong : new boolean[] {false, true}) {
      try (MockWebServer server = new MockWebServer()) {
        server.enqueue(
            new MockResponse()
                .setBody(
                    "{\"events\":[{\"id\":\"done\",\"type\":\"voice.work.completed\",\"work_id\":\""
                        + (wrong ? "other" : "work-1")
                        + "\"}],\"page\":{\"has_more\":true,\"next_before\":\"cursor\"}}"));
        server.enqueue(
            new MockResponse()
                .setBody(
                    "{\"events\":[{\"id\":\"accepted\",\"type\":\"voice.work.accepted\",\"work_id\":\"work-1\"}],\"page\":{\"has_more\":false}}"));
        server.start();
        try (ForwardClient client =
                ForwardClient.builder()
                    .baseUrl(server.url("/api/v1/forward").toString())
                    .pat("synthetic")
                    .build();
            PrintStream out = new PrintStream(new ByteArrayOutputStream())) {
          if (wrong)
            assertThrows(
                IllegalStateException.class,
                () -> RealtimeWorkLiveCheck.verifyHistory(client, "conversation", "work-1", out));
          else RealtimeWorkLiveCheck.verifyHistory(client, "conversation", "work-1", out);
        }
        assertEquals("work", server.takeRequest().getRequestUrl().queryParameter("types"));
        if (wrong) {
          assertEquals(1, server.getRequestCount());
        } else {
          RecordedRequest second = server.takeRequest(1, java.util.concurrent.TimeUnit.SECONDS);
          assertNotNull(second);
          assertEquals("work", second.getRequestUrl().queryParameter("types"));
          assertEquals("cursor", second.getRequestUrl().queryParameter("before"));
        }
      }
    }
  }

  @Test
  void workHistoryAcceptsAssistantProjectionOfTheSameWork() throws Exception {
    verifyProjectedHistory(
        "{\"id\":\"milestone\",\"type\":\"voice.assistant_message.completed\","
            + "\"work_id\":\"work-1\",\"role\":\"assistant\",\"status\":\"completed\","
            + "\"text\":\"Task progress\"}");
  }

  @Test
  void workHistoryRejectsUnrelatedOrMalformedAssistantProjections() throws Exception {
    String valid =
        "{\"id\":\"milestone\",\"type\":\"voice.assistant_message.completed\","
            + "\"work_id\":\"work-1\",\"role\":\"assistant\",\"status\":\"completed\","
            + "\"text\":\"Task progress\"}";
    for (String invalid :
        new String[] {
          valid.replace("work-1", "another-work"),
          valid.replace("\"work_id\":\"work-1\",", ""),
          valid.replace("\"role\":\"assistant\"", "\"role\":\"user\""),
          valid.replace("\"status\":\"completed\"", "\"status\":\"interrupted\""),
          valid.replace("Task progress", " "),
          valid.replace("voice.assistant_message.completed", "voice.user_message.completed")
        }) {
      assertThrows(IllegalStateException.class, () -> verifyProjectedHistory(invalid));
    }
  }

  private static void verifyProjectedHistory(String projected) throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.enqueue(
          new MockResponse()
              .setBody(
                  "{\"events\":[{\"id\":\"done\",\"type\":\"voice.work.completed\",\"work_id\":\"work-1\"},"
                      + projected
                      + "],\"page\":{\"has_more\":false}}"));
      server.start();
      try (ForwardClient client =
              ForwardClient.builder()
                  .baseUrl(server.url("/api/v1/forward").toString())
                  .pat("synthetic")
                  .build();
          PrintStream out = new PrintStream(new ByteArrayOutputStream())) {
        RealtimeWorkLiveCheck.verifyHistory(client, "conversation", "work-1", out);
      }
      assertEquals("work", server.takeRequest().getRequestUrl().queryParameter("types"));
    }
  }

  private static RealtimeEvent event(String status, String id) {
    String extra =
        status.equals("accepted")
            ? ",\"objective\":\"pwd\""
            : status.equals("completed")
                ? ",\"result\":\"/data/workspace\""
                : ",\"error\":{\"code\":\"failed\"}";
    return RealtimeEvent.fromJson(
        JsonValue.parse(
            "{\"version\":\"voice.realtime.v1\",\"type\":\"work."
                + status
                + "\",\"event_id\":\"event\",\"sequence\":1,\"conversation_id\":\"conversation\",\"timestamp\":\"2026-10-18T00:00:00Z\",\"work_id\":\""
                + id
                + "\",\"payload\":{\"status\":\""
                + status
                + "\""
                + extra
                + "}}"));
  }
}
