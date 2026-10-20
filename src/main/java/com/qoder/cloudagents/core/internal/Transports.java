package com.qoder.cloudagents.core.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Authenticator;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/** Owned transport factory. Authentication and retries belong to the SDK, not the HTTP library. */
public final class Transports {
  private Transports() {}

  public static ThreadFactory threads(String name) {
    AtomicInteger sequence = new AtomicInteger();
    return action -> {
      Thread thread = new Thread(action, name + "-" + sequence.incrementAndGet());
      thread.setDaemon(true);
      return thread;
    };
  }

  public static OkHttpClient create() {
    return new OkHttpClient.Builder()
        .dispatcher(new Dispatcher(Executors.newCachedThreadPool(threads("qca-http"))))
        .retryOnConnectionFailure(false)
        .followRedirects(false)
        .followSslRedirects(false)
        .authenticator(Authenticator.NONE)
        .proxyAuthenticator(Authenticator.NONE)
        .addInterceptor(FollowUpGuard.APPLICATION)
        .addNetworkInterceptor(FollowUpGuard.NETWORK)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(600, TimeUnit.SECONDS)
        .writeTimeout(600, TimeUnit.SECONDS)
        .build();
  }

  public static void close(OkHttpClient client) {
    client.dispatcher().cancelAll();
    client.connectionPool().evictAll();
    client.dispatcher().executorService().shutdownNow();
  }
}
