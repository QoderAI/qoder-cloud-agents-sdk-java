package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncChannelPairings {
  private final ClientEngine engine;

  public AsyncChannelPairings(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<ChannelPairing> create(ChannelPairingCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ChannelPairing> create(
      ChannelPairingCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedChannelPairing> delete(String pairingId) {
    return delete(pairingId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedChannelPairing> delete(String pairingId, RequestOptions options) {
    return Futures.map(withResponse().delete(pairingId, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<ChannelPairing>> create(
        ChannelPairingCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ChannelPairing>> create(
        ChannelPairingCreateParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"channel_pairings"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new ChannelPairing(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedChannelPairing>> delete(String pairingId) {
      return delete(pairingId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedChannelPairing>> delete(
        String pairingId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"channel_pairings", pairingId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedChannelPairing(v, r.requestId())));
    }
  }
}
