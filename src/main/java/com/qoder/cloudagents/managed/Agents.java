package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Agents {
  private final AsyncAgents async;

  public Agents(AsyncAgents async) {
    this.async = async;
  }

  public AgentVersions versions() {
    return new AgentVersions(async.versions());
  }

  public Agent archive(String agentId) {
    return archive(agentId, RequestOptions.DEFAULT);
  }

  public Agent archive(String agentId, RequestOptions options) {
    return Futures.await(async.archive(agentId, options));
  }

  public Agent create(AgentCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Agent create(AgentCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public Agent retrieve(String agentId) {
    return retrieve(agentId, AgentRetrieveParams.builder().build());
  }

  public Agent retrieve(String agentId, RequestOptions options) {
    return retrieve(agentId, AgentRetrieveParams.builder().build(), options);
  }

  public Agent retrieve(String agentId, AgentRetrieveParams params) {
    return retrieve(agentId, params, RequestOptions.DEFAULT);
  }

  public Agent retrieve(String agentId, AgentRetrieveParams params, RequestOptions options) {
    return Futures.await(async.retrieve(agentId, params, options));
  }

  public AgentListResponse list() {
    return list(AgentListParams.builder().build());
  }

  public AgentListResponse list(RequestOptions options) {
    return list(AgentListParams.builder().build(), options);
  }

  public AgentListResponse list(AgentListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public AgentListResponse list(AgentListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public AgentListResponse search() {
    return search(AgentSearchParams.builder().build());
  }

  public AgentListResponse search(RequestOptions options) {
    return search(AgentSearchParams.builder().build(), options);
  }

  public AgentListResponse search(AgentSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public AgentListResponse search(AgentSearchParams params, RequestOptions options) {
    return Futures.await(async.search(params, options));
  }

  public Agent update(String agentId, AgentUpdateParams params) {
    return update(agentId, params, RequestOptions.DEFAULT);
  }

  public Agent update(String agentId, AgentUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(agentId, params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<Agent> archive(String agentId) {
      return archive(agentId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Agent> archive(String agentId, RequestOptions options) {
      return Futures.await(async.withResponse().archive(agentId, options));
    }

    public ApiResponse<Agent> create(AgentCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Agent> create(AgentCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<Agent> retrieve(String agentId) {
      return retrieve(agentId, AgentRetrieveParams.builder().build());
    }

    public ApiResponse<Agent> retrieve(String agentId, RequestOptions options) {
      return retrieve(agentId, AgentRetrieveParams.builder().build(), options);
    }

    public ApiResponse<Agent> retrieve(String agentId, AgentRetrieveParams params) {
      return retrieve(agentId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Agent> retrieve(
        String agentId, AgentRetrieveParams params, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(agentId, params, options));
    }

    public ApiResponse<AgentListResponse> list() {
      return list(AgentListParams.builder().build());
    }

    public ApiResponse<AgentListResponse> list(RequestOptions options) {
      return list(AgentListParams.builder().build(), options);
    }

    public ApiResponse<AgentListResponse> list(AgentListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<AgentListResponse> list(AgentListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<AgentListResponse> search() {
      return search(AgentSearchParams.builder().build());
    }

    public ApiResponse<AgentListResponse> search(RequestOptions options) {
      return search(AgentSearchParams.builder().build(), options);
    }

    public ApiResponse<AgentListResponse> search(AgentSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<AgentListResponse> search(AgentSearchParams params, RequestOptions options) {
      return Futures.await(async.withResponse().search(params, options));
    }

    public ApiResponse<Agent> update(String agentId, AgentUpdateParams params) {
      return update(agentId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Agent> update(
        String agentId, AgentUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(agentId, params, options));
    }
  }
}
