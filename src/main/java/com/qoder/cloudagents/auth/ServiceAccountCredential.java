package com.qoder.cloudagents.auth;

import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.core.internal.Authentication;
import com.qoder.cloudagents.core.internal.Futures;
import com.qoder.cloudagents.core.internal.RetryPolicy;
import com.qoder.cloudagents.core.internal.Transports;
import com.qoder.cloudagents.errors.*;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import okhttp3.*;

/**
 * Explicit, lazy SA Key exchange. Share the instance, and close it after all clients stop using it.
 */
public final class ServiceAccountCredential implements Credential, AutoCloseable {
  private final String key, scope;
  private final Region region;
  private final int ttl;
  private final HttpUrl endpoint;
  private final OkHttpClient transport;
  private final LongSupplier nanoTime;
  private final Clock clock;
  private final boolean ownsTransport;
  private CompletableFuture<String> inflight;
  private Call call;
  private String token;
  private long refreshAt, safeUntil, retryAt;
  private RuntimeException failure;
  private boolean transientFailure, closed;

  private ServiceAccountCredential(Builder builder) {
    this(
        builder,
        Transports.create(),
        HttpUrl.get(builder.region.exchangeUrl()),
        System::nanoTime,
        Clock.systemUTC(),
        true);
  }

  // Test seam stays package-private: a production SA Key cannot be redirected by a public endpoint
  // option.
  ServiceAccountCredential(
      Builder builder,
      OkHttpClient transport,
      HttpUrl endpoint,
      LongSupplier nanoTime,
      Clock clock,
      boolean ownsTransport) {
    builder.validate();
    key = builder.saKey;
    region = builder.region;
    ttl = builder.ttl;
    scope = String.join(" ", builder.scopes);
    this.endpoint = endpoint;
    this.transport =
        transport
            .newBuilder()
            .callTimeout(RequestOptions.positiveMillis(builder.timeout), TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false)
            .followRedirects(false)
            .followSslRedirects(false)
            .authenticator(Authenticator.NONE)
            .proxyAuthenticator(Authenticator.NONE)
            .build();
    this.nanoTime = nanoTime;
    this.clock = clock;
    this.ownsTransport = ownsTransport;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Region region() {
    return region;
  }

  public void validateRegion(String host) {
    if ((host.equals("api.qoder.com.cn") && region != Region.CN)
        || (host.equals("api.qoder.com") && region != Region.GLOBAL))
      throw new ConfigurationException("SA credential region does not match baseUrl");
  }

  @Override
  public synchronized CompletableFuture<String> getToken() {
    if (closed) return Futures.failed(new ConfigurationException("Credential is closed"));
    long now = nanoTime.getAsLong();
    if (failure != null && now < retryAt) return fallback(now);
    if (token != null && now < refreshAt) return CompletableFuture.completedFuture(token);
    if (inflight == null) {
      CompletableFuture<String> shared = new CompletableFuture<>();
      inflight = shared;
      try {
        exchange(shared, now);
      } catch (RuntimeException error) {
        fail(shared, new ApiException("SA exchange could not start"), true, 5000);
      }
      return shared.thenApply(value -> value);
    }
    // Each caller receives a detached view. Cancelling it must not abort a shared refresh.
    return inflight.thenApply(value -> value);
  }

  private CompletableFuture<String> fallback(long now) {
    return transientFailure && token != null && now < safeUntil
        ? CompletableFuture.completedFuture(token)
        : Futures.failed(failure);
  }

  private void exchange(CompletableFuture<String> shared, long started) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("grant_type", "client_credentials");
    payload.put("audience", "qoder");
    payload.put("scope", scope);
    payload.put("ttl_seconds", ttl);
    RequestBody json =
        RequestBody.create(JsonValue.of(payload).toJson(), MediaType.get("application/json"));
    RequestBody once =
        new RequestBody() {
          @Override
          public MediaType contentType() {
            return json.contentType();
          }

          @Override
          public long contentLength() throws IOException {
            return json.contentLength();
          }

          @Override
          public boolean isOneShot() {
            return true;
          }

          @Override
          public void writeTo(okio.BufferedSink sink) throws IOException {
            json.writeTo(sink);
          }
        };
    call =
        transport.newCall(
            new Request.Builder()
                .url(endpoint)
                .post(once)
                .header("Authorization", "Bearer " + key)
                .header("Accept", "application/json")
                .build());
    call.enqueue(
        new Callback() {
          @Override
          public void onFailure(Call call, IOException error) {
            fail(
                shared,
                error instanceof InterruptedIOException
                    ? new ApiTimeoutException("SA exchange timed out")
                    : new ApiConnectionException("SA exchange connection failed"),
                true,
                5000);
          }

          @Override
          public void onResponse(Call call, Response response) {
            try (Response owned = response) {
              if (!response.isSuccessful()) {
                long cooldown =
                    response.code() == 429
                        ? Math.max(5000, RetryPolicy.delayMillis(response.headers(), 0, clock, 0))
                        : 5000;
                // Never retain an issuer body or cause that could echo the Key or issued token.
                String requestId =
                    safeDiagnostic(response.header("x-request-id", response.header("request-id")));
                fail(
                    shared,
                    new ServiceAccountExchangeException(response.code(), requestId),
                    response.code() == 429 || response.code() >= 500,
                    cooldown);
                return;
              }
              String text = response.body() == null ? "" : response.body().string();
              JsonValue body = JsonValue.parse(text);
              String issued = Authentication.validateToken(body.get("access_token").asString());
              if (!"bearer".equalsIgnoreCase(body.get("token_type").asString()))
                throw new ResponseValidationException("Invalid SA token type");
              JsonValue expires = body.get("expires_in");
              long seconds;
              String ttlText = expires.stringOrNull();
              if (ttlText != null && ttlText.matches("[0-9]+")) seconds = Long.parseLong(ttlText);
              else seconds = expires.asLong();
              if (seconds < 1 || seconds > 43200)
                throw new ResponseValidationException("Invalid SA token lifetime");
              JsonValue expiresAt = body.get("expires_at");
              if (!expiresAt.isMissing()) OffsetDateTime.parse(expiresAt.asString());
              long safe =
                  started + (long) ((seconds - Math.min(5, seconds * .05)) * 1_000_000_000L);
              synchronized (ServiceAccountCredential.this) {
                if (closed) return;
                if (nanoTime.getAsLong() >= safe)
                  throw new ResponseValidationException("Insufficient remaining SA token lifetime");
                token = issued;
                safeUntil = safe;
                refreshAt =
                    started + (long) ((seconds - Math.min(60, seconds * .1)) * 1_000_000_000L);
                failure = null;
                retryAt = 0;
                inflight = null;
                call = null;
              }
              shared.complete(issued);
            } catch (IOException error) {
              fail(
                  shared,
                  error instanceof InterruptedIOException
                      ? new ApiTimeoutException("SA exchange body timed out")
                      : new ApiConnectionException("SA exchange body read failed"),
                  true,
                  5000);
            } catch (RuntimeException error) {
              fail(
                  shared,
                  new ResponseValidationException("Invalid SA exchange response"),
                  false,
                  5000);
            }
          }
        });
  }

  private synchronized String safeDiagnostic(String value) {
    if (value == null) return null;
    value = value.replace(key, "[redacted]");
    if (token != null) value = value.replace(token, "[redacted]");
    return value.substring(0, Math.min(value.length(), 256));
  }

  private void fail(
      CompletableFuture<String> shared,
      RuntimeException error,
      boolean transientError,
      long cooldownMillis) {
    String fallback = null;
    synchronized (this) {
      if (closed) return;
      failure = error;
      transientFailure = transientError;
      retryAt = nanoTime.getAsLong() + TimeUnit.MILLISECONDS.toNanos(cooldownMillis);
      inflight = null;
      call = null;
      if (transientError && token != null && nanoTime.getAsLong() < safeUntil) fallback = token;
    }
    if (fallback != null) shared.complete(fallback);
    else shared.completeExceptionally(error);
  }

  @Override
  public synchronized void close() {
    closed = true;
    token = null;
    if (call != null) call.cancel();
    if (inflight != null)
      inflight.completeExceptionally(new ConfigurationException("Credential is closed"));
    if (ownsTransport) Transports.close(transport);
  }

  @Override
  public String toString() {
    return "ServiceAccountCredential(" + region + ", [redacted])";
  }

  public static final class Builder {
    private String saKey;
    private Region region;
    private LinkedHashSet<String> scopes = new LinkedHashSet<>();
    private int ttl = 3600;
    private Duration timeout = Duration.ofSeconds(30);

    public Builder saKey(String value) {
      saKey = value;
      return this;
    }

    /** Explicitly opt in to reading QODER_SA_KEY. Client builders never read it automatically. */
    public Builder saKeyFromEnv() {
      return saKey(System.getenv("QODER_SA_KEY"));
    }

    public Builder region(Region value) {
      region = value;
      return this;
    }

    public Builder scopes(String... values) {
      if (values == null) throw new ConfigurationException("scopes are required");
      scopes = new LinkedHashSet<>(Arrays.asList(values));
      return this;
    }

    public Builder ttlSeconds(int value) {
      ttl = value;
      return this;
    }

    public Builder timeout(Duration value) {
      RequestOptions.positiveMillis(value);
      timeout = value;
      return this;
    }

    private void validate() {
      Authentication.validateToken(saKey);
      if (region == null) throw new ConfigurationException("SA region is required");
      if (ttl < 1 || ttl > 43200)
        throw new ConfigurationException("ttlSeconds must be from 1 to 43200");
      if (scopes.isEmpty()) throw new ConfigurationException("scopes are required");
      for (String scope : scopes) {
        if (scope == null || !scope.matches("[^\\s\\p{Cntrl}]+"))
          throw new ConfigurationException("Invalid scope");
      }
    }

    public ServiceAccountCredential build() {
      validate();
      return new ServiceAccountCredential(this);
    }
  }
}
