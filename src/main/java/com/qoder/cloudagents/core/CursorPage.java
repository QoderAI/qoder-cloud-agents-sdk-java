package com.qoder.cloudagents.core;

import com.qoder.cloudagents.core.internal.Futures;
import com.qoder.cloudagents.errors.PaginationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * One cursor page. Iteration lazily loads following pages in server order. Async consumers can
 * chain getNextPageAsync(); no background prefetch occurs.
 */
public abstract class CursorPage<T, P extends CursorPage<T, P>> extends JsonModel
    implements Iterable<T> {
  private final List<T> data;
  private final Map<String, String> query;
  private final Set<String> seen;
  private final boolean pageToken;
  private final java.util.function.BiFunction<
          Map<String, String>, Set<String>, CompletableFuture<P>>
      fetcher;

  protected CursorPage(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      java.util.function.BiFunction<Map<String, String>, Set<String>, CompletableFuture<P>> fetcher,
      java.util.function.Function<JsonValue, T> parser) {
    this(value, requestId, query, seen, fetcher, parser, false);
  }

  protected CursorPage(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      java.util.function.BiFunction<Map<String, String>, Set<String>, CompletableFuture<P>> fetcher,
      java.util.function.Function<JsonValue, T> parser,
      boolean pageToken) {
    super(value, requestId);
    data =
        Collections.unmodifiableList(
            field("data").asArray().stream().map(parser).collect(Collectors.toList()));
    this.query = Collections.unmodifiableMap(new LinkedHashMap<>(query));
    this.seen = Collections.unmodifiableSet(new HashSet<>(seen));
    this.fetcher = fetcher;
    this.pageToken = pageToken;
  }

  public List<T> data() {
    return data;
  }

  public boolean hasMore() {
    return field("has_more").asBoolean();
  }

  public String firstId() {
    return optionalString("first_id");
  }

  public String lastId() {
    return optionalString("last_id");
  }

  private String cursor() {
    return query.containsKey("before_id")
        ? firstId()
        : pageToken && !query.containsKey("after_id") ? optionalString("next_page") : lastId();
  }

  public boolean hasNextPage() {
    return hasMore() && !data.isEmpty() && cursor() != null && !cursor().isEmpty();
  }

  public P getNextPage() {
    return Futures.await(getNextPageAsync());
  }

  public CompletableFuture<P> getNextPageAsync() {
    if (pageToken && query.containsKey("scope_id"))
      return Futures.failed(
          new PaginationException("File scope_id filtering does not support cursor continuation"));
    if (!hasNextPage())
      return Futures.failed(
          new PaginationException("No next page; check hasNextPage() before fetching"));
    if (fetcher == null)
      return Futures.failed(new PaginationException("Detached page cannot fetch a next page"));
    String cursor = cursor();
    if (seen.contains(cursor))
      return Futures.failed(
          new PaginationException("Server returned a repeated pagination cursor"));
    Set<String> nextSeen = new HashSet<>(seen);
    nextSeen.add(cursor);
    Map<String, String> nextQuery = new LinkedHashMap<>(query);
    nextQuery.put(
        query.containsKey("before_id")
            ? "before_id"
            : pageToken && !query.containsKey("after_id") ? "page" : "after_id",
        cursor);
    return fetcher.apply(nextQuery, nextSeen);
  }

  /** Iterates pages lazily, including this page. Request options apply separately to each page. */
  public Iterable<P> iterPages() {
    return () ->
        new Iterator<P>() {
          private P page = self();
          private boolean first = true;

          @Override
          public boolean hasNext() {
            return first || page.hasNextPage();
          }

          @Override
          public P next() {
            if (!hasNext()) throw new NoSuchElementException();
            if (first) first = false;
            else page = page.getNextPage();
            return page;
          }
        };
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      private final Iterator<P> pages = iterPages().iterator();
      private Iterator<T> items = Collections.emptyIterator();

      @Override
      public boolean hasNext() {
        while (!items.hasNext() && pages.hasNext()) items = pages.next().data().iterator();
        return items.hasNext();
      }

      @Override
      public T next() {
        if (!hasNext()) throw new NoSuchElementException();
        return items.next();
      }
    };
  }

  protected abstract P self();
}
