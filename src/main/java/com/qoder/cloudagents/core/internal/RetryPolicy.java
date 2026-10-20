package com.qoder.cloudagents.core.internal;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import okhttp3.Headers;

public final class RetryPolicy {
  // Cross-SDK policy limit, not a Java timer limitation.
  public static final long MAX_DELAY_MS = Integer.MAX_VALUE;

  private RetryPolicy() {}

  public static boolean shouldRetry(
      String method, boolean idempotent, boolean replayable, Integer status, Headers headers) {
    if (!replayable) return false;
    boolean safe = method.equals("GET") || method.equals("HEAD") || idempotent;
    if (status == null) return safe;
    if (status == 409 || (!safe && status != 429)) return false;
    if ("false".equals(headers.get("x-should-retry"))) return false;
    if ("true".equals(headers.get("x-should-retry"))) return true;
    return status == 408 || status == 429 || status >= 500;
  }

  public static long delayMillis(Headers headers, int attempt, Clock clock, double random) {
    Long delay = retryAfterMillis(headers.get("retry-after-ms"), headers.get("retry-after"), clock);
    if (delay != null) return delay;
    return (long) (Math.min(500 * Math.pow(2, Math.min(attempt, 5)), 8000) * (1 - random * .25));
  }

  public static Long retryAfterMillis(String retryAfterMs, String retryAfter, Clock clock) {
    Double delay = number(retryAfterMs);
    if (delay == null || delay == 0) {
      Double seconds = number(retryAfter);
      if (seconds != null) delay = seconds * 1000;
      else if (retryAfter != null) {
        try {
          delay =
              (double)
                  (ZonedDateTime.parse(retryAfter, DateTimeFormatter.RFC_1123_DATE_TIME)
                          .toInstant()
                          .toEpochMilli()
                      - clock.millis());
        } catch (RuntimeException ignored) {
          delay = null;
        }
      }
    }
    if (delay != null && Double.isFinite(delay) && delay > 0 && delay <= MAX_DELAY_MS)
      return Math.max(1, (long) Math.ceil(delay));
    return null;
  }

  private static Double number(String value) {
    if (value == null || value.trim().isEmpty()) return null;
    try {
      return Double.valueOf(value);
    } catch (NumberFormatException ignored) {
      return null;
    }
  }
}
