package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncAgents {
  private final ClientEngine engine;

  public AsyncAgents(ClientEngine engine) {
    this.engine = engine;
  }

  public AsyncAgentVersions versions() {
    return new AsyncAgentVersions(engine);
  }

  public CompletableFuture<Agent> archive(String agentId) {
    return archive(agentId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Agent> archive(String agentId, RequestOptions options) {
    return Futures.map(withResponse().archive(agentId, options), ApiResponse::data);
  }

  public CompletableFuture<Agent> create(AgentCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Agent> create(AgentCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<Agent> retrieve(String agentId) {
    return retrieve(agentId, AgentRetrieveParams.builder().build());
  }

  public CompletableFuture<Agent> retrieve(String agentId, RequestOptions options) {
    return retrieve(agentId, AgentRetrieveParams.builder().build(), options);
  }

  public CompletableFuture<Agent> retrieve(String agentId, AgentRetrieveParams params) {
    return retrieve(agentId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Agent> retrieve(
      String agentId, AgentRetrieveParams params, RequestOptions options) {
    return Futures.map(withResponse().retrieve(agentId, params, options), ApiResponse::data);
  }

  public CompletableFuture<AgentListResponse> list() {
    return list(AgentListParams.builder().build());
  }

  public CompletableFuture<AgentListResponse> list(RequestOptions options) {
    return list(AgentListParams.builder().build(), options);
  }

  public CompletableFuture<AgentListResponse> list(AgentListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<AgentListResponse> list(AgentListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<AgentListResponse> search() {
    return search(AgentSearchParams.builder().build());
  }

  public CompletableFuture<AgentListResponse> search(RequestOptions options) {
    return search(AgentSearchParams.builder().build(), options);
  }

  public CompletableFuture<AgentListResponse> search(AgentSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<AgentListResponse> search(
      AgentSearchParams params, RequestOptions options) {
    return Futures.map(withResponse().search(params, options), ApiResponse::data);
  }

  public CompletableFuture<Agent> update(String agentId, AgentUpdateParams params) {
    return update(agentId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Agent> update(
      String agentId, AgentUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(agentId, params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<Agent>> archive(String agentId) {
      return archive(agentId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Agent>> archive(String agentId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"agents", agentId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Agent(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Agent>> create(AgentCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Agent>> create(
        AgentCreateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"agents"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Agent(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Agent>> retrieve(String agentId) {
      return retrieve(agentId, AgentRetrieveParams.builder().build());
    }

    public CompletableFuture<ApiResponse<Agent>> retrieve(String agentId, RequestOptions options) {
      return retrieve(agentId, AgentRetrieveParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<Agent>> retrieve(
        String agentId, AgentRetrieveParams params) {
      return retrieve(agentId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Agent>> retrieve(
        String agentId, AgentRetrieveParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "GET", new String[] {"agents", agentId}, params.query(), null, true, options),
          r -> r.map(v -> v.isNull() ? null : new Agent(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<AgentListResponse>> list() {
      return list(AgentListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<AgentListResponse>> list(RequestOptions options) {
      return list(AgentListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<AgentListResponse>> list(AgentListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<AgentListResponse>> list(
        AgentListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine, new String[] {"agents"}, params.query(), options, AgentListResponse::new);
    }

    public CompletableFuture<ApiResponse<AgentListResponse>> search() {
      return search(AgentSearchParams.builder().build());
    }

    public CompletableFuture<ApiResponse<AgentListResponse>> search(RequestOptions options) {
      return search(AgentSearchParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<AgentListResponse>> search(AgentSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<AgentListResponse>> search(
        AgentSearchParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return SearchRequests.search(
          engine,
          new String[] {"agents", "search"},
          params.toJsonValue(),
          Collections.emptyMap(),
          options,
          AgentListResponse::new);
    }

    public CompletableFuture<ApiResponse<Agent>> update(String agentId, AgentUpdateParams params) {
      return update(agentId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Agent>> update(
        String agentId, AgentUpdateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"agents", agentId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Agent(v, r.requestId())));
    }
  }
}
