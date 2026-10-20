package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.realtime.*;
import java.io.PrintStream;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * Single-turn text/raw-PCM example. Requires an existing Identity and Realtime-enabled Template.
 */
public final class RealtimeExample {
  private RealtimeExample() {}

  public static void main(String[] args) throws Exception {
    String path = System.getenv("QODER_PCM_FILE");
    byte[] pcm = null;
    if (path != null && !path.isEmpty()) {
      Path file = Paths.get(path);
      if (Files.size(file) > 960000)
        throw new IllegalArgumentException("PCM file must be at most 30 seconds");
      pcm = Files.readAllBytes(file);
    }
    String text = System.getenv("QODER_REALTIME_TEXT");
    validateInput(text, pcm);
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      String id = System.getenv("QODER_CONVERSATION_ID");
      if (id == null || id.isEmpty()) {
        id =
            create(
                setup.client(),
                required("QODER_IDENTITY_ID"),
                required("QODER_TEMPLATE_ID"),
                required("QODER_IDEMPOTENCY_KEY"));
        System.out.println("conversation_id=" + id + " (retained by the service)");
      }
      run(setup.client(), id, text, pcm, System.out);
      history(setup.client(), id, 10, System.out);
    }
  }

  private static String required(String name) {
    String value = System.getenv(name);
    if (value == null || value.isEmpty()) throw new IllegalArgumentException("Set " + name);
    return value;
  }

  public static String create(
      ForwardClient client, String identityId, String templateId, String stableKey) {
    return client
        .realtime()
        .conversations()
        .create(
            RealtimeConversationCreateParams.builder()
                .identityId(identityId)
                .templateId(templateId)
                .title("Java SDK Realtime example")
                .build(),
            RequestOptions.builder()
                .idempotencyKey(stableKey)
                .totalTimeout(Duration.ofMinutes(1))
                .build())
        .id();
  }

  static void validateInput(String text, byte[] pcm) {
    if (text != null && (text.trim().isEmpty() || pcm != null))
      throw new IllegalArgumentException("Choose nonempty text or raw PCM, not both");
    if (pcm != null) {
      if (pcm.length == 0 || pcm.length % 2 != 0 || pcm.length > 960000)
        throw new IllegalArgumentException("PCM must contain complete samples, at most 30 seconds");
      if (pcm.length >= 12
          && pcm[0] == 'R'
          && pcm[1] == 'I'
          && pcm[2] == 'F'
          && pcm[3] == 'F'
          && pcm[8] == 'W'
          && pcm[9] == 'A'
          && pcm[10] == 'V'
          && pcm[11] == 'E')
        throw new IllegalArgumentException("Provide raw PCM16, not a WAV container");
    }
  }

  public static void run(ForwardClient client, String id, String text, byte[] pcm, PrintStream out)
      throws Exception {
    validateInput(text, pcm);
    ExecutorService sender =
        Executors.newSingleThreadExecutor(
            action -> {
              Thread t = new Thread(action, "example-pcm-sender");
              t.setDaemon(true);
              return t;
            });
    Future<?> input = null;
    try (RealtimeConnection connection =
        client
            .realtime()
            .connect(
                RealtimeConnectParams.builder().conversationId(id).build(),
                RequestOptions.builder().totalTimeout(Duration.ofMinutes(2)).build())) {
      out.println("conversation_id=" + id + " request_id=" + connection.requestId());
      if (text != null) connection.sendText(text);
      if (pcm != null)
        input =
            sender.submit(
                () -> {
                  try {
                    for (int start = 0; start < pcm.length; start += 640) {
                      connection.appendAudio(
                          Arrays.copyOfRange(pcm, start, Math.min(start + 640, pcm.length)));
                      Thread.sleep(20); // 20 ms of 16 kHz mono PCM16 per chunk.
                    }
                  } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Audio input interrupted");
                  }
                });
      connection.ping();
      boolean user = pcm == null, assistant = false, audioDone = false, pong = false;
      long outputBytes = 0;
      Set<String> cancelled = new HashSet<>();
      while (true) {
        if (input != null && input.isDone()) input.get();
        RealtimeEvent event = connection.receive(Duration.ofSeconds(60));
        if (event == null)
          throw new IllegalStateException("Connection ended before turn completion");
        out.println("event_id=" + event.eventId() + " type=" + event.type());
        if (event.transcript() != null
            && event.type().equals("transcript.final")
            && !event.transcript().text().trim().isEmpty()) {
          user |= "user".equals(event.transcript().role());
          assistant |= "assistant".equals(event.transcript().role());
        }
        if (event.audio() != null) {
          outputBytes += event.audio().length;
          out.println("output_pcm_bytes=" + event.audio().length);
          if (cancelled.add(event.workId() + "/" + event.announcementId()))
            connection.playbackCancelled(event.workId(), event.announcementId());
        }
        if (event.error() != null)
          throw new IllegalStateException(
              "Realtime returned an error; inspect its structured payload");
        if (event.type().equals("audio.done")) audioDone = true;
        if (event.type().equals("pong")) pong = true;
        if ((text == null && pcm == null && pong) || (user && assistant && audioDone && pong))
          break;
      }
      if (input != null) input.get(35, TimeUnit.SECONDS);
      if ((text != null || pcm != null) && outputBytes == 0)
        throw new IllegalStateException("Turn finished without output PCM");
      String outcome = connection.closeGracefully().outcome();
      out.println("close_outcome=" + outcome);
      if (!Arrays.asList("no_active_response", "no_content", "saved", "saved_interrupted")
          .contains(outcome)) throw new IllegalStateException("Unexpected graceful close outcome");
      out.println("verified_output_pcm_bytes=" + outputBytes + " verified_pong=" + pong);
    } finally {
      if (input != null) input.cancel(true);
      sender.shutdownNow();
      if (!sender.awaitTermination(2, TimeUnit.SECONDS))
        throw new IllegalStateException("Audio sender did not stop");
    }
  }

  /** Read-only history, with an explicit page cap for a demo against an existing conversation. */
  public static void history(ForwardClient client, String id, int maxPages, PrintStream out) {
    if (maxPages < 1) throw new IllegalArgumentException("maxPages must be positive");
    RealtimeHistoryPage first =
        client
            .realtime()
            .conversations()
            .history(id, RealtimeHistoryParams.builder().limit(50).types("message,work").build());
    int pages = 0;
    for (RealtimeHistoryPage page : first.iterPages()) {
      for (RealtimeHistoryEvent event : page.events())
        out.println("history_id=" + event.id() + " type=" + event.type());
      if (++pages >= maxPages) {
        if (page.hasNextPage()) out.println("History page cap reached");
        break;
      }
    }
  }
}
