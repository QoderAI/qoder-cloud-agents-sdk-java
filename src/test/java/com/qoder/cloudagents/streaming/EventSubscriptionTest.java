package com.qoder.cloudagents.streaming;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;

@Timeout(5)
class EventSubscriptionTest {
  @Test
  void closeKeepsCancellationWhenReaderFinishesDuringCleanup() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    CountDownLatch closing = new CountDownLatch(1);
    AtomicReference<EventSubscription> reference = new AtomicReference<>();
    Future<?> reader =
        executor.submit(
            () -> {
              assertTrue(closing.await(2, TimeUnit.SECONDS));
              reference.get().completion().complete(null);
              return null;
            });
    EventSubscription subscription =
        new EventSubscription(
            () -> {
              closing.countDown();
              try {
                reader.get(2, TimeUnit.SECONDS);
              } catch (Exception error) {
                throw new AssertionError(error);
              }
            });
    reference.set(subscription);
    try {
      subscription.close();
      assertTrue(subscription.completion().isCancelled());
      assertThrows(CancellationException.class, () -> subscription.completion().join());
    } finally {
      closing.countDown();
      executor.shutdownNow();
    }
  }

  @Test
  void closePreservesAnAlreadyCompletedOutcomeAndStillReleasesResources() {
    for (boolean failed : new boolean[] {false, true}) {
      CountDownLatch released = new CountDownLatch(1);
      EventSubscription subscription = new EventSubscription(released::countDown);
      RuntimeException error = new IllegalStateException("reader failed");
      if (failed) subscription.completion().completeExceptionally(error);
      else subscription.completion().complete(null);
      subscription.close();
      assertFalse(subscription.completion().isCancelled());
      assertEquals(0, released.getCount());
      if (failed)
        assertSame(
            error,
            assertThrows(CompletionException.class, () -> subscription.completion().join())
                .getCause());
      else assertNull(subscription.completion().join());
    }
  }
}
