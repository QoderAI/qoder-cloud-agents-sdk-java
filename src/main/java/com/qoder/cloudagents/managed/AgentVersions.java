package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class AgentVersions {
  private final AsyncAgentVersions async;

  public AgentVersions(AsyncAgentVersions async) {
    this.async = async;
  }

  public AgentVersionListResponse list(String agentId) {
    return list(agentId, AgentVersionListParams.builder().build());
  }

  public AgentVersionListResponse list(String agentId, RequestOptions options) {
    return list(agentId, AgentVersionListParams.builder().build(), options);
  }

  public AgentVersionListResponse list(String agentId, AgentVersionListParams params) {
    return list(agentId, params, RequestOptions.DEFAULT);
  }

  public AgentVersionListResponse list(
      String agentId, AgentVersionListParams params, RequestOptions options) {
    return Futures.await(async.list(agentId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<AgentVersionListResponse> list(String agentId) {
      return list(agentId, AgentVersionListParams.builder().build());
    }

    public ApiResponse<AgentVersionListResponse> list(String agentId, RequestOptions options) {
      return list(agentId, AgentVersionListParams.builder().build(), options);
    }

    public ApiResponse<AgentVersionListResponse> list(
        String agentId, AgentVersionListParams params) {
      return list(agentId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<AgentVersionListResponse> list(
        String agentId, AgentVersionListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(agentId, params, options));
    }
  }
}
