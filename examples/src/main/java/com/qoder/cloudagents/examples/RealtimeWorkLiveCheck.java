package com.qoder.cloudagents.examples;

import static com.qoder.cloudagents.examples.ForwardLiveCheck.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.realtime.*;
import java.io.PrintStream;
import java.time.Duration;
import java.util.*;

final class RealtimeWorkLiveCheck {
  static void run(ForwardClient client, String conversation, PrintStream out) throws Exception {
    WorkTracker tracker = new WorkTracker();
    Set<String> cancelled = new HashSet<>();
    try (RealtimeConnection connection =
        client
            .realtime()
            .connect(
                RealtimeConnectParams.builder().conversationId(conversation).build(),
                RequestOptions.builder().totalTimeout(Duration.ofMinutes(4)).build())) {
      log(
          out,
          "work_connected",
          "conversation_id",
          conversation,
          "request_id",
          connection.requestId());
      connection.sendText("请调用模板能力执行一个后台任务：只运行 pwd 并告诉我当前工作目录，不读取其他文件，不修改任何文件。");
      connection.ping();
      boolean pong = false;
      while (!tracker.completed || !pong) {
        RealtimeEvent event = connection.receive(Duration.ofSeconds(90));
        require(event != null, "Realtime ended before Work completion");
        log(
            out,
            "work_event",
            "type",
            event.type(),
            "event_id",
            event.eventId(),
            "work_id",
            event.workId());
        tracker.accept(event);
        if ("pong".equals(event.type())) pong = true;
        if (event.audio() != null && cancelled.add(event.workId() + "/" + event.announcementId()))
          connection.playbackCancelled(event.workId(), event.announcementId());
      }
      String outcome = connection.closeGracefully().outcome();
      require(
          Arrays.asList("no_active_response", "no_content", "saved", "saved_interrupted")
              .contains(outcome),
          "Unexpected Work close outcome");
      log(out, "work_closed", "work_id", tracker.id, "outcome", outcome);
    }
    verifyHistory(client, conversation, tracker.id, out);
  }

  static void verifyHistory(
      ForwardClient client, String conversation, String workId, PrintStream out) {
    RealtimeHistoryPage first =
        client
            .realtime()
            .conversations()
            .history(
                conversation,
                RealtimeHistoryParams.builder().limit(1).types("work").build(),
                options());
    Set<String> ids = new HashSet<>();
    int pages = 0;
    boolean completed = false;
    for (RealtimeHistoryPage page : first.iterPages()) {
      for (RealtimeHistoryEvent event : page.events()) {
        log(
            out,
            "work_history",
            "id",
            event.id(),
            "type",
            event.type(),
            "work_id",
            event.workId(),
            "page",
            pages + 1,
            "request_id",
            page.requestId());
        require(
            event.id() != null && ids.add(event.id()), "Work history cursor repeated or lacks ID");
        // types=work selects stored Work records; spoken progress is projected as a message.
        boolean workEvent = event.type() != null && event.type().startsWith("voice.work.");
        boolean spokenWork =
            "voice.assistant_message.completed".equals(event.type())
                && "assistant".equals(event.role())
                && "completed".equals(event.status())
                && event.text() != null
                && !event.text().trim().isEmpty();
        require(
            workId.equals(event.workId()) && (workEvent || spokenWork),
            "Work history contains an unrelated or invalid event");
        if (workId.equals(event.workId()) && "voice.work.completed".equals(event.type()))
          completed = true;
      }
      require(++pages < 30 || !page.hasNextPage(), "Work history exceeded page limit");
    }
    require(completed, "Completed Work was not persisted in History");
    log(out, "work_verified", "work_id", workId, "history_pages", pages);
  }

  static final class WorkTracker {
    String id;
    boolean completed;

    void accept(RealtimeEvent event) {
      require(
          event.error() == null && !"work.failed".equals(event.type()),
          "Realtime Work returned a failure");
      if ("work.accepted".equals(event.type())) {
        require(event.workId() != null && !event.workId().isEmpty(), "Accepted Work has no ID");
        require(id == null || id.equals(event.workId()), "Single task produced multiple Work IDs");
        id = event.workId();
      }
      if ("work.completed".equals(event.type())) {
        require(
            id != null && id.equals(event.workId()),
            "Work completion does not match accepted Work");
        require(
            event.work() != null && "completed".equals(event.work().status()),
            "Work completion payload status differs");
        completed = true;
      }
    }
  }
}
