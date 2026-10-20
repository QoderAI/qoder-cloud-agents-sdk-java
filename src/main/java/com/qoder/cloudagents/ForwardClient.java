package com.qoder.cloudagents;

import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.auth.Credential;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.core.internal.Authentication;
import com.qoder.cloudagents.core.internal.ClientEngine;
import com.qoder.cloudagents.errors.ConfigurationException;
import com.qoder.cloudagents.forward.Batches;
import com.qoder.cloudagents.forward.ChannelPairings;
import com.qoder.cloudagents.forward.Channels;
import com.qoder.cloudagents.forward.Drives;
import com.qoder.cloudagents.forward.Environments;
import com.qoder.cloudagents.forward.Files;
import com.qoder.cloudagents.forward.Identities;
import com.qoder.cloudagents.forward.MemoryStores;
import com.qoder.cloudagents.forward.Models;
import com.qoder.cloudagents.forward.ScheduleRuns;
import com.qoder.cloudagents.forward.Schedules;
import com.qoder.cloudagents.forward.ServiceAccountTokens;
import com.qoder.cloudagents.forward.Sessions;
import com.qoder.cloudagents.forward.Skills;
import com.qoder.cloudagents.forward.Templates;
import com.qoder.cloudagents.forward.Usage;
import com.qoder.cloudagents.forward.Vaults;
import com.qoder.cloudagents.forward.realtime.Realtime;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import okhttp3.HttpUrl;

/**
 * Thread-safe Forward client. Close it after its synchronous and asynchronous views are finished.
 */
public final class ForwardClient implements AutoCloseable {
  private final Usage usage;
  private final ServiceAccountTokens serviceAccountTokens;
  private final Drives drives;
  private final Realtime realtime;
  private final ClientEngine engine;
  private final Models models;
  private final Files files;
  private final ChannelPairings channelPairings;
  private final Channels channels;
  private final Batches batches;
  private final ScheduleRuns scheduleRuns;
  private final Schedules schedules;
  private final Vaults vaults;
  private final Skills skills;
  private final Environments environments;
  private final Templates templates;
  private final Identities identities;
  private final Sessions sessions;
  private final MemoryStores memoryStores;
  private final AsyncForwardClient async;

  private ForwardClient(ClientEngine engine) {
    this.engine = engine;
    realtime = new Realtime(engine);
    async = new AsyncForwardClient(engine, false);
    models = new Models(async.models());
    drives = new Drives(async.drives());
    serviceAccountTokens = new ServiceAccountTokens(async.serviceAccountTokens());
    usage = new Usage(async.usage());
    files = new Files(async.files());
    channelPairings = new ChannelPairings(async.channelPairings());
    channels = new Channels(async.channels());
    batches = new Batches(async.batches());
    scheduleRuns = new ScheduleRuns(async.scheduleRuns());
    schedules = new Schedules(async.schedules());
    vaults = new Vaults(async.vaults());
    skills = new Skills(async.skills());
    environments = new Environments(async.environments());
    templates = new Templates(async.templates());
    identities = new Identities(async.identities());
    sessions = new Sessions(async.sessions());
    memoryStores = new MemoryStores(async.memoryStores());
  }

  public Realtime realtime() {
    return realtime;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Usage usage() {
    return usage;
  }

  public ServiceAccountTokens serviceAccountTokens() {
    return serviceAccountTokens;
  }

  public Drives drives() {
    return drives;
  }

  public Models models() {
    return models;
  }

  public Templates templates() {
    return templates;
  }

  public Identities identities() {
    return identities;
  }

  public Sessions sessions() {
    return sessions;
  }

  public MemoryStores memoryStores() {
    return memoryStores;
  }

  /**
   * A non-owning view. Closing the parent closes its in-flight requests; closing this view is a
   * no-op.
   */
  public AsyncForwardClient async() {
    return async;
  }

  public Environments environments() {
    return environments;
  }

  public Files files() {
    return files;
  }

  public Skills skills() {
    return skills;
  }

  public Vaults vaults() {
    return vaults;
  }

  public Schedules schedules() {
    return schedules;
  }

  public ScheduleRuns scheduleRuns() {
    return scheduleRuns;
  }

  public Batches batches() {
    return batches;
  }

  public Channels channels() {
    return channels;
  }

  public ChannelPairings channelPairings() {
    return channelPairings;
  }

  @Override
  public void close() {
    engine.close();
  }

  public static final class Builder {
    private String baseUrl, pat, accessToken;
    private AuthMode authMode;
    private Credential credential;
    private Duration timeout = Duration.ofMinutes(10);
    private int maxRetries = 2;
    private final Map<String, String> headers = new LinkedHashMap<>();

    public Builder baseUrl(String value) {
      baseUrl = required(value);
      return this;
    }

    public Builder authMode(AuthMode value) {
      authMode = required(value);
      return this;
    }

    public Builder pat(String value) {
      pat = Authentication.validateToken(value);
      return this;
    }

    public Builder accessToken(String value) {
      accessToken = Authentication.validateToken(value);
      return this;
    }

    public Builder credential(Credential value) {
      credential = required(value);
      return this;
    }

    private static <T> T required(T value) {
      if (value == null)
        throw new ConfigurationException("Explicit client options must be non-null");
      return value;
    }

    public Builder timeout(Duration value) {
      RequestOptions.positiveMillis(value);
      timeout = value;
      return this;
    }

    public Builder maxRetries(int value) {
      if (value < 0) throw new ConfigurationException("maxRetries must not be negative");
      maxRetries = value;
      return this;
    }

    public Builder defaultHeader(String name, String value) {
      RequestOptions.builder().header(name, value);
      headers.put(name, value);
      return this;
    }

    private ClientEngine engine() {
      String value = baseUrl;
      if (value == null) value = System.getenv("QODER_FORWARD_BASE_URL");
      if (value == null || value.isEmpty()) value = "https://api.qoder.com/api/v1/forward";
      HttpUrl url;
      try {
        url = HttpUrl.get(value);
      } catch (IllegalArgumentException error) {
        throw new ConfigurationException("Invalid Forward baseUrl");
      }
      if (!url.username().isEmpty()
          || !url.password().isEmpty()
          || url.query() != null
          || url.fragment() != null)
        throw new ConfigurationException("baseUrl cannot contain credentials, query or fragment");
      Credential resolved =
          Authentication.resolve(
              authMode, pat, accessToken, credential, url.host(), System::getenv);
      return new ClientEngine(url, resolved, timeout, maxRetries, headers);
    }

    public ForwardClient build() {
      return new ForwardClient(engine());
    }

    /** Builds an independently owned async client with the same configuration options. */
    public AsyncForwardClient buildAsync() {
      return new AsyncForwardClient(engine(), true);
    }
  }
}
