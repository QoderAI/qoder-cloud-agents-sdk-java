package com.qoder.cloudagents;

import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.auth.Credential;
import com.qoder.cloudagents.core.ApiRequest;
import com.qoder.cloudagents.core.ApiResponse;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.core.http.HttpInterceptor;
import com.qoder.cloudagents.core.http.HttpTransport;
import com.qoder.cloudagents.core.internal.Authentication;
import com.qoder.cloudagents.core.internal.ClientEngine;
import com.qoder.cloudagents.core.internal.Futures;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
  private final Builder configuration;
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

  private ForwardClient(ClientEngine engine, Builder configuration) {
    this.configuration = configuration;
    this.engine = engine;
    realtime = new Realtime(engine);
    async = new AsyncForwardClient(engine, false, configuration);
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

  /** Creates an independently closeable client, retaining unspecified resolved options. */
  public ForwardClient withOptions(Consumer<Builder> configure) {
    Builder copy = configuration.copy();
    Builder.required(configure).accept(copy);
    return copy.build();
  }

  /** Calls an additional JSON API using the client's authentication, retries and error handling. */
  public ApiResponse<JsonValue> request(ApiRequest request) {
    return request(request, RequestOptions.DEFAULT);
  }

  public ApiResponse<JsonValue> request(ApiRequest request, RequestOptions options) {
    return Futures.await(async.request(request, options));
  }

  @Override
  public void close() {
    engine.close();
  }

  public static final class Builder {
    private String baseUrl, pat, accessToken;
    private AuthMode authMode;
    private Credential credential;
    private Credential resolvedCredential;
    private HttpTransport transport;
    private final List<HttpInterceptor> interceptors = new ArrayList<>();
    private boolean derived, modeChanged;
    private Duration timeout = Duration.ofMinutes(10);
    private int maxRetries = 2;
    private final Map<String, String> headers = new LinkedHashMap<>();

    public Builder baseUrl(String value) {
      baseUrl = required(value);
      return this;
    }

    public Builder authMode(AuthMode value) {
      authMode = required(value);
      modeChanged = true;
      return this;
    }

    public Builder pat(String value) {
      if (derived) {
        credential = null;
        accessToken = null;
        if (!modeChanged) authMode = AuthMode.PAT;
      }
      pat = Authentication.validateToken(value);
      return this;
    }

    public Builder accessToken(String value) {
      if (derived) {
        credential = null;
        pat = null;
      }
      accessToken = Authentication.validateToken(value);
      return this;
    }

    public Builder credential(Credential value) {
      if (derived) {
        pat = null;
        accessToken = null;
        if (!modeChanged) authMode = null;
      }
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
      headers.keySet().removeIf(key -> key.equalsIgnoreCase(name));
      headers.put(name, value);
      return this;
    }

    /** Caller-owned transport, also used for isolated storage downloads; not WebSockets. */
    public Builder transport(HttpTransport value) {
      transport = required(value);
      return this;
    }

    /** Appends an API interceptor; storage downloads do not invoke it. */
    public Builder interceptor(HttpInterceptor value) {
      interceptors.add(required(value));
      return this;
    }

    public Builder clearInterceptors() {
      interceptors.clear();
      return this;
    }

    public Builder clearDefaultHeaders() {
      headers.clear();
      return this;
    }

    Builder copy() {
      Builder copy = new Builder();
      copy.baseUrl = baseUrl;
      copy.pat = pat;
      copy.accessToken = accessToken;
      copy.authMode = authMode;
      copy.credential = credential;
      copy.timeout = timeout;
      copy.maxRetries = maxRetries;
      copy.transport = transport;
      copy.headers.putAll(headers);
      copy.interceptors.addAll(interceptors);
      copy.derived = derived;
      return copy;
    }

    private Builder resolve() {
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
      Builder snapshot = copy();
      snapshot.baseUrl = url.toString();
      snapshot.authMode =
          authMode != null
              ? authMode
              : credential instanceof com.qoder.cloudagents.auth.ServiceAccountCredential
                  ? AuthMode.SA
                  : AuthMode.PAT;
      snapshot.resolvedCredential = resolved;
      if (pat == null && accessToken == null) snapshot.credential = resolved;
      snapshot.derived = true;
      return snapshot;
    }

    private ClientEngine engine() {
      return new ClientEngine(
          HttpUrl.get(baseUrl),
          resolvedCredential,
          timeout,
          maxRetries,
          headers,
          transport,
          interceptors);
    }

    public ForwardClient build() {
      Builder snapshot = resolve();
      return new ForwardClient(snapshot.engine(), snapshot);
    }

    /** Builds an independently owned async client with the same configuration options. */
    public AsyncForwardClient buildAsync() {
      Builder snapshot = resolve();
      return new AsyncForwardClient(snapshot.engine(), true, snapshot);
    }
  }
}
