package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import java.io.PrintStream;
import java.time.Duration;

/** Helpers for an application UI. QR secrets are returned to the caller, never printed. */
public final class ChannelAuthorizationExample {
  private ChannelAuthorizationExample() {}

  /** Starts user-driven authorization on an existing enabled Channel. Display the QR in your UI. */
  public static ChannelQRSession beginQr(
      ForwardClient client, String channelId, String idempotencyKey) {
    return client
        .channels()
        .qrSessions()
        .create(channelId, options().toBuilder().idempotencyKey(idempotencyKey).build());
  }

  /** One poll only. A confirmed QR refreshes the Channel but does not create a Pairing. */
  public static ChannelQRSession inspectQr(
      ForwardClient client, String sessionKey, PrintStream out) {
    ChannelQRSession qr = client.channels().qrSessions().retrieve(sessionKey, options());
    out.println(
        "qr_status="
            + qr.status()
            + " expires_at="
            + qr.expiresAt()
            + " request_id="
            + qr.requestId());
    if ("confirmed".equals(qr.status())) {
      Channel channel = client.channels().retrieve(qr.channelId(), options());
      out.println(
          "channel="
              + channel.id()
              + " enabled="
              + channel.enabled()
              + " binding_status="
              + channel.bindingStatus());
    }
    return qr;
  }

  /**
   * Authorizes a caller-selected message scope. Caller owns this binding; it is not auto-removed.
   */
  public static ChannelPairing pair(
      ForwardClient client,
      ChannelPairingCreateParams params,
      String idempotencyKey,
      PrintStream out) {
    ChannelPairing result =
        client
            .channelPairings()
            .create(params, options().toBuilder().idempotencyKey(idempotencyKey).build());
    out.println(
        "pairing="
            + result.id()
            + " status="
            + result.status()
            + " request_id="
            + result.requestId());
    return result;
  }

  /** Explicitly revokes only the binding selected by the caller. */
  public static void unpair(ForwardClient client, String pairingId, PrintStream out) {
    client.channelPairings().delete(pairingId, options());
    out.println("unpaired=" + pairingId);
  }

  private static RequestOptions options() {
    return RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build();
  }
}
