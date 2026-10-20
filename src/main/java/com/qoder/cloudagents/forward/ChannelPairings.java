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

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
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
