package com.qoder.cloudagents.core.internal;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import java.time.*;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Headers;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class RetryPolicyTest {
  @Test
  void safetyGatePrecedesServerInstruction() {
    Headers force = new Headers.Builder().add("x-should-retry", "true").build();
    assertFalse(RetryPolicy.shouldRetry("POST", false, true, 503, force));
    assertFalse(RetryPolicy.shouldRetry("GET", false, true, 409, force));
    assertFalse(RetryPolicy.shouldRetry("POST", true, false, 429, force));
    assertTrue(RetryPolicy.shouldRetry("POST", false, true, 429, force));
    assertTrue(RetryPolicy.shouldRetry("POST", true, true, 503, force));
    assertTrue(RetryPolicy.shouldRetry("GET", false, true, null, force));
    assertFalse(RetryPolicy.shouldRetry("POST", false, true, null, force));
    assertFalse(
        RetryPolicy.shouldRetry("GET", false, true, 500, Headers.of("x-should-retry", "false")));
  }

  @Test
  void retryAfterBoundariesAndHttpDate() {
    Clock clock = Clock.fixed(Instant.parse("2026-10-16T00:00:00Z"), ZoneOffset.UTC);
    assertEquals(120000, RetryPolicy.delayMillis(Headers.of("Retry-After", "120"), 0, clock, 0));
    assertEquals(1, RetryPolicy.delayMillis(Headers.of("retry-after-ms", "0.5"), 0, clock, 0));
    assertEquals(
        Integer.MAX_VALUE,
        RetryPolicy.delayMillis(Headers.of("retry-after-ms", "2147483647"), 0, clock, 0));
    for (String value : new String[] {"0", "-1", "NaN", "Infinity", "2147483648", "invalid"})
      assertEquals(500, RetryPolicy.delayMillis(Headers.of("retry-after-ms", value), 0, clock, 0));
    assertEquals(
        120000,
        RetryPolicy.delayMillis(
            Headers.of("Retry-After", "Fri, 16 Oct 2026 00:02:00 GMT"), 0, clock, 0));
    assertEquals(6000, RetryPolicy.delayMillis(new Headers.Builder().build(), 100, clock, 1));
  }

  ClientEngine engine(MockWebServer server, int retries, AtomicInteger tokens) {
    return new ClientEngine(
        server.url("/"),
        () -> CompletableFuture.completedFuture("token-" + tokens.incrementAndGet()),
        Duration.ofSeconds(2),
        retries,
        Collections.emptyMap());
  }

  CompletableFuture<ApiResponse<JsonValue>> request(
      ClientEngine engine, String method, boolean replayable, RequestOptions options) {
    return engine.request(
        method,
        new String[] {"resource"},
        Collections.emptyMap(),
        method.equals("POST") ? JsonValue.parse("{\"value\":1}") : null,
        replayable,
        options);
  }

  @Test
  void okhttpCannotAddHidden503RetriesEvenWithRetryAfterZero() throws Exception {
    for (String method : new String[] {"GET", "POST"}) {
      try (MockWebServer server = new MockWebServer()) {
        server.start();
        server.enqueue(
            new MockResponse().setResponseCode(503).setHeader("Retry-After", "0").setBody("{}"));
        server.enqueue(new MockResponse().setBody("{}"));
        try (ClientEngine engine =
            engine(server, method.equals("GET") ? 0 : 2, new AtomicInteger())) {
          ExecutionException error =
              assertThrows(
                  ExecutionException.class,
                  () -> request(engine, method, true, RequestOptions.DEFAULT).get());
          assertEquals(503, ((ApiStatusException) error.getCause()).statusCode());
          assertEquals(1, server.getRequestCount());
        }
      }
    }
  }

  @Test
  void safeRetryRefreshesCredentialsAndPreservesBodyAndIdempotencyKey() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(
          new MockResponse().setResponseCode(503).setHeader("retry-after-ms", "1").setBody("{}"));
      server.enqueue(new MockResponse().setBody("{\"ok\":true}"));
      AtomicInteger tokens = new AtomicInteger();
      try (ClientEngine engine = engine(server, 1, tokens)) {
        assertTrue(
            request(engine, "POST", true, RequestOptions.builder().idempotencyKey("once").build())
                .get()
                .data()
                .get("ok")
                .asBoolean());
        RecordedRequest first = server.takeRequest(), second = server.takeRequest();
        assertEquals(first.getBody().readUtf8(), second.getBody().readUtf8());
        assertEquals("once", second.getHeader("Idempotency-Key"));
        assertEquals("Bearer token-1", first.getHeader("Authorization"));
        assertEquals("Bearer token-2", second.getHeader("Authorization"));
        assertEquals("1", second.getHeader("X-Qoder-Retry-Count"));
        assertEquals(2, tokens.get());
      }
    }
  }

  @Test
  void retryBudgetBoundsAttemptsAndDoesNotRetry409() throws Exception {
    for (int status : new int[] {429, 409}) {
      try (MockWebServer server = new MockWebServer()) {
        server.start();
        for (int i = 0; i < 3; i++)
          server.enqueue(
              new MockResponse()
                  .setResponseCode(status)
                  .setHeader("retry-after-ms", "1")
                  .setHeader("x-should-retry", "true")
                  .setBody("{}"));
        try (ClientEngine engine = engine(server, 2, new AtomicInteger())) {
          assertThrows(
              ExecutionException.class,
              () -> request(engine, "POST", true, RequestOptions.DEFAULT).get());
          assertEquals(status == 429 ? 3 : 1, server.getRequestCount());
        }
      }
    }
  }

  @Test
  void cancellationDuringBackoffStopsRetry() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(
          new MockResponse().setResponseCode(429).setHeader("retry-after-ms", "300").setBody("{}"));
      server.enqueue(new MockResponse().setBody("{}"));
      try (ClientEngine engine = engine(server, 2, new AtomicInteger())) {
        CompletableFuture<?> future = request(engine, "GET", true, RequestOptions.DEFAULT);
        server.takeRequest();
        future.cancel(true);
        assertNull(server.takeRequest(700, TimeUnit.MILLISECONDS));
      }
    }
  }

  @Test
  void failedBodyReadRetriesSafeGet() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(
          new MockResponse()
              .setBody("{\"value\":123456789}")
              .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY));
      server.enqueue(new MockResponse().setBody("{\"ok\":true}"));
      try (ClientEngine engine = engine(server, 1, new AtomicInteger())) {
        assertTrue(
            request(engine, "GET", true, RequestOptions.DEFAULT)
                .get()
                .data()
                .get("ok")
                .asBoolean());
        assertEquals(2, server.getRequestCount());
      }
    }
  }
}
