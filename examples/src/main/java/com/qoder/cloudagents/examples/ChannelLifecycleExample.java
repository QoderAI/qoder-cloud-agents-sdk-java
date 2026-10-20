package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import java.io.PrintStream;
import java.time.Duration;
import java.util.UUID;

/** Creates a disabled, unauthorised pairing Channel and deletes only that new Channel. */
public final class ChannelLifecycleExample {
  private ChannelLifecycleExample() {}

  public static void main(String[] args) {
    if (!"true".equals(System.getenv("QODER_ALLOW_WRITE")))
      throw new IllegalArgumentException("Set QODER_ALLOW_WRITE=true");
    String type = System.getenv("QODER_CHANNEL_TYPE");
    if (type == null || type.isEmpty())
      throw new IllegalArgumentException("Set QODER_CHANNEL_TYPE to a QR-capable channel type");
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), type, System.out);
    }
  }

  public static void run(ForwardClient client, String channelType, PrintStream out) {
    if (!"wechat".equals(channelType)
        && !"wecom".equals(channelType)
        && !"feishu".equals(channelType)
        && !"dingtalk".equals(channelType))
      throw new IllegalArgumentException(
          "This credential-free example needs a QR-capable channel type");
    String key = UUID.randomUUID().toString(), channelId = null;
    RuntimeException failure = null;
    out.println("channel_create_key=" + key + "-create");
    try {
      Channel created =
          client
              .channels()
              .create(
                  ChannelCreateParams.builder()
                      .channelType(channelType)
                      .name("sdk-java-disabled-" + key)
                      .identityResolution(
                          ChannelIdentityResolution.builder().mode("pairing").build())
                      .enabled(false)
                      .build(),
                  write(key + "-create"));
      channelId = created.id();
      out.println("created_channel=" + channelId);
      client.channels().retrieve(channelId, read());
      Channel updated =
          client
              .channels()
              .update(
                  channelId,
                  ChannelUpdateParams.builder()
                      .name("sdk-java-updated-" + key)
                      .enabled(false)
                      .channelConfig(
                          ChannelConfigParams.builder()
                              .responseOptions(
                                  ChannelResponseOptions.builder()
                                      .includeToolCalls(false)
                                      .includeThinking(false)
                                      .build())
                              .build())
                      .build(),
                  write(key + "-update"));
      if (!Boolean.FALSE.equals(updated.enabled()))
        throw new IllegalStateException("Expected the example Channel to stay disabled");
      ChannelListResponse page =
          client
              .channels()
              .list(
                  ChannelListParams.builder()
                      .channelType(channelType)
                      .enabled(false)
                      .limit(10)
                      .build(),
                  read());
      out.println("listed_channels=" + page.data().size() + " request_id=" + page.requestId());
    } catch (RuntimeException e) {
      failure = e;
      throw e;
    } finally {
      if (channelId != null)
        try {
          client.channels().delete(channelId, write(key + "-delete"));
          out.println("deleted_channel=" + channelId);
        } catch (RuntimeException e) {
          out.println("cleanup_failed_channel=" + channelId);
          if (failure != null) failure.addSuppressed(e);
          else throw e;
        }
    }
  }

  private static RequestOptions read() {
    return RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build();
  }

  private static RequestOptions write(String key) {
    return read().toBuilder().idempotencyKey(key).build();
  }
}
