package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class ChannelPairings {
  private final AsyncChannelPairings async;

  public ChannelPairings(AsyncChannelPairings async) {
    this.async = async;
  }

  public ChannelPairing create(ChannelPairingCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public ChannelPairing create(ChannelPairingCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public DeletedChannelPairing delete(String pairingId) {
    return delete(pairingId, RequestOptions.DEFAULT);
  }

  public DeletedChannelPairing delete(String pairingId, RequestOptions options) {
    return Futures.await(async.delete(pairingId, options));
  }

  public ChannelPairingListResponse list(ChannelPairingListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public ChannelPairingListResponse list(ChannelPairingListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public ChannelPairing retrieve(String pairingId) {
    return retrieve(pairingId, RequestOptions.DEFAULT);
  }

  public ChannelPairing retrieve(String pairingId, RequestOptions options) {
    return Futures.await(async.retrieve(pairingId, options));
  }

  public ChannelPairing update(String pairingId, ChannelPairingUpdateParams params) {
    return update(pairingId, params, RequestOptions.DEFAULT);
  }

  public ChannelPairing update(
      String pairingId, ChannelPairingUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(pairingId, params, options));
  }

  public ChannelPairingListResponse list() {
    return list(ChannelPairingListParams.builder().build());
  }

  public ChannelPairingListResponse list(RequestOptions options) {
    return list(ChannelPairingListParams.builder().build(), options);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<ChannelPairingListResponse> list() {
      return list(ChannelPairingListParams.builder().build());
    }

    public ApiResponse<ChannelPairingListResponse> list(RequestOptions options) {
      return list(ChannelPairingListParams.builder().build(), options);
    }

    public ApiResponse<ChannelPairingListResponse> list(ChannelPairingListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<ChannelPairingListResponse> list(
        ChannelPairingListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<ChannelPairing> retrieve(String pairingId) {
      return retrieve(pairingId, RequestOptions.DEFAULT);
    }

    public ApiResponse<ChannelPairing> retrieve(String pairingId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(pairingId, options));
    }

    public ApiResponse<ChannelPairing> update(String pairingId, ChannelPairingUpdateParams params) {
      return update(pairingId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<ChannelPairing> update(
        String pairingId, ChannelPairingUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(pairingId, params, options));
    }

    public ApiResponse<ChannelPairing> create(ChannelPairingCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<ChannelPairing> create(
        ChannelPairingCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<DeletedChannelPairing> delete(String pairingId) {
      return delete(pairingId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedChannelPairing> delete(String pairingId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(pairingId, options));
    }
  }
}
