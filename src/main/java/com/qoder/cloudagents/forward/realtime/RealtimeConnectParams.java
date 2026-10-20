package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.time.Duration;

/** Immutable connection limits. No automatic reconnect or input replay is performed. */
public final class RealtimeConnectParams {
  private final String conversationId;
  private final Duration handshakeTimeout, readyTimeout, sendTimeout, closeTimeout;
  private final int queueCapacity, queueBytes, readLimit;

  private RealtimeConnectParams(Builder b) {
    RealtimeValidation.text(b.conversationId, "conversation_id", 128);
    conversationId = b.conversationId;
    handshakeTimeout = b.handshakeTimeout;
    readyTimeout = b.readyTimeout;
    sendTimeout = b.sendTimeout;
    closeTimeout = b.closeTimeout;
    queueCapacity = b.queueCapacity;
    queueBytes = b.queueBytes;
    readLimit = b.readLimit;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String conversationId() {
    return conversationId;
  }

  public Duration handshakeTimeout() {
    return handshakeTimeout;
  }

  public Duration readyTimeout() {
    return readyTimeout;
  }

  public Duration sendTimeout() {
    return sendTimeout;
  }

  public Duration closeTimeout() {
    return closeTimeout;
  }

  public int queueCapacity() {
    return queueCapacity;
  }

  public int queueBytes() {
    return queueBytes;
  }

  /** Maximum decoded text-message bytes accepted before JSON parsing. */
  public int readLimit() {
    return readLimit;
  }

  public static final class Builder {
    private String conversationId;
    private Duration handshakeTimeout = Duration.ofSeconds(30),
        readyTimeout = Duration.ofSeconds(60),
        sendTimeout = Duration.ofSeconds(30),
        closeTimeout = Duration.ofSeconds(10);
    private int queueCapacity = 256, queueBytes = 4 * 1024 * 1024, readLimit = 1024 * 1024;

    private Builder() {}

    public Builder conversationId(String v) {
      conversationId = v;
      return this;
    }

    public Builder handshakeTimeout(Duration v) {
      RequestOptions.positiveMillis(v);
      handshakeTimeout = v;
      return this;
    }

    public Builder readyTimeout(Duration v) {
      RequestOptions.positiveMillis(v);
      readyTimeout = v;
      return this;
    }

    public Builder sendTimeout(Duration v) {
      RequestOptions.positiveMillis(v);
      sendTimeout = v;
      return this;
    }

    public Builder closeTimeout(Duration v) {
      RequestOptions.positiveMillis(v);
      closeTimeout = v;
      return this;
    }

    public Builder queueCapacity(int v) {
      positive(v);
      queueCapacity = v;
      return this;
    }

    public Builder queueBytes(int v) {
      positive(v);
      queueBytes = v;
      return this;
    }

    public Builder readLimit(int v) {
      positive(v);
      readLimit = v;
      return this;
    }

    private void positive(int v) {
      if (v <= 0) throw new ConfigurationException("Connection limits must be positive");
    }

    public RealtimeConnectParams build() {
      return new RealtimeConnectParams(this);
    }
  }
}
