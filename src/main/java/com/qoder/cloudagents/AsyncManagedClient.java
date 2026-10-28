package com.qoder.cloudagents;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.ClientEngine;
import com.qoder.cloudagents.errors.ConfigurationException;
import com.qoder.cloudagents.managed.*;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** Thread-safe Managed API client. Close owned clients after use. */
public final class AsyncManagedClient implements AutoCloseable {
  private final ClientEngine engine;
  private final ManagedClient.Builder configuration;
  private final boolean ownsEngine;
  private final AsyncAgents agents;
  private final AsyncDeployments deployments;
  private final AsyncDeploymentRuns deploymentRuns;
  private final AsyncSessions sessions;
  private final AsyncEnvironments environments;
  private final AsyncFiles files;
  private final AsyncSkills skills;
  private final AsyncVaults vaults;
  private final AsyncOAuth oauth;
  private final AsyncMemoryStores memoryStores;
  private final AsyncModels models;
  private final AsyncDreams dreams;

  AsyncManagedClient(ClientEngine engine, boolean ownsEngine, ManagedClient.Builder configuration) {
    this.engine = engine;
    this.configuration = configuration;
    this.ownsEngine = ownsEngine;
    agents = new AsyncAgents(engine);
    deployments = new AsyncDeployments(engine);
    deploymentRuns = new AsyncDeploymentRuns(engine);
    sessions = new AsyncSessions(engine);
    environments = new AsyncEnvironments(engine);
    files = new AsyncFiles(engine);
    skills = new AsyncSkills(engine);
    vaults = new AsyncVaults(engine);
    oauth = new AsyncOAuth(engine);
    memoryStores = new AsyncMemoryStores(engine);
    models = new AsyncModels(engine);
    dreams = new AsyncDreams(engine);
  }

  public AsyncAgents agents() {
    return agents;
  }

  public AsyncDeployments deployments() {
    return deployments;
  }

  public AsyncDeploymentRuns deploymentRuns() {
    return deploymentRuns;
  }

  public AsyncSessions sessions() {
    return sessions;
  }

  public AsyncEnvironments environments() {
    return environments;
  }

  public AsyncFiles files() {
    return files;
  }

  public AsyncSkills skills() {
    return skills;
  }

  public AsyncVaults vaults() {
    return vaults;
  }

  public AsyncOAuth oauth() {
    return oauth;
  }

  public AsyncMemoryStores memoryStores() {
    return memoryStores;
  }

  public AsyncModels models() {
    return models;
  }

  public AsyncDreams dreams() {
    return dreams;
  }

  public static ManagedClient.Builder builder() {
    return ManagedClient.builder();
  }

  /** Creates an independently owned async client; the original client is unchanged. */
  public AsyncManagedClient withOptions(Consumer<ManagedClient.Builder> configure) {
    if (configure == null)
      throw new ConfigurationException("Configuration callback must not be null");
    ManagedClient.Builder copy = configuration.copy();
    configure.accept(copy);
    return copy.buildAsync();
  }

  public CompletableFuture<ApiResponse<JsonValue>> request(ApiRequest request) {
    return request(request, RequestOptions.DEFAULT);
  }

  /** The returned future directly owns cancellation of the underlying operation. */
  public CompletableFuture<ApiResponse<JsonValue>> request(
      ApiRequest request, RequestOptions options) {
    if (request == null || options == null)
      throw new ConfigurationException("Request and options must not be null");
    return engine.request(
        request.method(), request.path(), Collections.emptyMap(), request.body(), true, options);
  }

  @Override
  public void close() {
    if (ownsEngine) engine.close();
  }
}
