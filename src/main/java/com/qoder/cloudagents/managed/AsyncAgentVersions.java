package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncAgentVersions {
  private final ClientEngine engine;

  public AsyncAgentVersions(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<AgentVersionListResponse> list(String agentId) {
    return list(agentId, AgentVersionListParams.builder().build());
  }

  public CompletableFuture<AgentVersionListResponse> list(String agentId, RequestOptions options) {
    return list(agentId, AgentVersionListParams.builder().build(), options);
  }

  public CompletableFuture<AgentVersionListResponse> list(
      String agentId, AgentVersionListParams params) {
    return list(agentId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<AgentVersionListResponse> list(
      String agentId, AgentVersionListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(agentId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<AgentVersionListResponse>> list(String agentId) {
      return list(agentId, AgentVersionListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<AgentVersionListResponse>> list(
        String agentId, RequestOptions options) {
      return list(agentId, AgentVersionListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<AgentVersionListResponse>> list(
        String agentId, AgentVersionListParams params) {
      return list(agentId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<AgentVersionListResponse>> list(
        String agentId, AgentVersionListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine,
          new String[] {"agents", agentId, "versions"},
          params.query(),
          options,
          AgentVersionListResponse::new);
    }
  }
}
