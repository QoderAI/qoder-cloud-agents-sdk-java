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

  public CompletableFuture<ChannelPairingListResponse> list(ChannelPairingListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ChannelPairingListResponse> list(
      ChannelPairingListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<ChannelPairing> retrieve(String pairingId) {
    return retrieve(pairingId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ChannelPairing> retrieve(String pairingId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(pairingId, options), ApiResponse::data);
  }

  public CompletableFuture<ChannelPairing> update(
      String pairingId, ChannelPairingUpdateParams params) {
    return update(pairingId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<ChannelPairing> update(
      String pairingId, ChannelPairingUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(pairingId, params, options), ApiResponse::data);
  }

  public CompletableFuture<ChannelPairingListResponse> list() {
    return list(ChannelPairingListParams.builder().build());
  }

  public CompletableFuture<ChannelPairingListResponse> list(RequestOptions options) {
    return list(ChannelPairingListParams.builder().build(), options);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<ChannelPairingListResponse>> list() {
      return list(ChannelPairingListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<ChannelPairingListResponse>> list(RequestOptions options) {
      return list(ChannelPairingListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<ChannelPairingListResponse>> list(
        ChannelPairingListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ChannelPairingListResponse>> list(
        ChannelPairingListParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return CursorRequests.list(
          engine,
          new String[] {"channel_pairings"},
          params.query(),
          options,
          ChannelPairingListResponse::new);
    }

    public CompletableFuture<ApiResponse<ChannelPairing>> retrieve(String pairingId) {
      return retrieve(pairingId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ChannelPairing>> retrieve(
        String pairingId, RequestOptions options) {
      if (options == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"channel_pairings", pairingId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new ChannelPairing(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<ChannelPairing>> update(
        String pairingId, ChannelPairingUpdateParams params) {
      return update(pairingId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<ChannelPairing>> update(
        String pairingId, ChannelPairingUpdateParams params, RequestOptions options) {
      if (options == null || params == null)
        throw new ConfigurationException("Request parameters and options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"channel_pairings", pairingId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new ChannelPairing(v, r.requestId())));
    }

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
