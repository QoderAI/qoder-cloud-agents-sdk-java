package com.qoder.cloudagents.forward.realtime;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.Futures;
import com.qoder.cloudagents.errors.PaginationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/** Each page preserves server order. Continuation loads older history, without reversing events. */
public final class RealtimeHistoryPage extends JsonModel implements Iterable<RealtimeHistoryEvent> {
  private final List<RealtimeHistoryEvent> events;
  private final Set<String> seen;
  private final BiFunction<String, Set<String>, CompletableFuture<RealtimeHistoryPage>> fetch;

  RealtimeHistoryPage(
      JsonValue value,
      String requestId,
      Set<String> seen,
      BiFunction<String, Set<String>, CompletableFuture<RealtimeHistoryPage>> fetch) {
    super(value, requestId);
    events =
        Collections.unmodifiableList(
            field("events").asArray().stream()
                .map(RealtimeHistoryEvent::fromJson)
                .collect(Collectors.toList()));
    this.seen = Collections.unmodifiableSet(new HashSet<>(seen));
    this.fetch = fetch;
    if (hasMore() && (nextBefore() == null || nextBefore().trim().isEmpty()))
      throw new PaginationException("History has_more requires next_before");
  }

  public RealtimeConversationSummary conversation() {
    return optional("conversation", RealtimeConversationSummary::fromJson);
  }

  public List<RealtimeHistoryEvent> events() {
    return events;
  }

  public boolean hasMore() {
    return field("page").get("has_more").asBoolean();
  }

  public String nextBefore() {
    JsonValue v = field("page").get("next_before");
    return v.isNull() || v.isMissing() ? null : v.asString();
  }

  public boolean hasNextPage() {
    return hasMore();
  }

  public RealtimeHistoryPage getNextPage() {
    return Futures.await(getNextPageAsync());
  }

  public CompletableFuture<RealtimeHistoryPage> getNextPageAsync() {
    if (!hasNextPage())
      return Futures.failed(
          new PaginationException("No next page; check hasNextPage() before fetching"));
    if (seen.contains(nextBefore()))
      return Futures.failed(new PaginationException("Repeated history cursor"));
    Set<String> next = new HashSet<>(seen);
    next.add(nextBefore());
    return fetch.apply(nextBefore(), next);
  }

  public Iterable<RealtimeHistoryPage> iterPages() {
    return () ->
        new Iterator<RealtimeHistoryPage>() {
          RealtimeHistoryPage page = RealtimeHistoryPage.this;
          boolean first = true;

          public boolean hasNext() {
            return first || page.hasNextPage();
          }

          public RealtimeHistoryPage next() {
            if (!hasNext()) throw new NoSuchElementException();
            if (first) first = false;
            else page = page.getNextPage();
            return page;
          }
        };
  }

  public Iterator<RealtimeHistoryEvent> iterator() {
    return new Iterator<RealtimeHistoryEvent>() {
      final Iterator<RealtimeHistoryPage> pages = iterPages().iterator();
      Iterator<RealtimeHistoryEvent> items = Collections.emptyIterator();

      public boolean hasNext() {
        while (!items.hasNext() && pages.hasNext()) items = pages.next().events().iterator();
        return items.hasNext();
      }

      public RealtimeHistoryEvent next() {
        if (!hasNext()) throw new NoSuchElementException();
        return items.next();
      }
    };
  }
}
