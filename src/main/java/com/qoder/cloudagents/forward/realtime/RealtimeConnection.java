package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import okhttp3.*;
import okio.ByteString;

/**
 * One connection, with a single receive consumer and serialized sends. Close explicitly after use.
 * Async continuations run on SDK/transport threads: use thenAcceptAsync for blocking application
 * work.
 */
public final class RealtimeConnection implements AutoCloseable {
  private final ClientEngine engine;
  private final RealtimeConnectParams params;
  private final CompletableFuture<RealtimeConnection> connected = new CompletableFuture<>();
  private final Deque<Frame> inbound = new ArrayDeque<>();
  private final Deque<Send> outbound = new ArrayDeque<>();
  private long inboundBytes, outboundBytes;
  private WebSocket socket;
  private CompletableFuture<WebSocket> opening;
  private CompletableFuture<RealtimeEvent> receiving;
  private CompletableFuture<RealtimeCloseResult> closingResult;
  private ScheduledFuture<?> stageTimer, receiveTimer, closeTimer, sendPoll;
  private Runnable unwatch = () -> {};
  private RealtimeReady ready;
  private ApiResponse<Void> handshake;
  private RuntimeException failure;
  private boolean terminated, closing;
  private Integer closeCode;
  private String closeReason, closeId;

  private RealtimeConnection(ClientEngine engine, RealtimeConnectParams params) {
    this.engine = engine;
    this.params = params;
  }

  static CompletableFuture<RealtimeConnection> open(
      ClientEngine engine, RealtimeConnectParams params, RequestOptions options) {
    if (params == null || options == null)
      throw new ConfigurationException("Parameters and options must not be null");
    if (!options.query().isEmpty())
      throw new ConfigurationException("Realtime connection only accepts conversation_id");
    RealtimeConnection connection = new RealtimeConnection(engine, params);
    connection.start(options);
    return connection.connected;
  }

  private synchronized void start(RequestOptions options) {
    connected.whenComplete(
        (v, e) -> {
          if (connected.isCancelled()) close();
        });
    unwatch = engine.watchStream(this::fail, options.totalTimeout());
    if (terminated) {
      unwatch.run();
      return;
    }
    Duration timeout = options.timeout() == null ? params.handshakeTimeout() : options.timeout();
    stageTimer =
        schedule(
            () -> {
              synchronized (this) {
                if (handshake == null)
                  fail(new ApiTimeoutException("Realtime handshake timed out"));
              }
            },
            timeout);
    if (terminated) return;
    opening =
        engine.webSocket(
            params.conversationId(), options, timeout, params.sendTimeout(), listener());
    opening.whenComplete(
        (ws, error) -> {
          synchronized (this) {
            if (error != null) {
              if (!terminated) fail(asRuntime(error));
            } else if (terminated) ws.cancel();
            else socket = ws;
          }
        });
  }

  public String conversationId() {
    return params.conversationId();
  }

  public synchronized String requestId() {
    return handshake == null ? null : handshake.requestId();
  }

  public synchronized ApiResponse<Void> handshakeResponse() {
    return handshake;
  }

  public synchronized RealtimeReady ready() {
    return ready;
  }

  public synchronized boolean isClosed() {
    return terminated;
  }

  public synchronized Integer closeCode() {
    return closeCode;
  }

  public synchronized String closeReason() {
    return closeReason;
  }

  /** Returns null after a normal close; buffered events are delivered before terminal failures. */
  public RealtimeEvent receive() {
    return Futures.await(receiveAsync());
  }

  public RealtimeEvent receive(Duration timeout) {
    return Futures.await(receiveAsync(timeout));
  }

  public CompletableFuture<RealtimeEvent> receiveAsync() {
    return receiveAsync(null);
  }

  /** Timeout/cancellation affects this receive only. The connection remains usable. */
  public synchronized CompletableFuture<RealtimeEvent> receiveAsync(Duration timeout) {
    if (timeout != null) RequestOptions.positiveMillis(timeout);
    if (receiving != null)
      throw new ConfigurationException("Only one Realtime receive may be pending");
    if (!inbound.isEmpty()) {
      Frame frame = inbound.removeFirst();
      inboundBytes -= frame.bytes;
      return CompletableFuture.completedFuture(frame.event);
    }
    if (terminated)
      return failure == null ? CompletableFuture.completedFuture(null) : Futures.failed(failure);
    CompletableFuture<RealtimeEvent> result = new CompletableFuture<>();
    receiving = result;
    if (timeout != null)
      receiveTimer =
          schedule(
              () -> {
                synchronized (this) {
                  if (receiving == result) {
                    receiving = null;
                    receiveTimer = null;
                    result.completeExceptionally(
                        new ApiTimeoutException("Realtime receive timed out"));
                  }
                }
              },
              timeout);
    result.whenComplete(
        (v, e) -> {
          synchronized (this) {
            if (receiving == result) {
              receiving = null;
              cancel(receiveTimer);
              receiveTimer = null;
            }
          }
        });
    return result;
  }

  public void sendText(String text) {
    Futures.await(sendTextAsync(text));
  }

  public CompletableFuture<Void> sendTextAsync(String text) {
    text = RealtimeValidation.trim(text);
    if (text == null
        || text.isEmpty()
        || !RealtimeValidation.validUnicode(text)
        || text.getBytes(StandardCharsets.UTF_8).length > 16384)
      throw new ConfigurationException("Realtime text must be nonempty and at most 16 KiB");
    return sendEventAsync("text.message", JsonValue.of(Collections.singletonMap("text", text)));
  }

  /** Raw PCM16 little-endian, 16 kHz, mono. No WAV/container headers. */
  public void appendAudio(byte[] pcm) {
    Futures.await(appendAudioAsync(pcm));
  }

  public CompletableFuture<Void> appendAudioAsync(byte[] pcm) {
    if (pcm == null || pcm.length == 0 || pcm.length % 2 != 0)
      throw new ConfigurationException("Audio must contain complete PCM16 samples");
    if (pcm.length > 196560)
      throw new ConfigurationException("Audio chunk exceeds the Realtime event limit");
    return sendEventAsync(
        "audio.append",
        JsonValue.of(Collections.singletonMap("audio", Base64.getEncoder().encodeToString(pcm))));
  }

  public void interrupt() {
    Futures.await(interruptAsync());
  }

  public CompletableFuture<Void> interruptAsync() {
    return sendEventAsync("interrupt", JsonValue.of(Collections.emptyMap()));
  }

  public void ping() {
    Futures.await(pingAsync());
  }

  public CompletableFuture<Void> pingAsync() {
    return sendEventAsync("ping", JsonValue.of(Collections.emptyMap()));
  }

  public void playbackStarted(String workId, String announcementId) {
    Futures.await(playbackStartedAsync(workId, announcementId));
  }

  public void playbackEnded(String workId, String announcementId) {
    Futures.await(playbackEndedAsync(workId, announcementId));
  }

  public void playbackCancelled(String workId, String announcementId) {
    Futures.await(playbackCancelledAsync(workId, announcementId));
  }

  public CompletableFuture<Void> playbackStartedAsync(String workId, String announcementId) {
    return playback("playback.started", workId, announcementId);
  }

  public CompletableFuture<Void> playbackEndedAsync(String workId, String announcementId) {
    return playback("playback.ended", workId, announcementId);
  }

  public CompletableFuture<Void> playbackCancelledAsync(String workId, String announcementId) {
    return playback("playback.cancelled", workId, announcementId);
  }

  private CompletableFuture<Void> playback(String type, String workId, String announcementId) {
    Map<String, String> payload = new LinkedHashMap<>();
    if (workId != null) payload.put("work_id", workId);
    if (announcementId != null) payload.put("announcement_id", announcementId);
    return sendEventAsync(type, JsonValue.of(payload));
  }

  public void sendEvent(String type, JsonValue payload) {
    Futures.await(sendEventAsync(type, payload));
  }

  /**
   * Sends JSON text frames. Completion means transport write completion, not server
   * acknowledgement.
   */
  public synchronized CompletableFuture<Void> sendEventAsync(String type, JsonValue payload) {
    return enqueue(type, payload, false);
  }

  private CompletableFuture<Void> enqueue(String type, JsonValue payload, boolean closeEvent) {
    if (terminated)
      return Futures.failed(
          failure == null ? new ApiUserAbortException("Realtime connection closed") : failure);
    if (ready == null || (closing && !closeEvent))
      return Futures.failed(
          new ConfigurationException("Realtime connection is not accepting input"));
    RealtimeValidation.text(type, "event type", 128);
    if (payload == null || !payload.isObject())
      throw new ConfigurationException("Event payload must be an object");
    Map<String, Object> frame = new LinkedHashMap<>();
    frame.put("version", RealtimeEvent.VERSION);
    frame.put("type", type);
    frame.put("payload", payload);
    String json = JsonValue.of(frame).toJson();
    int bytes = json.getBytes(StandardCharsets.UTF_8).length;
    if (bytes > 262144) throw new ConfigurationException("Realtime event exceeds 256 KiB");
    if (outbound.size() >= params.queueCapacity() || outboundBytes + bytes > params.queueBytes())
      return Futures.failed(problem("Realtime send queue is full", null));
    Send send = new Send(json, bytes);
    outbound.addLast(send);
    outboundBytes += bytes;
    send.deadline =
        schedule(
            () -> {
              synchronized (this) {
                if (!outbound.contains(send)) return;
                if (send.started)
                  fail(new ApiTimeoutException("Realtime send timed out; delivery is uncertain"));
                else {
                  remove(send);
                  send.result.completeExceptionally(
                      new ApiTimeoutException("Realtime send queue timed out"));
                }
              }
            },
            params.sendTimeout());
    send.result.whenComplete(
        (v, e) -> {
          synchronized (this) {
            if (send.result.isCancelled() && outbound.contains(send)) {
              if (send.started)
                fail(new ApiUserAbortException("Realtime send cancelled; delivery is uncertain"));
              else remove(send);
            }
          }
        });
    pump();
    return send.result;
  }

  private void pump() {
    if (terminated || outbound.isEmpty()) return;
    Send send = outbound.peekFirst();
    if (!send.started) {
      send.started = true;
      if (!socket.send(send.json)) {
        fail(problem("Realtime transport rejected a send", null));
        return;
      }
    }
    if (sendPoll == null)
      sendPoll =
          scheduleMillis(
              () -> {
                synchronized (this) {
                  sendPoll = null;
                  if (terminated) return;
                  Send current = outbound.peekFirst();
                  if (current != null && current.started && socket.queueSize() == 0) {
                    remove(current);
                    current.result.complete(null);
                  }
                  pump();
                }
              },
              5);
  }

  private void remove(Send send) {
    if (outbound.remove(send)) outboundBytes -= send.bytes;
    cancel(send.deadline);
  }

  public RealtimeCloseResult closeGracefully() {
    return Futures.await(closeGracefullyAsync());
  }

  /** Waits for connection.closed with this request ID before releasing the transport. */
  public synchronized CompletableFuture<RealtimeCloseResult> closeGracefullyAsync() {
    if (closingResult != null) return closingResult;
    if (terminated)
      return Futures.failed(
          failure == null
              ? new ApiUserAbortException("Realtime connection already closed")
              : failure);
    if (ready == null || !Boolean.TRUE.equals(ready.capabilities().gracefulClose()))
      return Futures.failed(new ConfigurationException("Server does not support graceful close"));
    closing = true;
    closeId = UUID.randomUUID().toString();
    closingResult = new CompletableFuture<>();
    closeTimer =
        schedule(
            () -> fail(new ApiTimeoutException("Realtime close acknowledgement timed out")),
            params.closeTimeout());
    closingResult.whenComplete(
        (v, e) -> {
          if (closingResult.isCancelled()) close();
        });
    enqueue("connection.close", JsonValue.of(Collections.singletonMap("request_id", closeId)), true)
        .whenComplete(
            (v, e) -> {
              if (e != null) fail(asRuntime(e));
            });
    return closingResult;
  }

  /** Releases the connection immediately, without claiming that pending output was saved. */
  @Override
  public synchronized void close() {
    terminate(null);
  }

  private synchronized void fail(RuntimeException error) {
    terminate(error);
  }

  private void terminate(RuntimeException error) {
    if (terminated) return;
    terminated = true;
    failure = error;
    cancel(stageTimer);
    cancel(receiveTimer);
    cancel(closeTimer);
    cancel(sendPoll);
    unwatch.run();
    if (opening != null && !opening.isDone()) opening.cancel(true);
    if (socket != null) socket.cancel();
    RuntimeException pendingError =
        error == null ? new ApiUserAbortException("Realtime connection closed") : error;
    connected.completeExceptionally(pendingError);
    if (closingResult != null && !closingResult.isDone())
      closingResult.completeExceptionally(pendingError);
    List<Send> sends = new ArrayList<>(outbound);
    outbound.clear();
    outboundBytes = 0;
    for (Send send : sends) {
      cancel(send.deadline);
      send.result.completeExceptionally(pendingError);
    }
    if (receiving != null) {
      CompletableFuture<RealtimeEvent> target = receiving;
      receiving = null;
      if (error == null) target.complete(null);
      else target.completeExceptionally(error);
    }
  }

  private ScheduledFuture<?> schedule(Runnable action, Duration duration) {
    return scheduleMillis(action, RequestOptions.positiveMillis(duration));
  }

  private ScheduledFuture<?> scheduleMillis(Runnable action, long millis) {
    try {
      return engine.schedule(action, millis);
    } catch (RejectedExecutionException e) {
      fail(new ApiUserAbortException("Client closed"));
      return null;
    }
  }

  private static void cancel(ScheduledFuture<?> task) {
    if (task != null) task.cancel(false);
  }

  private RuntimeException asRuntime(Throwable e) {
    Throwable cause = Futures.unwrap(e);
    return cause instanceof RuntimeException
        ? (RuntimeException) cause
        : problem("Realtime connection failed", null);
  }

  private RealtimeException problem(String message, JsonValue event) {
    return new RealtimeException(
        message, conversationId(), requestId(), closeCode, closeReason, event);
  }

  private WebSocketListener listener() {
    return new WebSocketListener() {
      @Override
      public void onOpen(WebSocket ws, Response response) {
        synchronized (RealtimeConnection.this) {
          if (terminated) {
            ws.cancel();
            return;
          }
          socket = ws;
          handshake = new ApiResponse<>(null, response.code(), response.headers().toMultimap());
          cancel(stageTimer);
          stageTimer =
              schedule(
                  () -> {
                    synchronized (RealtimeConnection.this) {
                      if (ready == null)
                        fail(new ApiTimeoutException("Realtime voice.ready timed out"));
                    }
                  },
                  params.readyTimeout());
        }
      }

      @Override
      public void onMessage(WebSocket ws, String text) {
        synchronized (RealtimeConnection.this) {
          if (terminated) return;
          try {
            int bytes = text.getBytes(StandardCharsets.UTF_8).length;
            if (bytes > params.readLimit())
              throw problem("Realtime message exceeds read limit", null);
            RealtimeEvent event = RealtimeEvent.fromJson(JsonValue.parse(text));
            if (!event.conversationId().equals(conversationId()))
              throw problem("Realtime conversation ID mismatch", null);
            if (event.fatal() || (ready == null && event.error() != null)) {
              fail(problem("Realtime server rejected the connection", event.toJsonValue()));
              return;
            }
            boolean firstReady = ready == null && event.ready() != null;
            if (firstReady) {
              ready = event.ready();
              cancel(stageTimer);
            }
            boolean delivered = false;
            if (receiving != null) {
              CompletableFuture<RealtimeEvent> target = receiving;
              receiving = null;
              cancel(receiveTimer);
              receiveTimer = null;
              delivered = target.complete(event);
            }
            if (!delivered) {
              if (inbound.size() >= params.queueCapacity()
                  || inboundBytes + bytes > params.queueBytes())
                throw problem("Realtime receive queue is full", null);
              inbound.addLast(new Frame(event, bytes));
              inboundBytes += bytes;
            }
            if (firstReady) connected.complete(RealtimeConnection.this);
            if (closingResult != null
                && event.closed() != null
                && closeId.equals(event.closed().requestId())) {
              closingResult.complete(event.closed());
              terminate(null);
            }
          } catch (RuntimeException e) {
            fail(e);
          }
        }
      }

      @Override
      public void onMessage(WebSocket ws, ByteString bytes) {
        fail(problem("Expected a Realtime JSON text frame", null));
      }

      @Override
      public void onClosing(WebSocket ws, int code, String reason) {
        synchronized (RealtimeConnection.this) {
          closeCode = code;
          closeReason = reason;
          ws.close(1000, "");
          if (code == 1000 && ready != null && !closing) terminate(null);
          else fail(problem("Realtime closed before completion", null));
        }
      }

      @Override
      public void onClosed(WebSocket ws, int code, String reason) {
        synchronized (RealtimeConnection.this) {
          closeCode = code;
          closeReason = reason;
          if (!terminated) terminate(null);
        }
      }

      @Override
      public void onFailure(WebSocket ws, Throwable error, Response response) {
        synchronized (RealtimeConnection.this) {
          if (terminated) return;
          if (response != null && handshake == null)
            handshake = new ApiResponse<>(null, response.code(), response.headers().toMultimap());
          if (closeCode == null) closeCode = 1006;
          fail(asRuntime(error));
        }
      }
    };
  }

  private static final class Frame {
    final RealtimeEvent event;
    final int bytes;

    Frame(RealtimeEvent event, int bytes) {
      this.event = event;
      this.bytes = bytes;
    }
  }

  private static final class Send {
    final String json;
    final int bytes;
    final CompletableFuture<Void> result = new CompletableFuture<>();
    boolean started;
    ScheduledFuture<?> deadline;

    Send(String json, int bytes) {
      this.json = json;
      this.bytes = bytes;
    }
  }
}
