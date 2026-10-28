package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Models {
  private final AsyncModels async;

  public Models(AsyncModels async) {
    this.async = async;
  }

  public ModelListResponse list() {
    return list(RequestOptions.DEFAULT);
  }

  public ModelListResponse list(RequestOptions options) {
    return Futures.await(async.list(options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<ModelListResponse> list() {
      return list(RequestOptions.DEFAULT);
    }

    public ApiResponse<ModelListResponse> list(RequestOptions options) {
      return Futures.await(async.withResponse().list(options));
    }
  }
}
