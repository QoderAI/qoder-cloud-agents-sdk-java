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
import com.qoder.cloudagents.managed.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import okhttp3.HttpUrl;

/** Thread-safe Managed API client. Close owned clients after use. */
public final class ManagedClient implements AutoCloseable {
  private final ClientEngine engine;
  private final ManagedClient.Builder configuration;
  private final AsyncManagedClient async;
  private final Agents agents;
  private final Deployments deployments;
  private final DeploymentRuns deploymentRuns;
  private final Sessions sessions;
  private final Environments environments;
  private final Files files;
  private final Skills skills;
  private final Vaults vaults;
  private final OAuth oauth;
  private final MemoryStores memoryStores;
  private final Models models;
  private final Dreams dreams;

  private ManagedClient(ClientEngine engine, ManagedClient.Builder configuration) {
    this.engine = engine;
    this.configuration = configuration;
    async = new AsyncManagedClient(engine, false, configuration);
    agents = new Agents(async.agents());
    deployments = new Deployments(async.deployments());
    deploymentRuns = new DeploymentRuns(async.deploymentRuns());
    sessions = new Sessions(async.sessions());
    environments = new Environments(async.environments());
    files = new Files(async.files());
    skills = new Skills(async.skills());
    vaults = new Vaults(async.vaults());
    oauth = new OAuth(async.oauth());
    memoryStores = new MemoryStores(async.memoryStores());
    models = new Models(async.models());
    dreams = new Dreams(async.dreams());
  }

  public Agents agents() {
    return agents;
  }

  public Deployments deployments() {
    return deployments;
  }

  public DeploymentRuns deploymentRuns() {
    return deploymentRuns;
  }

  public Sessions sessions() {
    return sessions;
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

  public OAuth oauth() {
    return oauth;
  }

  public MemoryStores memoryStores() {
    return memoryStores;
  }

  public Models models() {
    return models;
  }

  public Dreams dreams() {
    return dreams;
  }

  public AsyncManagedClient async() {
    return async;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Creates an independently closeable client, retaining unspecified resolved options. */
  public ManagedClient withOptions(Consumer<Builder> configure) {
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
      if (value == null) value = System.getenv("QODER_BASE_URL");
      if (value == null || value.isEmpty()) value = "https://api.qoder.com/api/v1/cloud";
      HttpUrl url;
      try {
        url = HttpUrl.get(value);
      } catch (IllegalArgumentException error) {
        throw new ConfigurationException("Invalid Managed baseUrl");
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

    public ManagedClient build() {
      Builder snapshot = resolve();
      return new ManagedClient(snapshot.engine(), snapshot);
    }

    /** Builds an independently owned async client with the same configuration options. */
    public AsyncManagedClient buildAsync() {
      Builder snapshot = resolve();
      return new AsyncManagedClient(snapshot.engine(), true, snapshot);
    }
  }
}
