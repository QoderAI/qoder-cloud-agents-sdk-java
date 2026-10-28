package com.qoder.cloudagents;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.ClientEngine;
import com.qoder.cloudagents.errors.ConfigurationException;
import com.qoder.cloudagents.forward.AsyncBatches;
import com.qoder.cloudagents.forward.AsyncChannelPairings;
import com.qoder.cloudagents.forward.AsyncChannels;
import com.qoder.cloudagents.forward.AsyncDecisions;
import com.qoder.cloudagents.forward.AsyncDreams;
import com.qoder.cloudagents.forward.AsyncDrives;
import com.qoder.cloudagents.forward.AsyncEnvironments;
import com.qoder.cloudagents.forward.AsyncFiles;
import com.qoder.cloudagents.forward.AsyncIdentities;
import com.qoder.cloudagents.forward.AsyncMemoryStores;
import com.qoder.cloudagents.forward.AsyncModels;
import com.qoder.cloudagents.forward.AsyncScheduleRuns;
import com.qoder.cloudagents.forward.AsyncSchedules;
import com.qoder.cloudagents.forward.AsyncServiceAccountTokens;
import com.qoder.cloudagents.forward.AsyncSessions;
import com.qoder.cloudagents.forward.AsyncSkills;
import com.qoder.cloudagents.forward.AsyncTemplates;
import com.qoder.cloudagents.forward.AsyncUsage;
import com.qoder.cloudagents.forward.AsyncVaults;
import com.qoder.cloudagents.forward.AsyncWebhooks;
import com.qoder.cloudagents.forward.realtime.AsyncRealtime;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** Native asynchronous Forward client. Build with ForwardClient.builder().buildAsync(). */
public final class AsyncForwardClient implements AutoCloseable {
  private final AsyncDecisions decisions;
  private final AsyncDreams dreams;
  private final AsyncWebhooks webhooks;
  private final AsyncUsage usage;
  private final AsyncServiceAccountTokens serviceAccountTokens;
  private final AsyncDrives drives;
  private final AsyncRealtime realtime;
  private final ClientEngine engine;
  private final ForwardClient.Builder configuration;
  private final boolean ownsEngine;
  private final AsyncModels models;
  private final AsyncFiles files;
  private final AsyncChannelPairings channelPairings;
  private final AsyncChannels channels;
  private final AsyncBatches batches;
  private final AsyncScheduleRuns scheduleRuns;
  private final AsyncSchedules schedules;
  private final AsyncVaults vaults;
  private final AsyncSkills skills;
  private final AsyncEnvironments environments;
  private final AsyncTemplates templates;
  private final AsyncIdentities identities;
  private final AsyncSessions sessions;
  private final AsyncMemoryStores memoryStores;

  AsyncForwardClient(ClientEngine engine, boolean ownsEngine, ForwardClient.Builder configuration) {
    this.configuration = configuration;
    this.engine = engine;
    realtime = new AsyncRealtime(engine);
    drives = new AsyncDrives(engine);
    serviceAccountTokens = new AsyncServiceAccountTokens(engine);
    usage = new AsyncUsage(engine);
    webhooks = new AsyncWebhooks(engine);
    this.ownsEngine = ownsEngine;
    dreams = new AsyncDreams(engine);
    decisions = new AsyncDecisions(engine);
    models = new AsyncModels(engine);
    files = new AsyncFiles(engine);
    channelPairings = new AsyncChannelPairings(engine);
    channels = new AsyncChannels(engine);
    batches = new AsyncBatches(engine);
    scheduleRuns = new AsyncScheduleRuns(engine);
    schedules = new AsyncSchedules(engine);
    vaults = new AsyncVaults(engine);
    skills = new AsyncSkills(engine);
    environments = new AsyncEnvironments(engine);
    templates = new AsyncTemplates(engine);
    identities = new AsyncIdentities(engine);
    sessions = new AsyncSessions(engine);
    memoryStores = new AsyncMemoryStores(engine);
  }

  public AsyncDecisions decisions() {
    return decisions;
  }

  public AsyncDreams dreams() {
    return dreams;
  }

  public AsyncWebhooks webhooks() {
    return webhooks;
  }

  public AsyncRealtime realtime() {
    return realtime;
  }

  public AsyncUsage usage() {
    return usage;
  }

  public AsyncServiceAccountTokens serviceAccountTokens() {
    return serviceAccountTokens;
  }

  public AsyncDrives drives() {
    return drives;
  }

  public AsyncModels models() {
    return models;
  }

  public AsyncTemplates templates() {
    return templates;
  }

  public AsyncIdentities identities() {
    return identities;
  }

  public AsyncSessions sessions() {
    return sessions;
  }

  public AsyncMemoryStores memoryStores() {
    return memoryStores;
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

  public AsyncSchedules schedules() {
    return schedules;
  }

  public AsyncScheduleRuns scheduleRuns() {
    return scheduleRuns;
  }

  public AsyncBatches batches() {
    return batches;
  }

  public AsyncChannels channels() {
    return channels;
  }

  public AsyncChannelPairings channelPairings() {
    return channelPairings;
  }

  /** Creates an independently owned async client; the original client is unchanged. */
  public AsyncForwardClient withOptions(Consumer<ForwardClient.Builder> configure) {
    if (configure == null)
      throw new ConfigurationException("Configuration callback must not be null");
    ForwardClient.Builder copy = configuration.copy();
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
